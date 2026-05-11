package com.codeescape.engine;

import com.codeescape.model.GameMode;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressPolicyTest {
    private static final int FINAL_LEVEL = 19;

    @Test
    void noSaveStartsWithOnlyLevelOneUnlocked() {
        assertEquals(1, ProgressPolicy.highestSelectableLevel(Optional.empty(), FINAL_LEVEL));
    }

    @Test
    void completingLevelOneSavesLevelTwoCheckpoint() {
        SavedProgress progress = ProgressPolicy.checkpointAfterLevelCompletion(
                Optional.empty(),
                GameMode.NORMAL,
                1,
                FINAL_LEVEL,
                0,
                true
        );

        assertEquals(2, progress.currentLevelNumber());
        assertEquals(2, progress.highestUnlockedLevel());
        assertFalse(progress.gameFinished());
    }

    @Test
    void replayingEarlierLevelDoesNotRegressSavedProgress() {
        SavedProgress existing = new SavedProgress(GameMode.NORMAL, 4, 4, 1, true, false);

        SavedProgress progress = ProgressPolicy.checkpointAfterLevelCompletion(
                Optional.of(existing),
                GameMode.NORMAL,
                1,
                FINAL_LEVEL,
                0,
                true
        );

        assertEquals(4, progress.currentLevelNumber());
        assertEquals(4, progress.highestUnlockedLevel());
        assertEquals(0, progress.bugCount());
    }

    @Test
    void finalLevelCompletionMarksGameFinished() {
        SavedProgress progress = ProgressPolicy.checkpointAfterLevelCompletion(
                Optional.of(new SavedProgress(GameMode.HARD, FINAL_LEVEL, FINAL_LEVEL, 1, true, false)),
                GameMode.HARD,
                FINAL_LEVEL,
                FINAL_LEVEL,
                1,
                true
        );

        assertTrue(progress.gameFinished());
        assertEquals(FINAL_LEVEL, progress.currentLevelNumber());
        assertEquals(FINAL_LEVEL, progress.highestUnlockedLevel());
    }

    @Test
    void overwriteConfirmedRunStartsFreshWithoutMutatingExistingProgress() {
        SavedProgress oldSave = new SavedProgress(GameMode.HARD, 4, 4, 2, true, false);
        ProgressRun overwriteRun = ProgressRun.saveEnabled(Optional.empty());

        assertTrue(overwriteRun.getProgressBase().isEmpty());

        SavedProgress replacement = overwriteRun.checkpointAfterLevelCompletion(
                GameMode.NORMAL,
                1,
                FINAL_LEVEL,
                0,
                false
        ).orElseThrow();

        assertEquals(4, oldSave.currentLevelNumber());
        assertEquals(2, replacement.currentLevelNumber());
        assertEquals(GameMode.NORMAL, replacement.gameMode());
    }

    @Test
    void unsavedRunNeverProducesCheckpoint() {
        ProgressRun unsavedRun = ProgressRun.unsaved();

        assertTrue(unsavedRun.checkpointAfterLevelCompletion(
                GameMode.NORMAL,
                1,
                FINAL_LEVEL,
                0,
                true
        ).isEmpty());
        assertFalse(unsavedRun.isSaveEnabled());
    }
}
