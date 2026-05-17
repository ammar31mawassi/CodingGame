package com.codeescape.engine;

import com.codeescape.model.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticeLibraryTest {
    @Test
    void everyNotebookEntryHasPracticePrompt() {
        for (NotebookEntry entry : NotebookLibrary.allEntries()) {
            assertTrue(PracticeLibrary.forNotebookEntry(entry.id()).isPresent(), entry.id());
        }
    }

    @Test
    void finalLevelUsesLatestUnlockedPracticePattern() {
        LevelManager levelManager = new LevelManager();
        levelManager.loadLevels();

        Level finalLevel = levelManager.getLevel(24);
        PracticePrompt prompt = PracticeLibrary.forLevel(finalLevel).orElseThrow();

        assertEquals("object-call", prompt.notebookEntryId());
    }

    @Test
    void earlyConditionLevelMapsToIfBlockPractice() {
        LevelManager levelManager = new LevelManager();
        levelManager.loadLevels();

        Level level = levelManager.getLevel(4);
        PracticePrompt prompt = PracticeLibrary.forLevel(level).orElseThrow();

        assertEquals("if-block", prompt.notebookEntryId());
    }

    @Test
    void laterVariableLevelKeepsVariablePracticeMapping() {
        LevelManager levelManager = new LevelManager();
        levelManager.loadLevels();

        Level level = levelManager.getLevel(5);
        PracticePrompt prompt = PracticeLibrary.forLevel(level).orElseThrow();

        assertEquals("variable-declaration", prompt.notebookEntryId());
    }

    @Test
    void notebookPracticeCyclesIntoDebugVariantAfterFirstCompletion() {
        PracticePrompt prompt = PracticeLibrary.forNotebookEntry("if-block", 1).orElseThrow();

        assertEquals("If Debug Fix", prompt.title());
        assertEquals("if-block", prompt.notebookEntryId());
    }
}
