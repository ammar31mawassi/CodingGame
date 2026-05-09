package com.codeescape.validation;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClassFieldsValidator implements CodeValidator {
    @Override
    public ValidationResult validate(String code) {
        Optional<ClassOrInterfaceDeclaration> declaration = parseSingleClass(code);
        if (declaration.isEmpty() || !"Item".equals(declaration.get().getNameAsString())) {
            return ValidationResult.failure("Build a class named Item.");
        }

        Map<String, String> fields = new HashMap<>();
        for (BodyDeclaration<?> member : declaration.get().getMembers()) {
            if (!(member instanceof FieldDeclaration field)) {
                return ValidationResult.failure("Item should only contain fields in this puzzle.");
            }
            if (!field.getModifiers().isEmpty()) {
                return ValidationResult.failure("Use simple fields without modifiers.");
            }
            for (VariableDeclarator variable : field.getVariables()) {
                if (variable.getInitializer().isPresent() || fields.containsKey(variable.getNameAsString())) {
                    return ValidationResult.failure("Use each required field once without initial values.");
                }
                fields.put(variable.getNameAsString(), variable.getType().asString());
            }
        }

        return fields.size() == 2
                && "String".equals(fields.get("name"))
                && "int".equals(fields.get("power"))
                ? ValidationResult.success("Valid Item class")
                : ValidationResult.failure("Item needs exactly String name and int power fields.");
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
