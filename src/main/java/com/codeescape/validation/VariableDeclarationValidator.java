package com.codeescape.validation;

import java.util.Set;

public class VariableDeclarationValidator implements CodeValidator {
    private static final Set<String> VALID_TYPES = Set.of("int", "double", "String", "char", "boolean");

    @Override
    public ValidationResult validate(String code) {
        return ValidationResult.failure("Variable declaration validation is not implemented yet.");
    }

    private boolean isValidVariableName(String name) {
        return name != null && name.matches("[A-Za-z_$][A-Za-z0-9_$]*");
    }

    private boolean isValueCompatibleWithType(String type, String value) {
        return VALID_TYPES.contains(type) && value != null;
    }

    private boolean isInteger(String value) {
        return value != null && value.matches("-?\\d+");
    }

    private boolean isDouble(String value) {
        return value != null && value.matches("-?\\d+(\\.\\d+)?");
    }

    private boolean isStringLiteral(String value) {
        return value != null && value.matches("\".*\"");
    }

    private boolean isCharLiteral(String value) {
        return value != null && value.matches("'.'");
    }

    private boolean isBooleanLiteral(String value) {
        return "true".equals(value) || "false".equals(value);
    }
}
