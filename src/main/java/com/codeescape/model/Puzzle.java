package com.codeescape.model;

import com.codeescape.validation.CodeValidator;
import com.codeescape.validation.ValidationResult;
import java.util.List;

public class Puzzle {
    private final String title;
    private final String instructions;
    private final List<String> requiredConcepts;
    private final CodeValidator validator;

    public Puzzle(String title, String instructions, List<String> requiredConcepts, CodeValidator validator) {
        this.title = title;
        this.instructions = instructions;
        this.requiredConcepts = List.copyOf(requiredConcepts);
        this.validator = validator;
    }

    public ValidationResult checkAnswer(String code) {
        return validator.validate(code);
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public List<String> getRequiredConcepts() {
        return requiredConcepts;
    }

    public CodeValidator getValidator() {
        return validator;
    }
}
