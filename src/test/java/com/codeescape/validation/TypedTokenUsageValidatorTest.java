package com.codeescape.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void acceptsDottedAccessBuiltFromSeparateTokens() {
        ValidationResult result = TypedTokenUsageValidator.validate(
                "ironChest.locked = false;",
                List.of("ironChest", ".", "locked", "=", "false", ";")
        );

        assertTrue(result.isValid());
    }

    @Test
    void resolvesTypedCodeToOwnedTokenIndexes() {
        assertEquals(
                List.of(0, 1, 2, 3, 4),
                TypedTokenUsageValidator.resolveTokenIndexes(
                        "String name = \"Ammar\";",
                        List.of("String", "name", "=", "\"Ammar\"", ";")
                ).orElseThrow()
        );
    }

    @Test
    void resolvesDuplicateTokensByConsumingSeparateIndexes() {
        assertEquals(
                List.of(0, 1, 2, 3, 4, 5, 6, 7),
                TypedTokenUsageValidator.resolveTokenIndexes(
                        "if (x == x) {}",
                        List.of("if", "(", "x", "==", "x", ")", "{", "}")
                ).orElseThrow()
        );
    }

    @Test
    void resolvesDottedTypedTokensToSeparateOwnedTokens() {
        assertEquals(
                List.of(0, 1, 2, 3, 4, 5),
                TypedTokenUsageValidator.resolveTokenIndexes(
                        "key.use();",
                        List.of("key", ".", "use", "(", ")", ";")
                ).orElseThrow()
        );
    }
}
