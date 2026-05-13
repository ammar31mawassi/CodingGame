package com.codeescape.engine;

import com.codeescape.model.GameMode;
import java.util.Optional;

public final class ProgressPolicy {
    private static final int FIRST_LEVEL_NUMBER = 1;
    private static final int MAX_BUG_COUNT = 3;

    private ProgressPolicy() {
    }

    public static int highestSelectableLevel(Optional<SavedProgress> savedProgress, int finalLevelNumber) {
        int finalLevel = Math.max(FIRST_LEVEL_NUMBER, finalLevelNumber);
        if (savedProgress == null || savedProgress.isEmpty()) {
            return FIRST_LEVEL_NUMBER;
        }

        SavedProgress progress = savedProgress.get();
        if (progress.gameFinished()) {
            return finalLevel;
        }
        return clamp(progress.highestUnlockedLevel(), FIRST_LEVEL_NUMBER, finalLevel);
    }

    public static SavedProgress checkpointAfterLevelCompletion(
            Optional<SavedProgress> baseProgress,
            GameMode gameMode,
            int completedLevelNumber,
            int finalLevelNumber,
            int bugCount,
            boolean tutorialSeen
    ) {
        if (finalLevelNumber < FIRST_LEVEL_NUMBER) {
            throw new IllegalArgumentException("Final level must be at least 1.");
        }
        if (completedLevelNumber < FIRST_LEVEL_NUMBER || completedLevelNumber > finalLevelNumber) {
            throw new IllegalArgumentException("Completed level is outside the known level range.");
        }

        Optional<SavedProgress> progressBase = baseProgress == null ? Optional.empty() : baseProgress;
        int nextCheckpointLevel = Math.min(completedLevelNumber + 1, finalLevelNumber);
        int previousCurrentLevel = progressBase
                .map(SavedProgress::currentLevelNumber)
                .orElse(FIRST_LEVEL_NUMBER);
        int previousHighestLevel = progressBase
                .map(progress -> Math.max(progress.currentLevelNumber(), progress.highestUnlockedLevel()))
                .orElse(FIRST_LEVEL_NUMBER);
        boolean gameFinished = progressBase.map(SavedProgress::gameFinished).orElse(false)
                || completedLevelNumber == finalLevelNumber;

        int currentLevel = gameFinished
                ? finalLevelNumber
                : clamp(Math.max(previousCurrentLevel, nextCheckpointLevel), FIRST_LEVEL_NUMBER, finalLevelNumber);
        int highestUnlockedLevel = gameFinished
                ? finalLevelNumber
                : clamp(Math.max(previousHighestLevel, nextCheckpointLevel), FIRST_LEVEL_NUMBER, finalLevelNumber);

        return new SavedProgress(
                gameMode == null ? GameMode.NORMAL : gameMode,
                currentLevel,
                highestUnlockedLevel,
                clamp(bugCount, 0, MAX_BUG_COUNT),
                tutorialSeen,
                gameFinished,
                progressBase.map(SavedProgress::profile).orElse(PlayerProgressProfile.empty())
        );
    }

    static boolean isValid(SavedProgress progress, int finalLevelNumber) {
        if (progress == null || progress.gameMode() == null || finalLevelNumber < FIRST_LEVEL_NUMBER) {
            return false;
        }
        if (progress.currentLevelNumber() < FIRST_LEVEL_NUMBER
                || progress.currentLevelNumber() > finalLevelNumber
                || progress.highestUnlockedLevel() < progress.currentLevelNumber()
                || progress.highestUnlockedLevel() > finalLevelNumber
                || progress.bugCount() < 0
                || progress.bugCount() > MAX_BUG_COUNT) {
            return false;
        }

        return !progress.gameFinished()
                || (progress.currentLevelNumber() == finalLevelNumber
                && progress.highestUnlockedLevel() == finalLevelNumber);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }
}
