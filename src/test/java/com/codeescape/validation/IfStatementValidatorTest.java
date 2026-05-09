package com.codeescape.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IfStatementValidatorTest {
    private final IfStatementValidator validator = new IfStatementValidator();

    @BeforeEach
    void resetVariables() {
        VariableDeclarationValidator.getInstance();
        VariableDeclarationValidator.resetVariables();
    }

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
    void acceptsBooleanLiteralAsCondition() {
        assertTrue(validator.validate("if (true) {}").isValid());
        assertTrue(validator.validate("if ( true ) { }").isValid());
    }

    @Test
    void acceptsTokenBuilderSpacing() {
        assertTrue(validator.validate("if ( x > 5 ) { }").isValid());
        assertTrue(validator.validate("if ( age >= 18 ) { }").isValid());
    }

    @Test
    void acceptsCompactTypedSpacing() {
        assertTrue(validator.validate("if(x>5){}").isValid());
        assertTrue(validator.validate("if(active==true){}").isValid());
    }

    @Test
    void acceptsSimpleBlockContentsAndLogicalConditions() {
        VariableDeclarationValidator variableValidator = VariableDeclarationValidator.getInstance();
        assertTrue(variableValidator.validate("boolean ready = true;").isValid());

        assertTrue(validator.validate("if (x > 5) { System.out.println(\"ok\"); }").isValid());
        assertTrue(validator.validate("if (ready && score > 0) { score = score + 1; }").isValid());
    }

    @Test
    void rejectsAssignmentInsideCondition() {
        assertFalse(validator.validate("if (active = true) {}").isValid());
        assertFalse(validator.validate("if (x = 5) {}").isValid());
    }

    @Test
    void rejectsElseBranchForIfOnlyPuzzle() {
        assertFalse(validator.validate("if (x > 5) {} else {}").isValid());
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
