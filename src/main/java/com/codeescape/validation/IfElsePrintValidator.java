package com.codeescape.validation;

import java.util.regex.Pattern;

public class IfElsePrintValidator implements CodeValidator {
    private static final Pattern GRADE_GREATER_THAN_X = Pattern.compile(
            "^if\\s*\\(\\s*(grade\\s*>\\s*x|x\\s*<\\s*grade)\\s*\\)\\s*\\{\\s*"
                    + "System\\.out\\.println\\s*\\(\\s*\"passed\"\\s*\\)\\s*;\\s*}\\s*"
                    + "else\\s*\\{\\s*"
                    + "System\\.out\\.println\\s*\\(\\s*\"failed\"\\s*\\)\\s*;\\s*}\\s*$",
            Pattern.DOTALL
    );

    @Override
    public ValidationResult validate(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.failure("Build the if-else statement.");
        }

        if (!GRADE_GREATER_THAN_X.matcher(code.trim()).matches()) {
            return ValidationResult.failure("Use grade and x with passed/failed print statements.");
        }

        return ValidationResult.success("Correct if-else statement.");
    }
}
