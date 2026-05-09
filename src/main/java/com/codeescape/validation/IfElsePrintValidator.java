package com.codeescape.validation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class IfElsePrintValidator implements CodeValidator {
    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isIfStmt()) {
            return ValidationResult.failure("Build the if-else statement.");
        }

        var ifStatement = statement.get().asIfStmt();
        if (ifStatement.getElseStmt().isEmpty()) {
            return ValidationResult.failure("Add an else branch.");
        }
        if (!isGradeGreaterThanX(ifStatement.getCondition())) {
            return ValidationResult.failure("Use grade and x in the condition.");
        }
        if (!printsExpectedMessage(ifStatement.getThenStmt(), "passed")) {
            return ValidationResult.failure("Print \"passed\" when the condition is true.");
        }
        if (!printsExpectedMessage(ifStatement.getElseStmt().get(), "failed")) {
            return ValidationResult.failure("Print \"failed\" in the else branch.");
        }

        return ValidationResult.success("Correct if-else statement.");
    }

    private boolean isGradeGreaterThanX(Expression condition) {
        Expression expression = unwrap(condition);
        if (!expression.isBinaryExpr()) {
            return false;
        }

        BinaryExpr binary = expression.asBinaryExpr();
        String left = binary.getLeft().toString();
        String right = binary.getRight().toString();
        return (binary.getOperator() == BinaryExpr.Operator.GREATER && left.equals("grade") && right.equals("x"))
                || (binary.getOperator() == BinaryExpr.Operator.LESS && left.equals("x") && right.equals("grade"));
    }

    private boolean printsExpectedMessage(Statement statement, String expectedMessage) {
        Statement printStatement = unwrapSingleBlock(statement);
        if (!printStatement.isExpressionStmt()) {
            return false;
        }
        Expression expression = printStatement.asExpressionStmt().getExpression();
        if (!expression.isMethodCallExpr()) {
            return false;
        }

        MethodCallExpr methodCall = expression.asMethodCallExpr();
        return methodCall.getNameAsString().equals("println")
                && methodCall.getScope().map(scope -> scope.toString().equals("System.out")).orElse(false)
                && methodCall.getArguments().size() == 1
                && methodCall.getArgument(0).isStringLiteralExpr()
                && methodCall.getArgument(0).asStringLiteralExpr().getValue().equals(expectedMessage);
    }

    private Statement unwrapSingleBlock(Statement statement) {
        if (statement.isBlockStmt() && statement.asBlockStmt().getStatements().size() == 1) {
            return statement.asBlockStmt().getStatement(0);
        }

        return statement;
    }

    private Expression unwrap(Expression expression) {
        if (expression.isEnclosedExpr()) {
            return unwrap(expression.asEnclosedExpr().getInner());
        }

        return expression;
    }
}
