package com.codeescape.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TypedTokenUsageValidator {
    private TypedTokenUsageValidator() {
    }

    public static ValidationResult validate(String code, List<String> collectedTokens) {
        List<String> typedTokens = tokenize(code);
        if (typedTokens.isEmpty()) {
            return ValidationResult.failure("Type code before submitting.");
        }

        Map<String, Integer> availableCounts = new HashMap<>();
        for (String token : collectedTokens) {
            availableCounts.merge(token, 1, Integer::sum);
        }

        for (String token : typedTokens) {
            if (consumeToken(token, availableCounts)) {
                continue;
            }

            if (!consumeDottedTokenParts(token, availableCounts)) {
                return ValidationResult.failure("Collect the '" + token + "' token before typing that code.");
            }
        }

        return ValidationResult.success("Typed code uses collected tokens.");
    }

    private static boolean consumeToken(String token, Map<String, Integer> availableCounts) {
        int available = availableCounts.getOrDefault(token, 0);
        if (available <= 0) {
            return false;
        }
        availableCounts.put(token, available - 1);
        return true;
    }

    private static boolean consumeDottedTokenParts(String token, Map<String, Integer> availableCounts) {
        if (!token.contains(".")) {
            return false;
        }

        Map<String, Integer> trialCounts = new HashMap<>(availableCounts);
        String[] parts = token.split("\\.", -1);
        for (int index = 0; index < parts.length; index++) {
            if (parts[index].isBlank() || !consumeToken(parts[index], trialCounts)) {
                return false;
            }
            if (index < parts.length - 1 && !consumeToken(".", trialCounts)) {
                return false;
            }
        }

        availableCounts.clear();
        availableCounts.putAll(trialCounts);
        return true;
    }

    static List<String> tokenize(String code) {
        List<String> tokens = new ArrayList<>();
        int index = 0;

        while (index < code.length()) {
            char current = code.charAt(index);
            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }

            if (current == '"' || current == '\'') {
                int end = readQuotedToken(code, index, current);
                if (end <= index) {
                    tokens.add(String.valueOf(current));
                    index++;
                } else {
                    tokens.add(code.substring(index, end));
                    index = end;
                }
                continue;
            }

            if (Character.isJavaIdentifierStart(current)) {
                int end = index + 1;
                while (end < code.length() && isIdentifierOrQualifiedPart(code.charAt(end))) {
                    end++;
                }
                tokens.add(code.substring(index, end));
                index = end;
                continue;
            }

            if (current == '-' && index + 1 < code.length() && Character.isDigit(code.charAt(index + 1))) {
                int end = readNumber(code, index + 1);
                tokens.add(code.substring(index, end));
                index = end;
                continue;
            }

            if (Character.isDigit(current)) {
                int end = readNumber(code, index);
                tokens.add(code.substring(index, end));
                index = end;
                continue;
            }

            if (index + 1 < code.length()) {
                String twoCharacterToken = code.substring(index, index + 2);
                if (isTwoCharacterOperator(twoCharacterToken)) {
                    tokens.add(twoCharacterToken);
                    index += 2;
                    continue;
                }
            }

            tokens.add(String.valueOf(current));
            index++;
        }

        return tokens;
    }

    private static boolean isIdentifierOrQualifiedPart(char value) {
        return Character.isJavaIdentifierPart(value) || value == '.';
    }

    private static boolean isTwoCharacterOperator(String token) {
        return token.equals(">=")
                || token.equals("<=")
                || token.equals("==")
                || token.equals("!=")
                || token.equals("&&")
                || token.equals("||")
                || token.equals("++")
                || token.equals("--");
    }

    private static int readNumber(String code, int startIndex) {
        int end = startIndex + 1;
        while (end < code.length() && (Character.isDigit(code.charAt(end)) || code.charAt(end) == '.')) {
            end++;
        }
        return end;
    }

    private static int readQuotedToken(String code, int startIndex, char quote) {
        boolean escaped = false;
        for (int index = startIndex + 1; index < code.length(); index++) {
            char current = code.charAt(index);
            if (escaped) {
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == quote) {
                return index + 1;
            }
        }
        return -1;
    }
}
