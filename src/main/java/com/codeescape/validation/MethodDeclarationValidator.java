package com.codeescape.validation;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class MethodDeclarationValidator implements CodeValidator {
    private final String expectedReturnType;
    private final String expectedName;
    private final String expectedPrintArgument;
    private final String expectedReturnExpression;

    private MethodDeclarationValidator(
            String expectedReturnType,
            String expectedName,
            String expectedPrintArgument,
            String expectedReturnExpression
    ) {
        this.expectedReturnType = expectedReturnType;
        this.expectedName = expectedName;
        this.expectedPrintArgument = expectedPrintArgument;
        this.expectedReturnExpression = expectedReturnExpression;
    }

    public static MethodDeclarationValidator printMethod(
            String expectedReturnType,
            String expectedName,
            String expectedPrintArgument
    ) {
        return new MethodDeclarationValidator(expectedReturnType, expectedName, expectedPrintArgument, null);
    }

    public static MethodDeclarationValidator returnMethod(
            String expectedReturnType,
            String expectedName,
            String expectedReturnExpression
    ) {
        return new MethodDeclarationValidator(expectedReturnType, expectedName, null, expectedReturnExpression);
    }

    @Override
    public ValidationResult validate(String code) {
        Optional<MethodDeclaration> method = parseSingleMethod(code);
        if (method.isEmpty()) {
            return ValidationResult.failure("Build one method.");
        }

        MethodDeclaration declaration = method.get();
        if (!declaration.getType().asString().equals(expectedReturnType)
                || !declaration.getNameAsString().equals(expectedName)
                || !declaration.getParameters().isEmpty()
                || declaration.getBody().isEmpty()) {
            return ValidationResult.failure("Use the expected method header.");
        }

        if (expectedPrintArgument != null && !hasSingleExpectedPrint(declaration)) {
            return ValidationResult.failure("Print the expected value inside the method.");
        }
        if (expectedReturnExpression != null && !hasSingleExpectedReturn(declaration)) {
            return ValidationResult.failure("Return the expected value from the method.");
        }

        return ValidationResult.success("Correct method declaration.");
    }

    private Optional<MethodDeclaration> parseSingleMethod(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return JavaSyntaxValidator.parseClassDeclarationAst("class Temp { " + code.trim() + " }")
                .filter(unit -> unit.getTypes().size() == 1)
                .map(unit -> unit.getType(0))
                .filter(type -> type instanceof ClassOrInterfaceDeclaration)
                .map(type -> (ClassOrInterfaceDeclaration) type)
                .filter(type -> type.getMembers().size() == 1)
                .map(type -> type.getMembers().get(0))
                .filter(member -> member instanceof MethodDeclaration)
                .map(member -> (MethodDeclaration) member);
    }

    private boolean hasSingleExpectedPrint(MethodDeclaration declaration) {
        if (declaration.getBody().orElseThrow().getStatements().size() != 1) {
            return false;
        }
        Statement statement = declaration.getBody().orElseThrow().getStatement(0);
        return statement.isExpressionStmt()
                && new PrintStatementValidator(expectedPrintArgument)
                .isExpectedPrintCall(statement.asExpressionStmt().getExpression());
    }

    private boolean hasSingleExpectedReturn(MethodDeclaration declaration) {
        if (declaration.getBody().orElseThrow().getStatements().size() != 1) {
            return false;
        }
        Statement statement = declaration.getBody().orElseThrow().getStatement(0);
        if (!statement.isReturnStmt() || statement.asReturnStmt().getExpression().isEmpty()) {
            return false;
        }

        Expression expression = statement.asReturnStmt().getExpression().get();
        return expression.toString().equals(expectedReturnExpression);
    }
}
