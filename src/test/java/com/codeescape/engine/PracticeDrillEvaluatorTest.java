package com.codeescape.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticeDrillEvaluatorTest {
    @Test
    void acceptsSemanticIfBlockInsteadOfExactFormatting() {
        PracticePrompt prompt = PracticeLibrary.forNotebookEntry("if-block").orElseThrow();

        assertTrue(PracticeDrillEvaluator.matches(
                prompt,
                "if (score > 5){\nSystem.out.println(\"open\");\n}"
        ));
    }

    @Test
    void variableWarmupAcceptsAnyWholeNumberForKeys() {
        PracticePrompt prompt = PracticeLibrary.forNotebookEntry("variable-declaration").orElseThrow();

        assertTrue(PracticeDrillEvaluator.matches(prompt, "int keys = 5;"));
        assertTrue(PracticeDrillEvaluator.matches(prompt, "int keys = 0;"));
        assertTrue(PracticeDrillEvaluator.matches(prompt, "int keys = -1;"));
        assertFalse(PracticeDrillEvaluator.matches(prompt, "String keys = \"5\";"));
        assertFalse(PracticeDrillEvaluator.matches(prompt, "int boxes = 5;"));
    }

    @Test
    void ifDebugFixRejectsGreaterOrEqualWhenPromptRequiresGreaterThan() {
        PracticePrompt prompt = PracticeLibrary.forNotebookEntry("if-block", 1).orElseThrow();

        assertFalse(PracticeDrillEvaluator.matches(
                prompt,
                "if (score >= 7) { System.out.println(\"open\"); }"
        ));
        assertTrue(PracticeDrillEvaluator.matches(
                prompt,
                "if (score > 7) { System.out.println(\"open\"); }"
        ));
    }

    @Test
    void rejectsWrongAnswer() {
        PracticePrompt prompt = PracticeLibrary.forNotebookEntry("for-loop").orElseThrow();

        assertFalse(PracticeDrillEvaluator.matches(
                prompt,
                "while (count < 3) { count++; }"
        ));
    }

    @Test
    void acceptsDebugVariantRepairAnswer() {
        PracticePrompt prompt = PracticeLibrary.forNotebookEntry("print-statement", 1).orElseThrow();

        assertTrue(PracticeDrillEvaluator.matches(
                prompt,
                "System.out.println(\"Gate open\");"
        ));
    }
}
