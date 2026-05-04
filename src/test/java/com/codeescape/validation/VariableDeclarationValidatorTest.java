package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableDeclarationValidatorTest {
    private final VariableDeclarationValidator validator = VariableDeclarationValidator.getInstance();

    @Test
    void acceptsValidVariableDeclarations() {
        assertTrue(validator.validate("int x = 5;").isValid());
        assertTrue(validator.validate("double price = 4.5;").isValid());
        assertTrue(validator.validate("String name = \"Ammar\";").isValid());
        assertTrue(validator.validate("char grade = 'A';").isValid());
        assertTrue(validator.validate("boolean active = true;").isValid());
    }

    @Test
    void acceptsValidNamesWithUnderscoresDollarSignsAndNumbersAfterFirstCharacter() {
        assertTrue(validator.validate("int score_1 = 10;").isValid());
        assertTrue(validator.validate("double $price = 12.99;").isValid());
        assertTrue(validator.validate("boolean isActive2 = false;").isValid());
    }

    @Test
    void rejectsInvalidVariableNames() {
        assertFalse(validator.validate("int 5 = x;").isValid());
        assertFalse(validator.validate("int 5score = 10;").isValid());
        assertFalse(validator.validate("String first-name = \"Ammar\";").isValid());
    }

    @Test
    void rejectsValuesThatDoNotMatchDeclaredType() {
        assertFalse(validator.validate("String name = 7;").isValid());
        assertFalse(validator.validate("char c = \"A\";").isValid());
        assertFalse(validator.validate("boolean active = 10;").isValid());
        assertFalse(validator.validate("double price = \"hello\";").isValid());
        assertFalse(validator.validate("int count = 4.5;").isValid());
    }

    @Test
    void rejectsMalformedDeclarations() {
        assertFalse(validator.validate("").isValid());
        assertFalse(validator.validate("int x = 5").isValid());
        assertFalse(validator.validate("x int = 5;").isValid());
        assertFalse(validator.validate("int = 5;").isValid());
        assertFalse(validator.validate("int x 5;").isValid());
    }
}
