package com.codeescape.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringDeclarationValidatorTest {
    private final StringDeclarationValidator validator = new StringDeclarationValidator("\"Ammar\"");

    @BeforeEach
    void resetVariables() {
        VariableDeclarationValidator.getInstance();
        VariableDeclarationValidator.resetVariables();
    }

    @Test
    void acceptsStringDeclarationWithAnyValidVariableNameAndRequiredValue() {
        assertTrue(validator.validate("String name = \"Ammar\";").isValid());
        assertTrue(validator.validate("String compact=\"Ammar\";").isValid());
        assertTrue(validator.validate("String age = \"Ammar\";").isValid());
        assertTrue(validator.validate("String grade = \"Ammar\";").isValid());
    }

    @Test
    void rejectsOtherValidVariableDeclarations() {
        assertFalse(validator.validate("int age = 5;").isValid());
        assertFalse(validator.validate("char grade = 'A';").isValid());
        assertFalse(validator.validate("String name = \"Other\";").isValid());
    }

    @Test
    void rejectsInvalidStringVariableNames() {
        assertFalse(validator.validate("String 5name = \"Ammar\";").isValid());
        assertFalse(validator.validate("String first-name = \"Ammar\";").isValid());
    }
}
