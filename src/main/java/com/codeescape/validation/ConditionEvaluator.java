package com.codeescape.validation;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class ConditionEvaluator {
    public ValidationResult evaluateIfStatement(String ifStatement) {
        String condition = extractCondition(ifStatement);
        if (condition.isBlank()) {
            return ValidationResult.failure("Could not read the if-statement condition.");
        }

        return evaluateCondition(condition);
    }

    public String extractCondition(String ifStatement) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(ifStatement);
        if (statement.isEmpty() || !statement.get().isIfStmt()) {
            return "";
        }

        return statement.get().asIfStmt().getCondition().toString();
    }

    public ValidationResult evaluateCondition(String condition) {
        Optional<Expression> expression = JavaSyntaxValidator.parseExpressionAst(condition);
        if (expression.isEmpty()) {
            return ValidationResult.failure("Could not read the condition.");
        }

        Optional<ExpressionValue> value = EducationalExpressionEvaluator.evaluate(expression.get());
        if (value.isEmpty()) {
            return ValidationResult.failure("Condition uses an unknown or unsupported value.");
        }
        if (!value.get().isBoolean()) {
            return ValidationResult.failure("Condition must evaluate to boolean.");
        }

        return Boolean.parseBoolean(value.get().value())
                ? ValidationResult.success("Condition is true.")
                : ValidationResult.failure("Condition is false.");
    }

    public boolean usesVariable(String condition, String variableName) {
        Optional<Expression> expression = JavaSyntaxValidator.parseExpressionAst(condition);
        if (expression.isEmpty()) {
            return false;
        }

        return expression.get().findAll(NameExpr.class).stream()
                .anyMatch(name -> name.getNameAsString().equals(variableName));
    }
}
