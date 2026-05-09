package com.codeescape.validation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import java.util.Optional;

final class EducationalExpressionEvaluator {
    private EducationalExpressionEvaluator() {
    }

    static Optional<ExpressionValue> evaluate(Expression expression) {
        if (expression == null) {
            return Optional.empty();
        }
        if (expression.isEnclosedExpr()) {
            return evaluate(expression.asEnclosedExpr().getInner());
        }
        if (expression.isIntegerLiteralExpr()) {
            return evaluateInteger(expression.asIntegerLiteralExpr());
        }
        if (expression.isDoubleLiteralExpr()) {
            return evaluateDouble(expression.asDoubleLiteralExpr());
        }
        if (expression.isBooleanLiteralExpr()) {
            BooleanLiteralExpr literal = expression.asBooleanLiteralExpr();
            return Optional.of(new ExpressionValue("boolean", String.valueOf(literal.getValue())));
        }
        if (expression.isStringLiteralExpr()) {
            StringLiteralExpr literal = expression.asStringLiteralExpr();
            return Optional.of(new ExpressionValue("String", literal.getValue()));
        }
        if (expression.isCharLiteralExpr()) {
            CharLiteralExpr literal = expression.asCharLiteralExpr();
            return Optional.of(new ExpressionValue("char", literal.getValue()));
        }
        if (expression.isNameExpr()) {
            return resolveName(expression.asNameExpr());
        }
        if (expression.isUnaryExpr()) {
            return evaluateUnary(expression.asUnaryExpr());
        }
        if (expression.isBinaryExpr()) {
            return evaluateBinary(expression.asBinaryExpr());
        }

        return Optional.empty();
    }

    private static Optional<ExpressionValue> evaluateInteger(IntegerLiteralExpr literal) {
        String value = cleanNumber(literal.getValue());
        try {
            Integer.parseInt(value);
            return Optional.of(new ExpressionValue("int", value));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<ExpressionValue> evaluateDouble(DoubleLiteralExpr literal) {
        String value = cleanNumber(literal.getValue());
        try {
            Double.parseDouble(value);
            return Optional.of(new ExpressionValue("double", value));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<ExpressionValue> resolveName(NameExpr name) {
        String variableType = VariableDeclarationValidator.checkNameOfVariable(name.getNameAsString());
        if (variableType.equals("Does not exist")) {
            return Optional.empty();
        }

        return Optional.of(new ExpressionValue(
                variableType,
                VariableDeclarationValidator.checkValueOfVariable(name.getNameAsString())
        ));
    }

    private static Optional<ExpressionValue> evaluateUnary(UnaryExpr expression) {
        Optional<ExpressionValue> value = evaluate(expression.getExpression());
        if (value.isEmpty()) {
            return Optional.empty();
        }

        return switch (expression.getOperator()) {
            case PLUS -> value.get().isNumeric() ? value : Optional.empty();
            case MINUS -> value.get().isNumeric() ? Optional.of(negative(value.get())) : Optional.empty();
            case LOGICAL_COMPLEMENT -> value.get().isBoolean()
                    ? Optional.of(new ExpressionValue("boolean", String.valueOf(!Boolean.parseBoolean(value.get().value()))))
                    : Optional.empty();
            default -> Optional.empty();
        };
    }

    private static ExpressionValue negative(ExpressionValue value) {
        if (value.type().equals("int")) {
            return new ExpressionValue("int", String.valueOf(-Integer.parseInt(value.value())));
        }

        return new ExpressionValue("double", String.valueOf(-Double.parseDouble(value.value())));
    }

    private static Optional<ExpressionValue> evaluateBinary(BinaryExpr expression) {
        Optional<ExpressionValue> left = evaluate(expression.getLeft());
        Optional<ExpressionValue> right = evaluate(expression.getRight());
        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        return switch (expression.getOperator()) {
            case PLUS -> evaluatePlus(left.get(), right.get());
            case MINUS, MULTIPLY, DIVIDE, REMAINDER -> evaluateNumericOperation(left.get(), right.get(), expression.getOperator());
            case GREATER, GREATER_EQUALS, LESS, LESS_EQUALS -> evaluateNumericComparison(left.get(), right.get(), expression.getOperator());
            case EQUALS, NOT_EQUALS -> evaluateEquality(left.get(), right.get(), expression.getOperator());
            case AND, OR -> evaluateBooleanOperation(left.get(), right.get(), expression.getOperator());
            default -> Optional.empty();
        };
    }

    private static Optional<ExpressionValue> evaluatePlus(ExpressionValue left, ExpressionValue right) {
        if (left.type().equals("String") || right.type().equals("String")) {
            return Optional.of(new ExpressionValue("String", left.value() + right.value()));
        }

        return evaluateNumericOperation(left, right, BinaryExpr.Operator.PLUS);
    }

    private static Optional<ExpressionValue> evaluateNumericOperation(
            ExpressionValue left,
            ExpressionValue right,
            BinaryExpr.Operator operator
    ) {
        if (!left.isNumeric() || !right.isNumeric()) {
            return Optional.empty();
        }

        double result = switch (operator) {
            case PLUS -> left.numberValue() + right.numberValue();
            case MINUS -> left.numberValue() - right.numberValue();
            case MULTIPLY -> left.numberValue() * right.numberValue();
            case DIVIDE -> right.numberValue() == 0 ? Double.NaN : left.numberValue() / right.numberValue();
            case REMAINDER -> right.numberValue() == 0 ? Double.NaN : left.numberValue() % right.numberValue();
            default -> Double.NaN;
        };
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return Optional.empty();
        }
        if (left.type().equals("int") && right.type().equals("int") && operator != BinaryExpr.Operator.DIVIDE) {
            return Optional.of(new ExpressionValue("int", String.valueOf((int) result)));
        }

        return Optional.of(new ExpressionValue("double", String.valueOf(result)));
    }

    private static Optional<ExpressionValue> evaluateNumericComparison(
            ExpressionValue left,
            ExpressionValue right,
            BinaryExpr.Operator operator
    ) {
        if (!left.isNumeric() || !right.isNumeric()) {
            return Optional.empty();
        }

        boolean result = switch (operator) {
            case GREATER -> left.numberValue() > right.numberValue();
            case GREATER_EQUALS -> left.numberValue() >= right.numberValue();
            case LESS -> left.numberValue() < right.numberValue();
            case LESS_EQUALS -> left.numberValue() <= right.numberValue();
            default -> false;
        };
        return Optional.of(new ExpressionValue("boolean", String.valueOf(result)));
    }

    private static Optional<ExpressionValue> evaluateEquality(
            ExpressionValue left,
            ExpressionValue right,
            BinaryExpr.Operator operator
    ) {
        boolean result;
        if (left.isNumeric() && right.isNumeric()) {
            result = Double.compare(left.numberValue(), right.numberValue()) == 0;
        } else {
            result = left.type().equals(right.type()) && left.value().equals(right.value());
        }

        if (operator == BinaryExpr.Operator.NOT_EQUALS) {
            result = !result;
        }
        return Optional.of(new ExpressionValue("boolean", String.valueOf(result)));
    }

    private static Optional<ExpressionValue> evaluateBooleanOperation(
            ExpressionValue left,
            ExpressionValue right,
            BinaryExpr.Operator operator
    ) {
        if (!left.isBoolean() || !right.isBoolean()) {
            return Optional.empty();
        }

        boolean leftValue = Boolean.parseBoolean(left.value());
        boolean rightValue = Boolean.parseBoolean(right.value());
        boolean result = operator == BinaryExpr.Operator.AND
                ? leftValue && rightValue
                : leftValue || rightValue;
        return Optional.of(new ExpressionValue("boolean", String.valueOf(result)));
    }

    private static String cleanNumber(String value) {
        return value.replace("_", "");
    }
}
