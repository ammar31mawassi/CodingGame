package com.codeescape.validation;

import java.util.Set;

public class IfStatementValidator implements CodeValidator {
    private static final Set<String> VALID_OPERATORS = Set.of("==", "!=", ">", ">=", "<", "<=");

    @Override
    public ValidationResult validate(String code) {
        if (!hasValidStructure(code)) return ValidationResult.failure("Invalid structure");
        String condition = code.substring(code.indexOf("(") + 1, code.indexOf(")"));
        if (!isValidCondition(condition)) return ValidationResult.failure("Invalid condition");
        String[] condTokens = condition.split(" ");
        if (condTokens.length != 3) {
            if (condTokens.length == 1){
                VariableDeclarationValidator.getInstance();
                String varName = VariableDeclarationValidator.checkNameOfVariable(condTokens[0]);
                if (varName.equals("boolean")) return ValidationResult.success("Valid if");
                else return ValidationResult.failure("Invalid variable in the condition");
            }
            return ValidationResult.failure("Invalid token");
        }
        if (!isValidOperator(condTokens[1]) || condTokens[0].isBlank() || condTokens[2].isBlank()) return ValidationResult.failure("Invalid operator or condition");
        return  ValidationResult.success("Valid if");
    }

    private boolean hasValidStructure(String code) {
        return code != null && code.startsWith("if");
    }

    private boolean isValidCondition(String condition) {
        return condition != null && !condition.isBlank();
    }

    private boolean isValidOperator(String operator) {
        return VALID_OPERATORS.contains(operator);
    }
}
