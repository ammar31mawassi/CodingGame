package com.codeescape.validation;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ClassDeclarationValidator implements CodeValidator {
    @Override
    public ValidationResult validate(String code) {
        Optional<ClassOrInterfaceDeclaration> classDeclaration = parseSingleClass(code);
        if (classDeclaration.isEmpty()) {
            return ValidationResult.failure("Invalid class structure");
        }

        ClassOrInterfaceDeclaration declaration = classDeclaration.get();
        if (!isValidClassName(declaration.getNameAsString())) {
            return ValidationResult.failure("Invalid class name");
        }

        Set<String> fieldNames = new HashSet<>();
        for (BodyDeclaration<?> member : declaration.getMembers()) {
            ValidationResult result = validateMember(member, declaration.getNameAsString(), fieldNames);
            if (!result.isValid()) {
                return result;
            }
        }

        return ValidationResult.success("Valid class declaration");
    }

    private Optional<ClassOrInterfaceDeclaration> parseSingleClass(String code) {
        return JavaSyntaxValidator.parseClassDeclarationAst(code)
                .filter(compilationUnit -> compilationUnit.getTypes().size() == 1)
                .map(compilationUnit -> compilationUnit.getType(0))
                .filter(type -> type instanceof ClassOrInterfaceDeclaration)
                .map(type -> (ClassOrInterfaceDeclaration) type)
                .filter(type -> !type.isInterface());
    }

    private ValidationResult validateMember(
            BodyDeclaration<?> member,
            String className,
            Set<String> fieldNames
    ) {
        if (member instanceof FieldDeclaration field) {
            return validateField(field, fieldNames);
        }
        if (member instanceof ConstructorDeclaration constructor) {
            return validateConstructor(constructor, className);
        }
        if (member instanceof MethodDeclaration method) {
            return validateMethod(method);
        }

        return ValidationResult.failure("Class body can only contain fields, constructors, and simple methods.");
    }

    private ValidationResult validateField(FieldDeclaration field, Set<String> fieldNames) {
        for (VariableDeclarator variable : field.getVariables()) {
            String fieldName = variable.getNameAsString();
            if (!VariableDeclarationValidator.isValidVariableName(fieldName, false) || fieldNames.contains(fieldName)) {
                return ValidationResult.failure("Invalid or duplicate field name");
            }
            fieldNames.add(fieldName);

            if (variable.getInitializer().isPresent()) {
                Optional<ExpressionValue> value = EducationalExpressionEvaluator.evaluate(variable.getInitializer().get());
                String type = variable.getType().asString();
                if (VariableDeclarationValidator.isSupportedValueType(type) && value.isEmpty()) {
                    return ValidationResult.failure("Invalid field declaration: " + fieldName);
                }
                if (value.isPresent() && !VariableDeclarationValidator.isCompatibleValue(type, value.get())) {
                    return ValidationResult.failure("Invalid field declaration: " + fieldName);
                }
            }
        }

        return ValidationResult.success("Valid field declaration");
    }

    private ValidationResult validateConstructor(ConstructorDeclaration constructor, String className) {
        return constructor.getNameAsString().equals(className)
                ? ValidationResult.success("Valid constructor declaration")
                : ValidationResult.failure("Constructor name must match the class name.");
    }

    private ValidationResult validateMethod(MethodDeclaration method) {
        return method.getBody().isPresent()
                ? ValidationResult.success("Valid method declaration")
                : ValidationResult.failure("Methods need a body.");
    }

    private boolean isValidClassName(String name) {
        return name != null && name.matches("[A-Z][A-Za-z0-9_]*");
    }
}
