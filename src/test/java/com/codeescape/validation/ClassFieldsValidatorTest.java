package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassFieldsValidatorTest {
    private final ClassFieldsValidator validator = new ClassFieldsValidator();

    @Test
    void acceptsItemClassWithRequiredFieldsInAnyOrder() {
        assertTrue(validator.validate("class Item { String name; int power; }").isValid());
        assertTrue(validator.validate("""
                class Item {
                    int power;
                    String name;
                }
                """).isValid());
    }

    @Test
    void rejectsMissingOrExtraFields() {
        assertFalse(validator.validate("class Item { String name; }").isValid());
        assertFalse(validator.validate("class Item { String name; int power; boolean locked; }").isValid());
    }

    @Test
    void rejectsWrongClassNameOrFieldTypes() {
        assertFalse(validator.validate("class Player { String name; int power; }").isValid());
        assertFalse(validator.validate("class Item { String name; double power; }").isValid());
    }
}
