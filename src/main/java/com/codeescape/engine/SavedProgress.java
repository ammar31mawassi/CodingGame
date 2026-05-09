package com.codeescape.engine;

import com.codeescape.model.GameMode;
import java.util.Objects;

public record SavedProgress(
        GameMode gameMode,
        int currentLevelNumber,
        int highestUnlockedLevel,
        int bugCount,
        boolean tutorialSeen,
        boolean gameFinished
) {
    public SavedProgress {
        Objects.requireNonNull(gameMode, "gameMode");
    }
}
