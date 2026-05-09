package com.codeescape.validation;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class IfStatementValidator implements CodeValidator {
    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isIfStmt()) {
            return ValidationResult.failure("Invalid if-statement structure.");
        }

        IfStmt ifStatement = statement.get().asIfStmt();
        if (ifStatement.getElseStmt().isPresent()) {
            return ValidationResult.failure("Use only if for this puzzle, without else.");
        }
        if (!ifStatement.getThenStmt().isBlockStmt()) {
            return ValidationResult.failure("Put the if body inside braces.");
        }

        Expression condition = ifStatement.getCondition();
        if (!condition.findAll(AssignExpr.class).isEmpty()) {
            return ValidationResult.failure("Use a comparison, not assignment, in the condition.");
        }
        if (!isBooleanLikeCondition(condition)) {
            return ValidationResult.failure("The if condition must be boolean.");
        }

        return ValidationResult.success("Valid if");
    }

    private boolean isBooleanLikeCondition(Expression expression) {
        if (expression.isEnclosedExpr()) {
            return isBooleanLikeCondition(expression.asEnclosedExpr().getInner());
        }
        if (expression.isBooleanLiteralExpr()) {
            return true;
        }
        if (expression.isNameExpr()) {
            return VariableDeclarationValidator.checkNameOfVariable(expression.asNameExpr().getNameAsString()).equals("boolean");
        }
        if (expression.isUnaryExpr()) {
            UnaryExpr unary = expression.asUnaryExpr();
            return unary.getOperator() == UnaryExpr.Operator.LOGICAL_COMPLEMENT
                    && isBooleanLikeCondition(unary.getExpression());
        }
        if (expression.isBinaryExpr()) {
            return isBooleanLikeBinary(expression.asBinaryExpr());
        }

        return EducationalExpressionEvaluator.evaluate(expression)
                .map(ExpressionValue::isBoolean)
                .orElse(false);
    }

    private boolean isBooleanLikeBinary(BinaryExpr expression) {
        return switch (expression.getOperator()) {
            case EQUALS, NOT_EQUALS, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS -> true;
            case AND, OR -> isBooleanLikeCondition(expression.getLeft()) && isBooleanLikeCondition(expression.getRight());
            default -> false;
        };
    }
}
