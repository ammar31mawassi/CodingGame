package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IfElsePrintValidatorTest {
    private final IfElsePrintValidator validator = new IfElsePrintValidator();

    @Test
    void acceptsGradeGreaterThanXWithPassedFailedPrints() {
        assertTrue(validator.validate("if (grade > x) { System.out.println(\"passed\"); } else { System.out.println(\"failed\"); }").isValid());
        assertTrue(validator.validate("""
                if ( x < grade ) { System.out.println ("passed");
                } else { System.out.println ("failed");
                }
                """).isValid());
        assertTrue(validator.validate("if(grade>x) System.out.println(\"passed\"); else System.out.println(\"failed\");").isValid());
    }

    @Test
    void rejectsMissingElseOrWrongMessages() {
        assertFalse(validator.validate("if (grade > x) { System.out.println(\"passed\"); }").isValid());
        assertFalse(validator.validate("if (grade > x) { System.out.println(\"failed\"); } else { System.out.println(\"passed\"); }").isValid());
    }

    @Test
    void rejectsUsingLiteralThresholdInsteadOfX() {
        assertFalse(validator.validate("if (grade > 56) { System.out.println(\"passed\"); } else { System.out.println(\"failed\"); }").isValid());
    }
}
