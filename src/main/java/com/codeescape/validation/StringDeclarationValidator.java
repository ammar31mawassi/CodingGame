package com.codeescape.validation;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class StringDeclarationValidator implements CodeValidator {
    private final String requiredValue;
    private final VariableDeclarationValidator variableDeclarationValidator;

    public StringDeclarationValidator(String requiredValue) {
        this.requiredValue = unquote(requiredValue);
        this.variableDeclarationValidator = VariableDeclarationValidator.getInstance();
    }

    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isExpressionStmt()) {
            return ValidationResult.failure("That is not a String declaration.");
        }
        Expression expression = statement.get().asExpressionStmt().getExpression();
        if (!expression.isVariableDeclarationExpr()
                || expression.asVariableDeclarationExpr().getVariables().size() != 1) {
            return ValidationResult.failure("That is not a String declaration.");
        }

        VariableDeclarator variable = expression.asVariableDeclarationExpr().getVariables().get(0);
        if (!variable.getType().asString().equals("String") || variable.getInitializer().isEmpty()) {
            return ValidationResult.failure("That is not a String declaration.");
        }
        Expression initializer = variable.getInitializer().get();
        if (!initializer.isStringLiteralExpr() || !initializer.asStringLiteralExpr().getValue().equals(requiredValue)) {
            return ValidationResult.failure("That is not the required String value.");
        }

        ValidationResult result = variableDeclarationValidator.validate(code);
        if (!result.isValid()) {
            return result;
        }

        return ValidationResult.success("Correct String declaration.");
    }

    private String unquote(String value) {
        if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }

        return value == null ? "" : value;
    }
}
