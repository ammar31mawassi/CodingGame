package com.codeescape.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableThenIfValidatorTest {
    private final VariableThenIfValidator validator = new VariableThenIfValidator();

    @BeforeEach
    void resetVariables() {
        VariableDeclarationValidator.getInstance();
        VariableDeclarationValidator.resetVariables();
    }

    @Test
    void acceptsTrueIfStatementsUsingX() {
        assertTrue(validator.validate("int x = 5;\nif (x > 3) {}").isValid());
        assertTrue(validator.validate("int x = 5;\nif ( x > 3 ) { }").isValid());
        assertTrue(validator.validate("int x = 3;\nif (5 > x) {}").isValid());
        assertTrue(validator.validate("int x = 3;\nif (x == 3) {}").isValid());
        assertTrue(validator.validate("int x = 3;\nif (x != 5) {}").isValid());
    }

    @Test
    void acceptsRepeatedAttemptsWithoutDuplicateVariableFailure() {
        assertTrue(validator.validate("int x = 5;\nif (x > 3) {}").isValid());
        assertTrue(validator.validate("int x = 5;\nif (x > 3) {}").isValid());
    }

    @Test
    void rejectsWrongVariableDeclaration() {
        assertFalse(validator.validate("String x = \"ammar\";\nif (x > 3) {}").isValid());
        assertFalse(validator.validate("int name = 5;\nif (x > 3) {}").isValid());
        assertFalse(validator.validate("int x = 4.5;\nif (x > 3) {}").isValid());
    }

    @Test
    void rejectsWrongOrFalseIfStatement() {
        assertFalse(validator.validate("int x = 5;\nif (x > 6) {}").isValid());
        assertFalse(validator.validate("int x = 5;\nif (x < 3) {}").isValid());
        assertFalse(validator.validate("int x = 3;\nif (x > 3) {}").isValid());
        assertFalse(validator.validate("int x = 5;\nif (name > 3) {}").isValid());
        assertFalse(validator.validate("int x = 5;\nif (true) {}").isValid());
    }

    @Test
    void rejectsExtraTokens() {
        assertFalse(validator.validate("int x = 5;\nif (x > 3) {};").isValid());
        assertFalse(validator.validate("int x = 5;\nString name = \"ammar\";\nif (x > 3) {}").isValid());
    }
}
