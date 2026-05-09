package com.codeescape.validation;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.Statement;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class VariableDeclarationValidator implements CodeValidator {
    private record VariableData(String type, String value) {
    }

    private static final Set<String> VALID_TYPES = Set.of("int", "double", "char", "String", "boolean");
    private static final Set<String> INVALID_WORDS = Set.of(
            "int", "double", "char", "String", "boolean", "while", "if", "for", "class"
    );
    private static HashMap<String, VariableData> variables;
    private static volatile VariableDeclarationValidator instance;

    private VariableDeclarationValidator() {
        variables = new HashMap<>();
    }

    public static VariableDeclarationValidator getInstance() {
        if (instance == null) {
            synchronized (VariableDeclarationValidator.class) {
                if (instance == null) {
                    instance = new VariableDeclarationValidator();
                }
            }
        }
        return instance;
    }

    public static String checkNameOfVariable(String variableName) {
        getInstance();
        VariableData variableData = variables.get(variableName);
        return variableData == null ? "Does not exist" : variableData.type();
    }

    public static String checkValueOfVariable(String variableName) {
        getInstance();
        VariableData variableData = variables.get(variableName);
        return variableData == null ? "Does not exist" : variableData.value();
    }

    @Override
    public ValidationResult validate(String line) {
        return validateDeclaration(line, true);
    }

    public ValidationResult validateFieldDeclaration(String line) {
        if (line == null || line.isBlank()) {
            return ValidationResult.failure("Empty field declaration");
        }
        Optional<ClassOrInterfaceDeclaration> wrapperClass = parseFieldWrapper(line);
        if (wrapperClass.isEmpty() || wrapperClass.get().getMembers().size() != 1) {
            return ValidationResult.failure("Invalid field declaration: " + line.trim());
        }
        if (!(wrapperClass.get().getMembers().get(0) instanceof FieldDeclaration field)) {
            return ValidationResult.failure("Invalid field declaration: " + line.trim());
        }

        return validateField(field);
    }

    private ValidationResult validateDeclaration(String line, boolean rememberVariable) {
        if (line == null || line.isBlank()) {
            return ValidationResult.failure("Empty variable declaration");
        }

        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(line);
        if (statement.isEmpty() || !statement.get().isExpressionStmt()) {
            return ValidationResult.failure("That is not a variable declaration.");
        }

        Expression expression = statement.get().asExpressionStmt().getExpression();
        if (!expression.isVariableDeclarationExpr()) {
            return ValidationResult.failure("That is not a variable declaration.");
        }

        VariableDeclarationExpr declaration = expression.asVariableDeclarationExpr();
        if (declaration.getVariables().size() != 1) {
            return ValidationResult.failure("Declare one variable at a time.");
        }

        VariableDeclarator variable = declaration.getVariables().get(0);
        if (variable.getInitializer().isEmpty()) {
            return ValidationResult.failure("The variable needs an initial value.");
        }

        return validateVariable(variable, rememberVariable);
    }

    private ValidationResult validateField(FieldDeclaration field) {
        if (field.getVariables().size() != 1) {
            return ValidationResult.failure("Declare one field at a time.");
        }

        VariableDeclarator variable = field.getVariables().get(0);
        if (!isValidVariableName(variable.getNameAsString(), false)) {
            return ValidationResult.failure("Not a valid field name: " + variable.getNameAsString());
        }
        if (variable.getInitializer().isPresent()) {
            Optional<ExpressionValue> value = EducationalExpressionEvaluator.evaluate(variable.getInitializer().get());
            if (isSupportedValueType(variable.getType().asString()) && value.isEmpty()) {
                return ValidationResult.failure("Cannot evaluate the field value.");
            }
            if (value.isPresent() && !isCompatibleValue(variable.getType().asString(), value.get())) {
                return ValidationResult.failure("Field value does not match the field type.");
            }
        }

        return ValidationResult.success("Successful Field Declaration");
    }

    private ValidationResult validateVariable(VariableDeclarator variable, boolean rememberVariable) {
        String type = variable.getType().asString();
        String name = variable.getNameAsString();
        if (!isSupportedValueType(type)) {
            return ValidationResult.failure("Not a valid TYPE in line: " + type);
        }
        if (!isValidVariableName(name, rememberVariable)) {
            return ValidationResult.failure("Not valid Variable name in the line: " + name);
        }

        Optional<ExpressionValue> value = EducationalExpressionEvaluator.evaluate(variable.getInitializer().orElseThrow());
        if (value.isEmpty()) {
            return ValidationResult.failure("Cannot evaluate the value in the declaration.");
        }
        if (!isCompatibleValue(type, value.get())) {
            return ValidationResult.failure("Value does not match declared type.");
        }

        rememberVariable(name, type, value.get().value(), rememberVariable);
        return ValidationResult.success("Successful " + type + " Declaration");
    }

    private Optional<ClassOrInterfaceDeclaration> parseFieldWrapper(String line) {
        return JavaSyntaxValidator.parseClassDeclarationAst("class Temp { " + line.trim() + " }")
                .filter(compilationUnit -> compilationUnit.getTypes().size() == 1)
                .map(compilationUnit -> compilationUnit.getType(0))
                .filter(type -> type instanceof ClassOrInterfaceDeclaration)
                .map(type -> (ClassOrInterfaceDeclaration) type);
    }

    private void rememberVariable(String name, String type, String value, boolean rememberVariable) {
        if (rememberVariable) {
            variables.putIfAbsent(name, new VariableData(type, value));
        }
    }

    static boolean isSupportedValueType(String type) {
        return VALID_TYPES.contains(type);
    }

    static boolean isCompatibleValue(String declaredType, ExpressionValue value) {
        return switch (declaredType) {
            case "int" -> value.type().equals("int");
            case "double" -> value.type().equals("int") || value.type().equals("double");
            case "char" -> value.type().equals("char");
            case "String" -> value.type().equals("String");
            case "boolean" -> value.type().equals("boolean");
            default -> true;
        };
    }

    static boolean isValidVariableName(String name, boolean checkExistingVariable) {
        getInstance();
        if (name == null || !name.matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
            return false;
        }
        if (checkExistingVariable && variables.containsKey(name)) {
            return false;
        }

        return !INVALID_WORDS.contains(name);
    }

    public static void resetVariables() {
        getInstance();
        variables.clear();
    }
}
