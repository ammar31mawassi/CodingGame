package com.codeescape.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageCurriculumValidatorTest {
    @BeforeEach
    void resetVariables() {
        VariableDeclarationValidator.getInstance();
        VariableDeclarationValidator.resetVariables();
    }

    @Test
    void validatesPrintStatements() {
        PrintStatementValidator validator = new PrintStatementValidator("\"Hello\"");

        assertTrue(validator.validate("System.out.println(\"Hello\");").isValid());
        assertFalse(validator.validate("System.out.println(\"Hi\");").isValid());
    }

    @Test
    void validatesVariableThenPrint() {
        VariableThenPrintValidator validator = new VariableThenPrintValidator("int", "score");

        assertTrue(validator.validate("int score = 10; System.out.println(score);").isValid());
        assertFalse(validator.validate("int total = 10; System.out.println(total);").isValid());
    }

    @Test
    void validatesCharDeclaration() {
        CharDeclarationValidator validator = new CharDeclarationValidator("'A'");

        assertTrue(validator.validate("char grade = 'A';").isValid());
        assertFalse(validator.validate("String grade = \"A\";").isValid());
    }

    @Test
    void validatesSimpleMethods() {
        assertTrue(MethodDeclarationValidator.printMethod("void", "greet", "\"Hi\"")
                .validate("void greet() { System.out.println(\"Hi\"); }")
                .isValid());
        assertTrue(MethodDeclarationValidator.returnMethod("String", "getName", "\"Ammar\"")
                .validate("String getName() { return \"Ammar\"; }")
                .isValid());
        assertFalse(MethodDeclarationValidator.printMethod("void", "greet", "\"Hi\"")
                .validate("void greet(String name) { System.out.println(\"Hi\"); }")
                .isValid());
    }

    @Test
    void validatesLoops() {
        assertTrue(new WhileLoopValidator("count", "3")
                .validate("while (count < 3) { count++; }")
                .isValid());
        assertFalse(new WhileLoopValidator("count", "3")
                .validate("while (count < 3) { }")
                .isValid());

        assertTrue(new ForLoopValidator("i", "3", "i")
                .validate("for (int i = 0; i < 3; i++) { System.out.println(i); }")
                .isValid());
        assertFalse(new ForLoopValidator("i", "3", "i")
                .validate("for (int i = 1; i < 3; i++) { System.out.println(i); }")
                .isValid());
    }

    @Test
    void validatesCustomIfElsePrintConditions() {
        IfElsePrintValidator validator = new IfElsePrintValidator(
                "score",
                "pass",
                com.github.javaparser.ast.expr.BinaryExpr.Operator.GREATER_EQUALS,
                "unlocked",
                "locked"
        );

        assertTrue(validator.validate("""
                if (score >= pass) {
                    System.out.println("unlocked");
                } else {
                    System.out.println("locked");
                }
                """).isValid());
        assertTrue(validator.validate("if (pass <= score) System.out.println(\"unlocked\"); else System.out.println(\"locked\");").isValid());
        assertFalse(validator.validate("if (score > pass) System.out.println(\"unlocked\"); else System.out.println(\"locked\");").isValid());
    }
}
