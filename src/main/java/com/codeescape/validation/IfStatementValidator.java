package com.codeescape.validation;

import java.util.Set;

public class IfStatementValidator implements CodeValidator {
    private static final Set<String> VALID_OPERATORS = Set.of("==", "!=", ">", ">=", "<", "<=");

    @Override
    public ValidationResult validate(String code) {
        return ValidationResult.failure("If-statement validation is not implemented yet.");
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
