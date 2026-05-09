package com.codeescape.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassDeclarationValidatorTest {
    private final ClassDeclarationValidator validator = new ClassDeclarationValidator();

    @Test
    void acceptsEmptyClassDeclarations() {
        assertTrue(validator.validate("class Person {}").isValid());
        assertTrue(validator.validate("class Student { }").isValid());
        assertTrue(validator.validate("class CodeEscape {}").isValid());
    }

    @Test
    void acceptsClassDeclarationsWithFieldsOnly() {
        assertTrue(validator.validate("class Student { String name; }").isValid());
        assertTrue(validator.validate("class Book { String title=\"Code\"; int pages=120; }").isValid());
        assertTrue(validator.validate("class Person { int age = 20; boolean active = true; }").isValid());
        assertTrue(validator.validate("""
                class Product {
                    String name = "Book";
                    double price = 4.5;
                    char grade = 'A';
                }
                """).isValid());
    }

    @Test
    void acceptsConstructorsAndSimpleMethods() {
        assertTrue(validator.validate("""
                class Person {
                    String name;
                    Person(String name) { this.name = name; }
                    String getName() { return name; }
                    void rename(String name) { this.name = name; }
                }
                """).isValid());
    }

    @Test
    void rejectsInvalidClassNames() {
        assertFalse(validator.validate("class {}").isValid());
        assertFalse(validator.validate("Person class {}").isValid());
        assertFalse(validator.validate("class 123Person {}").isValid());
        assertFalse(validator.validate("class person {}").isValid());
    }

    @Test
    void rejectsMalformedClassDeclarations() {
        assertFalse(validator.validate("").isValid());
        assertFalse(validator.validate("class Person").isValid());
        assertFalse(validator.validate("class Person {").isValid());
        assertFalse(validator.validate("class Person }").isValid());
        assertFalse(validator.validate("class Person {} extra").isValid());
    }

    @Test
    void rejectsInvalidFields() {
        assertFalse(validator.validate("class Person { String name = 7; }").isValid());
        assertFalse(validator.validate("class Person { char grade = \"A\"; }").isValid());
        assertFalse(validator.validate("class Person { boolean active = 10; }").isValid());
        assertFalse(validator.validate("class Person { int 5age = 20; }").isValid());
    }

    @Test
    void rejectsUnsupportedMembersAndBodylessMethods() {
        assertFalse(validator.validate("class Person { if (age > 18) {} }").isValid());
        assertFalse(validator.validate("class Person { int age = 20; void run(); }").isValid());
        assertFalse(validator.validate("class Person { class Nested {} }").isValid());
    }

    @Test
    void rejectsDuplicateFieldNames() {
        assertFalse(validator.validate("class Person { String name; int name = 5; }").isValid());
        assertFalse(validator.validate("class Person { int age=20; double age=4.5; }").isValid());
    }

    @Test
    void rejectsMultipleTopLevelClasses() {
        assertFalse(validator.validate("class Person {} class Student {}").isValid());
    }
}
