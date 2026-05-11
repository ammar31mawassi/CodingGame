package com.codeescape.validation;

import java.util.List;

public class AcceptedAnswerValidator implements CodeValidator {
    private final List<String> acceptedAnswers;

    public AcceptedAnswerValidator(List<String> acceptedAnswers) {
        this.acceptedAnswers = acceptedAnswers.stream()
                .filter(answer -> answer != null && !answer.isBlank())
                .map(AcceptedAnswerValidator::normalize)
                .toList();
    }

    @Override
    public ValidationResult validate(String code) {
        if (acceptedAnswers.contains(normalize(code))) {
            return ValidationResult.success("Correct! The door is unlocked.");
        }

        return ValidationResult.failure("That answer does not match one of the accepted solutions.");
    }

    private static String normalize(String code) {
        return code == null ? "" : code.replaceAll("\\s+", "");
    }
}
