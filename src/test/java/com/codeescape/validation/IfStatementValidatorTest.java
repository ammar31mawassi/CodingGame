package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IfStatementValidatorTest {
    private final IfStatementValidator validator = new IfStatementValidator();

    @Test
    void acceptsValidComparisonConditions() {
        assertTrue(validator.validate("if (x > 5) {}").isValid());
        assertTrue(validator.validate("if (age >= 18) {}").isValid());
        assertTrue(validator.validate("if (active == true) {}").isValid());
        assertTrue(validator.validate("if (score != 0) {}").isValid());
        assertTrue(validator.validate("if (price <= 9.99) {}").isValid());
        assertTrue(validator.validate("if (count < 10) {}").isValid());
    }

    @Test
    void acceptsDeclaredBooleanVariableAsCondition() {
        VariableDeclarationValidator variableValidator = VariableDeclarationValidator.getInstance();
        assertTrue(variableValidator.validate("boolean canOpenDoorForIfTest = true;").isValid());

        assertTrue(validator.validate("if (canOpenDoorForIfTest) {}").isValid());
    }

    @Test
    void acceptsTokenBuilderSpacing() {
        assertTrue(validator.validate("if ( x > 5 ) { }").isValid());
        assertTrue(validator.validate("if ( age >= 18 ) { }").isValid());
    }

    @Test
    void rejectsAssignmentInsideCondition() {
        assertFalse(validator.validate("if (active = true) {}").isValid());
        assertFalse(validator.validate("if (x = 5) {}").isValid());
    }

    @Test
    void rejectsIncompleteConditions() {
        assertFalse(validator.validate("if () {}").isValid());
        assertFalse(validator.validate("if (x >) {}").isValid());
        assertFalse(validator.validate("if (> 5) {}").isValid());
        assertFalse(validator.validate("if (x) {}").isValid());
    }

    @Test
    void rejectsMalformedStructureWithoutThrowing() {
        assertFalse(validator.validate("if").isValid());
        assertFalse(validator.validate("if x > 5 {}").isValid());
        assertFalse(validator.validate("if (x > 5").isValid());
        assertFalse(validator.validate("if x > 5)").isValid());
    }

    @Test
    void rejectsMissingBracesAndExtraTrailingText() {
        assertFalse(validator.validate("if (x > 5)").isValid());
        assertFalse(validator.validate("if (x > 5);").isValid());
        assertFalse(validator.validate("if (x > 5) {} extra").isValid());
    }

    @Test
    void rejectsTextThatOnlyStartsWithIf() {
        assertFalse(validator.validate("iffy (x > 5) {}").isValid());
        assertFalse(validator.validate("ifCondition (x > 5) {}").isValid());
    }
}
