package com.codeescape.validation;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class CharDeclarationValidator implements CodeValidator {
    private final String requiredValue;

    public CharDeclarationValidator(String requiredValue) {
        this.requiredValue = requiredValue;
    }

    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isExpressionStmt()) {
            return ValidationResult.failure("That is not a char declaration.");
        }

        Expression expression = statement.get().asExpressionStmt().getExpression();
        if (!expression.isVariableDeclarationExpr()
                || expression.asVariableDeclarationExpr().getVariables().size() != 1) {
            return ValidationResult.failure("That is not a char declaration.");
        }

        VariableDeclarator variable = expression.asVariableDeclarationExpr().getVariables().get(0);
        if (!variable.getType().asString().equals("char") || variable.getInitializer().isEmpty()) {
            return ValidationResult.failure("Declare a char with a value.");
        }
        if (!variable.getInitializer().get().toString().equals(requiredValue)) {
            return ValidationResult.failure("That is not the required char value.");
        }

        return VariableDeclarationValidator.getInstance().validate(code);
    }
}
