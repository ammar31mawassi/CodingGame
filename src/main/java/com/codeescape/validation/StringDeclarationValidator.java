package com.codeescape.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringDeclarationValidator implements CodeValidator {
    private static final Pattern STRING_DECLARATION_PATTERN = Pattern.compile(
            "^String\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\s*=\\s*(\".*\")\\s*;$"
    );

    private final String requiredValue;
    private final VariableDeclarationValidator variableDeclarationValidator;

    public StringDeclarationValidator(String requiredValue) {
        this.requiredValue = requiredValue;
        this.variableDeclarationValidator = VariableDeclarationValidator.getInstance();
    }

    @Override
    public ValidationResult validate(String code) {
        if (code == null) {
            return ValidationResult.failure("That is not a String declaration.");
        }

        Matcher matcher = STRING_DECLARATION_PATTERN.matcher(code.trim());
        if (!matcher.matches()) {
            return ValidationResult.failure("That is not a String declaration.");
        }

        String value = matcher.group(2);
        if (!requiredValue.equals(value)) {
            return ValidationResult.failure("That is not the required String value.");
        }

        ValidationResult result = variableDeclarationValidator.validate(code);
        if (!result.isValid()) {
            return result;
        }

        return ValidationResult.success("Correct String declaration.");
    }
}
