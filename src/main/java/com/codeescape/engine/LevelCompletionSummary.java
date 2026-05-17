package com.codeescape.engine;

import java.util.List;

public record LevelCompletionSummary(
        MedalRank medalRank,
        List<AchievementId> newAchievements,
        List<NotebookEntry> newNotebookEntries,
        List<NotebookEntry> newRecoveryStamps,
        List<StageMilestoneReward> newStageRewards
) {
    public LevelCompletionSummary {
        newAchievements = List.copyOf(newAchievements);
        newNotebookEntries = List.copyOf(newNotebookEntries);
        newRecoveryStamps = List.copyOf(newRecoveryStamps);
        newStageRewards = List.copyOf(newStageRewards);
    }
}
