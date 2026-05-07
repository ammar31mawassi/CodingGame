package com.codeescape.model;

import java.util.Map;

public final class TokenDescriptions {
    private static final Map<String, String> DESCRIPTIONS = Map.ofEntries(
            Map.entry("int", "int is a Java type for whole numbers."),
            Map.entry("double", "double is a Java type for decimal numbers."),
            Map.entry("String", "String is a Java type for text."),
            Map.entry("char", "char is a Java type for one character."),
            Map.entry("boolean", "boolean is a Java type that can be true or false."),
            Map.entry("if", "if starts code that only runs when its condition is true."),
            Map.entry("else", "else runs when the if condition is false."),
            Map.entry("System.out.println", "System.out.println prints text or values to the console."),
            Map.entry("=", "= assigns a value to a variable."),
            Map.entry("==", "== checks whether two values are equal."),
            Map.entry(">", "> checks whether the left value is greater."),
            Map.entry(">=", ">= checks whether the left value is greater or equal."),
            Map.entry("<", "< checks whether the left value is smaller."),
            Map.entry("(", "( starts a condition or method arguments."),
            Map.entry(")", ") ends a condition or method arguments."),
            Map.entry("{", "{ starts a block of code."),
            Map.entry("}", "} ends a block of code."),
            Map.entry(";", "; ends a Java statement."),
            Map.entry("x", "x is a variable name used to store a value."),
            Map.entry("age", "age is a variable name that can store someone's age."),
            Map.entry("name", "name is a variable name that can store text."),
            Map.entry("grade", "grade is a variable name that can store a score.")
    );

    private TokenDescriptions() {
    }

    public static String describe(String token) {
        if (token == null || token.isBlank()) {
            return "This token can be used in your code answer.";
        }

        String description = DESCRIPTIONS.get(token);
        if (description != null) {
            return description;
        }
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token + " is a String literal, which means exact text in quotes.";
        }
        if (token.startsWith("'") && token.endsWith("'")) {
            return token + " is a char literal, which means one character in single quotes.";
        }
        if (token.matches("-?\\d+(\\.\\d+)?")) {
            return token + " is a number value.";
        }
        return token + " is a token you can use to build the level's code.";
    }
}
