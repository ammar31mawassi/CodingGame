package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaSyntaxValidatorTest {
    @Test
    void acceptsValidExpressions() {
        assertTrue(JavaSyntaxValidator.validateExpression("2 + 3 * 4").isValid());
        assertTrue(JavaSyntaxValidator.validateExpression("grade > 56 && passed").isValid());
        assertTrue(JavaSyntaxValidator.validateExpression("new Person(\"Ammar\")").isValid());
    }

    @Test
    void rejectsInvalidExpressions() {
        assertFalse(JavaSyntaxValidator.validateExpression("").isValid());
        assertFalse(JavaSyntaxValidator.validateExpression("2 +").isValid());
    }

    @Test
    void acceptsValidStatements() {
        assertTrue(JavaSyntaxValidator.validateStatement("int score = 10;").isValid());
        assertTrue(JavaSyntaxValidator.validateStatement("score = score + 1;").isValid());
        assertTrue(JavaSyntaxValidator.validateStatement("System.out.println(\"passed\");").isValid());
        assertTrue(JavaSyntaxValidator.validateStatement("if (score > 5) { score = score + 1; } else { score = 0; }").isValid());
    }

    @Test
    void rejectsInvalidStatements() {
        assertFalse(JavaSyntaxValidator.validateStatement("int score =").isValid());
        assertFalse(JavaSyntaxValidator.validateStatement("if score > 5 {}").isValid());
    }

    @Test
    void acceptsValidClassDeclarations() {
        assertTrue(JavaSyntaxValidator.validateClassDeclaration("""
                class Person {
                    String name;
                    Person(String name) { this.name = name; }
                    String getName() { return name; }
                }
                """).isValid());
    }

    @Test
    void rejectsInvalidClassDeclarations() {
        assertFalse(JavaSyntaxValidator.validateClassDeclaration("class Person {").isValid());
        assertFalse(JavaSyntaxValidator.validateClassDeclaration("class One {} class Two {}").isValid());
    }
}
