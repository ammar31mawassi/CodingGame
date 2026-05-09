package com.codeescape.engine;

import com.codeescape.model.GameMode;
import java.util.Optional;

public final class ProgressRun {
    private final boolean saveEnabled;
    private Optional<SavedProgress> progressBase;

    private ProgressRun(boolean saveEnabled, Optional<SavedProgress> progressBase) {
        this.saveEnabled = saveEnabled;
        this.progressBase = progressBase == null ? Optional.empty() : progressBase;
    }

    public static ProgressRun saveEnabled(Optional<SavedProgress> progressBase) {
        return new ProgressRun(true, progressBase);
    }

    public static ProgressRun unsaved() {
        return new ProgressRun(false, Optional.empty());
    }

    public boolean isSaveEnabled() {
        return saveEnabled;
    }

    public Optional<SavedProgress> getProgressBase() {
        return progressBase;
    }

    public Optional<SavedProgress> checkpointAfterLevelCompletion(
            GameMode gameMode,
            int completedLevelNumber,
            int finalLevelNumber,
            int bugCount,
            boolean tutorialSeen
    ) {
        if (!saveEnabled) {
            return Optional.empty();
        }

        SavedProgress checkpoint = ProgressPolicy.checkpointAfterLevelCompletion(
                progressBase,
                gameMode,
                completedLevelNumber,
                finalLevelNumber,
                bugCount,
                tutorialSeen
        );
        progressBase = Optional.of(checkpoint);
        return progressBase;
    }
}
