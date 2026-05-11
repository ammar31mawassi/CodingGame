package com.codeescape.validation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class IfElsePrintValidator implements CodeValidator {
    private final String leftOperand;
    private final String rightOperand;
    private final BinaryExpr.Operator operator;
    private final String trueMessage;
    private final String falseMessage;

    public IfElsePrintValidator() {
        this("grade", "x", BinaryExpr.Operator.GREATER, "passed", "failed");
    }

    public IfElsePrintValidator(
            String leftOperand,
            String rightOperand,
            BinaryExpr.Operator operator,
            String trueMessage,
            String falseMessage
    ) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
        this.trueMessage = trueMessage;
        this.falseMessage = falseMessage;
    }

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
        if (!isExpectedCondition(ifStatement.getCondition())) {
            return ValidationResult.failure("Use the expected condition.");
        }
        if (!printsExpectedMessage(ifStatement.getThenStmt(), trueMessage)) {
            return ValidationResult.failure("Print \"" + trueMessage + "\" when the condition is true.");
        }
        if (!printsExpectedMessage(ifStatement.getElseStmt().get(), falseMessage)) {
            return ValidationResult.failure("Print \"" + falseMessage + "\" in the else branch.");
        }

        return ValidationResult.success("Correct if-else statement.");
    }

    private boolean isExpectedCondition(Expression condition) {
        Expression expression = unwrap(condition);
        if (!expression.isBinaryExpr()) {
            return false;
        }

        BinaryExpr binary = expression.asBinaryExpr();
        String left = binary.getLeft().toString();
        String right = binary.getRight().toString();
        return (binary.getOperator() == operator
                && left.equals(leftOperand)
                && right.equals(rightOperand))
                || (binary.getOperator() == reversedOperator(operator)
                && left.equals(rightOperand)
                && right.equals(leftOperand));
    }

    private BinaryExpr.Operator reversedOperator(BinaryExpr.Operator operator) {
        return switch (operator) {
            case GREATER -> BinaryExpr.Operator.LESS;
            case GREATER_EQUALS -> BinaryExpr.Operator.LESS_EQUALS;
            case LESS -> BinaryExpr.Operator.GREATER;
            case LESS_EQUALS -> BinaryExpr.Operator.GREATER_EQUALS;
            default -> operator;
        };
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
