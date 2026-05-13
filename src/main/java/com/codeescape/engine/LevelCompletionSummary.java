package com.codeescape.engine;

import java.util.List;

public record LevelCompletionSummary(
        MedalRank medalRank,
        List<AchievementId> newAchievements,
        List<NotebookEntry> newNotebookEntries
) {
    public LevelCompletionSummary {
        newAchievements = List.copyOf(newAchievements);
        newNotebookEntries = List.copyOf(newNotebookEntries);
    }
}
