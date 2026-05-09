package com.codeescape.validation;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClassConstructorMethodValidator implements CodeValidator {
    @Override
    public ValidationResult validate(String code) {
        Optional<ClassOrInterfaceDeclaration> declaration = parseSingleClass(code);
        if (declaration.isEmpty() || !"Player".equals(declaration.get().getNameAsString())) {
            return ValidationResult.failure("Build a class named Player.");
        }

        Map<String, String> fields = new HashMap<>();
        ConstructorDeclaration constructor = null;
        MethodDeclaration healMethod = null;
        for (BodyDeclaration<?> member : declaration.get().getMembers()) {
            if (member instanceof FieldDeclaration field) {
                if (!collectField(field, fields)) {
                    return ValidationResult.failure("Player needs simple name and health fields.");
                }
            } else if (member instanceof ConstructorDeclaration foundConstructor) {
                if (constructor != null) {
                    return ValidationResult.failure("Use one Player constructor.");
                }
                constructor = foundConstructor;
            } else if (member instanceof MethodDeclaration method) {
                if (healMethod != null) {
                    return ValidationResult.failure("Use one heal method.");
                }
                healMethod = method;
            } else {
                return ValidationResult.failure("Player should contain fields, one constructor, and one method.");
            }
        }

        if (fields.size() != 2 || !"String".equals(fields.get("name")) || !"int".equals(fields.get("health"))) {
            return ValidationResult.failure("Player needs String name and int health fields.");
        }
        if (constructor == null || !isValidConstructor(constructor)) {
            return ValidationResult.failure("The constructor must receive name and health, then assign both fields.");
        }
        if (healMethod == null || !isValidHealMethod(healMethod)) {
            return ValidationResult.failure("Add void heal() that increases health by 1.");
        }

        return ValidationResult.success("Valid Player class");
    }

    private boolean collectField(FieldDeclaration field, Map<String, String> fields) {
        if (!field.getModifiers().isEmpty()) {
            return false;
        }
        for (VariableDeclarator variable : field.getVariables()) {
            String name = variable.getNameAsString();
            if (variable.getInitializer().isPresent() || fields.containsKey(name)) {
                return false;
            }
            fields.put(name, variable.getType().asString());
        }
        return true;
    }

    private boolean isValidConstructor(ConstructorDeclaration constructor) {
        if (!"Player".equals(constructor.getNameAsString()) || constructor.getParameters().size() != 2) {
            return false;
        }

        List<Parameter> parameters = constructor.getParameters();
        if (!"String".equals(parameters.get(0).getType().asString())
                || !"name".equals(parameters.get(0).getNameAsString())
                || !"int".equals(parameters.get(1).getType().asString())
                || !"health".equals(parameters.get(1).getNameAsString())) {
            return false;
        }

        List<AssignExpr> assignments = constructor.getBody().findAll(AssignExpr.class);
        return hasAssignment(assignments, "name") && hasAssignment(assignments, "health");
    }

    private boolean hasAssignment(List<AssignExpr> assignments, String fieldName) {
        return assignments.stream().anyMatch(assignment -> assignment.getOperator() == AssignExpr.Operator.ASSIGN
                && isThisField(assignment.getTarget(), fieldName)
                && assignment.getValue().isNameExpr()
                && fieldName.equals(assignment.getValue().asNameExpr().getNameAsString()));
    }

    private boolean isValidHealMethod(MethodDeclaration method) {
        return "heal".equals(method.getNameAsString())
                && method.getType().isVoidType()
                && method.getParameters().isEmpty()
                && method.getBody().isPresent()
                && method.getBody().get().findAll(Expression.class).stream().anyMatch(this::incrementsHealth);
    }

    private boolean incrementsHealth(Expression expression) {
        if (expression instanceof UnaryExpr unary) {
            return (unary.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT
                    || unary.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT)
                    && isHealthTarget(unary.getExpression());
        }
        if (expression instanceof AssignExpr assignment) {
            return assignment.getOperator() == AssignExpr.Operator.PLUS
                    && isHealthTarget(assignment.getTarget())
                    && isOne(assignment.getValue())
                    || assignment.getOperator() == AssignExpr.Operator.ASSIGN
                    && isHealthTarget(assignment.getTarget())
                    && isHealthPlusOne(assignment.getValue());
        }
        return false;
    }

    private boolean isHealthPlusOne(Expression expression) {
        if (!(expression instanceof BinaryExpr binary) || binary.getOperator() != BinaryExpr.Operator.PLUS) {
            return false;
        }
        return (isHealthTarget(binary.getLeft()) && isOne(binary.getRight()))
                || (isOne(binary.getLeft()) && isHealthTarget(binary.getRight()));
    }

    private boolean isOne(Expression expression) {
        return expression instanceof IntegerLiteralExpr integerLiteral
                && "1".equals(integerLiteral.getValue());
    }

    private boolean isHealthTarget(Expression expression) {
        if (expression instanceof NameExpr name) {
            return "health".equals(name.getNameAsString());
        }
        return isThisField(expression, "health");
    }

    private boolean isThisField(Expression expression, String fieldName) {
        return expression instanceof FieldAccessExpr fieldAccess
                && fieldName.equals(fieldAccess.getNameAsString())
                && fieldAccess.getScope() instanceof ThisExpr;
    }

    private Optional<ClassOrInterfaceDeclaration> parseSingleClass(String code) {
        return JavaSyntaxValidator.parseClassDeclarationAst(code)
                .filter(compilationUnit -> compilationUnit.getTypes().size() == 1)
                .map(compilationUnit -> compilationUnit.getType(0))
                .filter(type -> type instanceof ClassOrInterfaceDeclaration)
                .map(type -> (ClassOrInterfaceDeclaration) type)
                .filter(type -> !type.isInterface());
    }
}
