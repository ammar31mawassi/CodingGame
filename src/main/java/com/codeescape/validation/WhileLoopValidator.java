package com.codeescape.validation;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import java.util.Optional;

public class WhileLoopValidator implements CodeValidator {
    private final String variableName;
    private final String limit;

    public WhileLoopValidator(String variableName, String limit) {
        this.variableName = variableName;
        this.limit = limit;
    }

    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isWhileStmt()) {
            return ValidationResult.failure("Build a while loop.");
        }

        WhileStmt whileStatement = statement.get().asWhileStmt();
        if (!whileStatement.getBody().isBlockStmt()) {
            return ValidationResult.failure("Put the while body inside braces.");
        }
        if (!isExpectedCondition(whileStatement.getCondition())) {
            return ValidationResult.failure("Use " + variableName + " < " + limit + " as the loop condition.");
        }
        if (!incrementsVariable(whileStatement.getBody())) {
            return ValidationResult.failure("Increase " + variableName + " inside the loop.");
        }

        return ValidationResult.success("Correct while loop.");
    }

    private boolean isExpectedCondition(Expression condition) {
        Expression expression = unwrap(condition);
        return expression.isBinaryExpr()
                && expression.asBinaryExpr().getOperator() == BinaryExpr.Operator.LESS
                && expression.asBinaryExpr().getLeft().toString().equals(variableName)
                && expression.asBinaryExpr().getRight().toString().equals(limit);
    }

    private boolean incrementsVariable(Statement body) {
        return body.findAll(UnaryExpr.class).stream()
                .anyMatch(unary -> unary.getExpression().toString().equals(variableName)
                        && (unary.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT
                        || unary.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT))
                || body.findAll(AssignExpr.class).stream()
                .anyMatch(assign -> assign.getTarget().toString().equals(variableName)
                        && assign.getValue().toString().contains(variableName));
    }

    private Expression unwrap(Expression expression) {
        if (expression.isEnclosedExpr()) {
            return unwrap(expression.asEnclosedExpr().getInner());
        }
        return expression;
    }
}
