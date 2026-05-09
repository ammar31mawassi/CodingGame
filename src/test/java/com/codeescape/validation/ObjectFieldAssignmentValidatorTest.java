package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectFieldAssignmentValidatorTest {
    private final ObjectFieldAssignmentValidator validator = new ObjectFieldAssignmentValidator();

    @Test
    void acceptsIronChestLockedFalseAssignment() {
        assertTrue(validator.validate("ironChest.locked = false;").isValid());
        assertTrue(validator.validate("ironChest.locked=false;").isValid());
    }

    @Test
    void rejectsWrongObjectFieldOrValue() {
        assertFalse(validator.validate("goldChest.locked = false;").isValid());
        assertFalse(validator.validate("ironChest.open = false;").isValid());
        assertFalse(validator.validate("ironChest.locked = true;").isValid());
    }

    @Test
    void rejectsComparisonOrMethodCall() {
        assertFalse(validator.validate("ironChest.locked == false;").isValid());
        assertFalse(validator.validate("ironChest.unlock();").isValid());
    }
}
