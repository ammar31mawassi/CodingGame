package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectCreateAndCallValidatorTest {
    private final ObjectCreateAndCallValidator validator = new ObjectCreateAndCallValidator();

    @Test
    void acceptsItemObjectCreationAndMethodCall() {
        assertTrue(validator.validate("Item key = new Item(\"key\"); key.use();").isValid());
        assertTrue(validator.validate("""
                Item item = new Item("key");
                item.use();
                """).isValid());
    }

    @Test
    void rejectsMismatchedVariableNameOrMissingCall() {
        assertFalse(validator.validate("Item key = new Item(\"key\"); item.use();").isValid());
        assertFalse(validator.validate("Item key = new Item(\"key\");").isValid());
    }

    @Test
    void rejectsWrongConstructorArgumentOrMethod() {
        assertFalse(validator.validate("Item key = new Item(\"door\"); key.use();").isValid());
        assertFalse(validator.validate("Item key = new Item(\"key\"); key.open();").isValid());
    }
}
