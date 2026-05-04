package com.codeescape.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassDeclarationValidator implements CodeValidator {
    private static final Pattern CLASS_PATTERN = Pattern.compile("^class\\s+([A-Z][A-Za-z0-9_]*)\\s*\\{(.*)}$", Pattern.DOTALL);

    @Override
    public ValidationResult validate(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.failure("Empty class declaration");
        }

        String trimmedCode = code.trim();
        Matcher matcher = CLASS_PATTERN.matcher(trimmedCode);
        if (!matcher.matches()) {
            return ValidationResult.failure("Invalid class structure");
        }

        String className = matcher.group(1);
        if (!isValidClassName(className)) {
            return ValidationResult.failure("Invalid class name");
        }

        String body = matcher.group(2).trim();
        if (body.isEmpty()) {
            return ValidationResult.success("Valid class declaration");
        }

        if (!body.endsWith(";")) {
            return ValidationResult.failure("Class body can only contain field declarations");
        }

        VariableDeclarationValidator fieldValidator = VariableDeclarationValidator.getInstance();
        Set<String> fieldNames = new HashSet<>();
        for (String field : body.split(";")) {
            String fieldDeclaration = field.trim();
            if (fieldDeclaration.isEmpty()) {
                continue;
            }

            String fieldName = extractFieldName(fieldDeclaration);
            if (fieldName == null || fieldNames.contains(fieldName)) {
                return ValidationResult.failure("Invalid or duplicate field name");
            }
            fieldNames.add(fieldName);

            ValidationResult result = fieldValidator.validateFieldDeclaration(fieldDeclaration + ";");
            if (!result.isValid()) {
                return ValidationResult.failure("Invalid field declaration: " + fieldDeclaration);
            }
        }

        return ValidationResult.success("Valid class declaration");
    }

    private boolean isValidClassName(String name) {
        return name != null && name.matches("[A-Z][A-Za-z0-9_]*");
    }

    private boolean hasValidBraces(String code) {
        return code != null && code.contains("{") && code.endsWith("}");
    }

    private String extractFieldName(String fieldDeclaration) {
        String[] words = fieldDeclaration.trim().split("\\s+");
        if (words.length < 2) {
            return null;
        }

        return words[1];
    }
}
