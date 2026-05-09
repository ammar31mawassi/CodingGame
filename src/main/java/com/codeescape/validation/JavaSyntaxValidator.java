package com.codeescape.validation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public final class JavaSyntaxValidator {
    private JavaSyntaxValidator() {
    }

    public static ValidationResult validateExpression(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.failure("Type a Java expression.");
        }

        return parseExpressionAst(code).isPresent()
                ? ValidationResult.success("Valid Java expression.")
                : ValidationResult.failure("That is not a valid Java expression.");
    }

    public static ValidationResult validateStatement(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.failure("Type a Java statement.");
        }

        return parseStatementAst(code).isPresent()
                ? ValidationResult.success("Valid Java statement.")
                : ValidationResult.failure("That is not a valid Java statement.");
    }

    public static ValidationResult validateClassDeclaration(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.failure("Type a Java class declaration.");
        }

        Optional<CompilationUnit> compilationUnit = parseClassDeclarationAst(code);
        if (compilationUnit.isEmpty()) {
            return ValidationResult.failure("That is not a valid Java class declaration.");
        }
        if (compilationUnit.get().getTypes().size() != 1
                || !(compilationUnit.get().getType(0) instanceof ClassOrInterfaceDeclaration declaration)
                || declaration.isInterface()) {
            return ValidationResult.failure("Type one Java class declaration.");
        }

        return ValidationResult.success("Valid Java class declaration.");
    }

    static Optional<Expression> parseExpressionAst(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(StaticJavaParser.parseExpression(code.trim()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    static Optional<Statement> parseStatementAst(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(StaticJavaParser.parseStatement(code.trim()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    static Optional<BlockStmt> parseBlockAst(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(StaticJavaParser.parseBlock("{\n" + code.trim() + "\n}"));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    static Optional<CompilationUnit> parseClassDeclarationAst(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(StaticJavaParser.parse(code.trim()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
}
