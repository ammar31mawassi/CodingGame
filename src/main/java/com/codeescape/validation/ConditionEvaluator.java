package com.codeescape.validation;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionEvaluator {
    private static final Pattern IF_STATEMENT_PATTERN = Pattern.compile("^if\\s*\\((.*)\\)\\s*\\{\\s*}$");
    private static final Set<String> VALID_OPERATORS = Set.of("==", "!=", ">", ">=", "<", "<=");

    public ValidationResult evaluateIfStatement(String ifStatement) {
        String condition = extractCondition(ifStatement);
        if (condition.isBlank()) {
            return ValidationResult.failure("Could not read the if-statement condition.");
        }

        return evaluateCondition(condition);
    }

    public String extractCondition(String ifStatement) {
        if (ifStatement == null) {
            return "";
        }

        Matcher matcher = IF_STATEMENT_PATTERN.matcher(ifStatement.trim());
        if (!matcher.matches()) {
            return "";
        }

        return matcher.group(1).trim();
    }

    public ValidationResult evaluateCondition(String condition) {
        if (condition == null || condition.isBlank()) {
            return ValidationResult.failure("Empty condition.");
        }

        String[] tokens = condition.trim().split("\\s+");
        if (tokens.length == 1) {
            return evaluateSingleTokenCondition(tokens[0]);
        }
        if (tokens.length != 3) {
            return ValidationResult.failure("Only simple conditions can be evaluated right now.");
        }

        String operator = tokens[1];
        if (!VALID_OPERATORS.contains(operator)) {
            return ValidationResult.failure("Cannot evaluate operator: " + operator);
        }

        Operand left = resolveOperand(tokens[0]);
        Operand right = resolveOperand(tokens[2]);
        if (left == null || right == null) {
            return ValidationResult.failure("Condition uses an unknown value.");
        }

        Boolean result = evaluateComparison(left, operator, right);
        if (result == null) {
            return ValidationResult.failure("Cannot compare those values.");
        }

        return result
                ? ValidationResult.success("Condition is true.")
                : ValidationResult.failure("Condition is false.");
    }

    public boolean usesVariable(String condition, String variableName) {
        if (condition == null || condition.isBlank()) {
            return false;
        }

        for (String token : condition.trim().split("\\s+")) {
            if (token.equals(variableName)) {
                return true;
            }
        }

        return false;
    }

    private ValidationResult evaluateSingleTokenCondition(String token) {
        Operand operand = resolveOperand(token);
        if (operand == null || !operand.type().equals("boolean")) {
            return ValidationResult.failure("Single-value if conditions must be boolean.");
        }

        return Boolean.parseBoolean(operand.value())
                ? ValidationResult.success("Condition is true.")
                : ValidationResult.failure("Condition is false.");
    }

    private Boolean evaluateComparison(Operand left, String operator, Operand right) {
        if (left.isNumeric() && right.isNumeric()) {
            return compareNumbers(left.numberValue(), operator, right.numberValue());
        }

        if (operator.equals("==") || operator.equals("!=")) {
            boolean valuesMatch = left.type().equals(right.type()) && left.value().equals(right.value());
            return operator.equals("==") ? valuesMatch : !valuesMatch;
        }

        return null;
    }

    private boolean compareNumbers(double left, String operator, double right) {
        return switch (operator) {
            case "==" -> Double.compare(left, right) == 0;
            case "!=" -> Double.compare(left, right) != 0;
            case ">" -> left > right;
            case ">=" -> left >= right;
            case "<" -> left < right;
            case "<=" -> left <= right;
            default -> false;
        };
    }

    private Operand resolveOperand(String token) {
        String variableType = VariableDeclarationValidator.checkNameOfVariable(token);
        if (!variableType.equals("Does not exist")) {
            return new Operand(variableType, VariableDeclarationValidator.checkValueOfVariable(token));
        }
        if (token.matches("-?\\d+")) {
            return new Operand("int", token);
        }
        if (token.matches("-?\\d+\\.\\d+")) {
            return new Operand("double", token);
        }
        if (token.equals("true") || token.equals("false")) {
            return new Operand("boolean", token);
        }
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return new Operand("String", token);
        }
        if (token.startsWith("'") && token.endsWith("'") && token.length() == 3) {
            return new Operand("char", token);
        }

        return null;
    }

    private record Operand(String type, String value) {
        private boolean isNumeric() {
            return type.equals("int") || type.equals("double");
        }

        private double numberValue() {
            return Double.parseDouble(value);
        }
    }
}
