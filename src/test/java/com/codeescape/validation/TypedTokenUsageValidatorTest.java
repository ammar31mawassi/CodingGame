package com.codeescape.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TypedTokenUsageValidatorTest {
    @Test
    void acceptsTypedCodeBuiltFromCollectedTokens() {
        ValidationResult result = TypedTokenUsageValidator.validate(
                "String name = \"Ammar\";",
                List.of("String", "name", "=", "\"Ammar\"", ";")
        );

        assertTrue(result.isValid());
    }

    @Test
    void rejectsTypedCodeWithMissingToken() {
        ValidationResult result = TypedTokenUsageValidator.validate(
                "String name = \"Ammar\";",
                List.of("String", "age", "=", "\"Ammar\"", ";")
        );

        assertFalse(result.isValid());
    }

    @Test
    void rejectsTypedCodeThatUsesATokenTooManyTimes() {
        ValidationResult result = TypedTokenUsageValidator.validate(
                "if (x > 5) { }",
                List.of("if", "(", "x", ">", "5", ")", "{")
        );

        assertFalse(result.isValid());
    }

    @Test
    void acceptsQualifiedPrintTokenAndStringLiterals() {
        ValidationResult result = TypedTokenUsageValidator.validate(
                "System.out.println(\"passed\");",
                List.of("System.out.println", "(", "\"passed\"", ")", ";")
        );

        assertTrue(result.isValid());
    }
}
