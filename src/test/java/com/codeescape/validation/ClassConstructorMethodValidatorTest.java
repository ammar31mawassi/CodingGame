package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassConstructorMethodValidatorTest {
    private final ClassConstructorMethodValidator validator = new ClassConstructorMethodValidator();

    @Test
    void acceptsPlayerClassWithConstructorAndHealMethod() {
        assertTrue(validator.validate("""
                class Player {
                    String name;
                    int health;
                    Player(String name, int health) {
                        this.name = name;
                        this.health = health;
                    }
                    void heal() {
                        health = health + 1;
                    }
                }
                """).isValid());
    }

    @Test
    void acceptsCompactIncrementMethod() {
        assertTrue(validator.validate("""
                class Player { String name; int health; Player(String name,int health){this.name=name;this.health=health;} void heal(){health++;} }
                """).isValid());
    }

    @Test
    void rejectsWrongConstructorNameOrMissingAssignments() {
        assertFalse(validator.validate("""
                class Player {
                    String name;
                    int health;
                    Hero(String name, int health) { this.name = name; this.health = health; }
                    void heal() { health = health + 1; }
                }
                """).isValid());
        assertFalse(validator.validate("""
                class Player {
                    String name;
                    int health;
                    Player(String name, int health) { this.name = name; }
                    void heal() { health = health + 1; }
                }
                """).isValid());
    }

    @Test
    void rejectsMissingHealMethod() {
        assertFalse(validator.validate("""
                class Player {
                    String name;
                    int health;
                    Player(String name, int health) { this.name = name; this.health = health; }
                }
                """).isValid());
    }
}
