package com.codeescape.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public record PlayerProgressProfile(
        Set<AchievementId> achievements,
        Set<String> unlockedNotebookEntries,
        Map<Integer, MedalRank> levelMedals,
        Map<String, Integer> conceptHintUsage,
        Map<String, Integer> conceptBugCounts,
        Map<String, Integer> practiceCompletions,
        Set<String> recoveryStamps,
        Set<String> unlockedStageRewards
) {
    public PlayerProgressProfile {
        achievements = Collections.unmodifiableSet(EnumSet.copyOf(achievements == null || achievements.isEmpty()
                ? EnumSet.noneOf(AchievementId.class)
                : achievements));
        unlockedNotebookEntries = Collections.unmodifiableSet(new TreeSet<>(Objects.requireNonNullElse(unlockedNotebookEntries, Set.of())));
        levelMedals = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNullElse(levelMedals, Map.of())));
        conceptHintUsage = Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNullElse(conceptHintUsage, Map.of())));
        conceptBugCounts = Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNullElse(conceptBugCounts, Map.of())));
        practiceCompletions = Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNullElse(practiceCompletions, Map.of())));
        recoveryStamps = Collections.unmodifiableSet(new TreeSet<>(Objects.requireNonNullElse(recoveryStamps, Set.of())));
        unlockedStageRewards = Collections.unmodifiableSet(new TreeSet<>(Objects.requireNonNullElse(unlockedStageRewards, Set.of())));
    }

    public static PlayerProgressProfile empty() {
        return new PlayerProgressProfile(EnumSet.noneOf(AchievementId.class), Set.of(), Map.of(), Map.of(), Map.of(), Map.of(), Set.of(), Set.of());
    }

    public boolean hasAchievement(AchievementId achievementId) {
        return achievements.contains(achievementId);
    }

    public PlayerProgressProfile withAchievement(AchievementId achievementId) {
        EnumSet<AchievementId> updated = achievements.isEmpty()
                ? EnumSet.noneOf(AchievementId.class)
                : EnumSet.copyOf(achievements);
        updated.add(achievementId);
        return new PlayerProgressProfile(updated, unlockedNotebookEntries, levelMedals, conceptHintUsage, conceptBugCounts, practiceCompletions, recoveryStamps, unlockedStageRewards);
    }

    public PlayerProgressProfile withNotebookEntry(String entryId) {
        Set<String> updated = new TreeSet<>(unlockedNotebookEntries);
        updated.add(entryId);
        return new PlayerProgressProfile(achievements, updated, levelMedals, conceptHintUsage, conceptBugCounts, practiceCompletions, recoveryStamps, unlockedStageRewards);
    }

    public PlayerProgressProfile withNotebookEntries(Collection<String> entryIds) {
        Set<String> updated = new TreeSet<>(unlockedNotebookEntries);
        updated.addAll(entryIds);
        return new PlayerProgressProfile(achievements, updated, levelMedals, conceptHintUsage, conceptBugCounts, practiceCompletions, recoveryStamps, unlockedStageRewards);
    }

    public PlayerProgressProfile withLevelMedal(int levelNumber, MedalRank medalRank) {
        MedalRank current = levelMedals.get(levelNumber);
        if (current != null && current.ordinal() >= medalRank.ordinal()) {
            return this;
        }

        Map<Integer, MedalRank> updated = new LinkedHashMap<>(levelMedals);
        updated.put(levelNumber, medalRank);
        return new PlayerProgressProfile(achievements, unlockedNotebookEntries, updated, conceptHintUsage, conceptBugCounts, practiceCompletions, recoveryStamps, unlockedStageRewards);
    }

    public int hintUsageFor(String entryId) {
        return conceptHintUsage.getOrDefault(entryId, 0);
    }

    public int bugCountFor(String entryId) {
        return conceptBugCounts.getOrDefault(entryId, 0);
    }

    public int practiceCompletionsFor(String entryId) {
        return practiceCompletions.getOrDefault(entryId, 0);
    }

    public boolean hasRecoveryStamp(String entryId) {
        return recoveryStamps.contains(entryId);
    }

    public boolean hasStageReward(StageMilestoneReward reward) {
        return reward != null && unlockedStageRewards.contains(reward.id());
    }

    public PlayerProgressProfile withHintUsage(String entryId) {
        return new PlayerProgressProfile(
                achievements,
                unlockedNotebookEntries,
                levelMedals,
                incrementCount(conceptHintUsage, entryId),
                conceptBugCounts,
                practiceCompletions,
                recoveryStamps,
                unlockedStageRewards
        );
    }

    public PlayerProgressProfile withBugCount(String entryId) {
        return new PlayerProgressProfile(
                achievements,
                unlockedNotebookEntries,
                levelMedals,
                conceptHintUsage,
                incrementCount(conceptBugCounts, entryId),
                practiceCompletions,
                recoveryStamps,
                unlockedStageRewards
        );
    }

    public PlayerProgressProfile withPracticeCompletion(String entryId) {
        return new PlayerProgressProfile(
                achievements,
                unlockedNotebookEntries,
                levelMedals,
                conceptHintUsage,
                conceptBugCounts,
                incrementCount(practiceCompletions, entryId),
                recoveryStamps,
                unlockedStageRewards
        );
    }

    public PlayerProgressProfile withRecoveryStamp(String entryId) {
        Set<String> updated = new TreeSet<>(recoveryStamps);
        updated.add(entryId);
        return new PlayerProgressProfile(
                achievements,
                unlockedNotebookEntries,
                levelMedals,
                conceptHintUsage,
                conceptBugCounts,
                practiceCompletions,
                updated,
                unlockedStageRewards
        );
    }

    public PlayerProgressProfile withStageReward(StageMilestoneReward reward) {
        if (reward == null) {
            return this;
        }

        Set<String> updated = new TreeSet<>(unlockedStageRewards);
        updated.add(reward.id());
        return new PlayerProgressProfile(
                achievements,
                unlockedNotebookEntries,
                levelMedals,
                conceptHintUsage,
                conceptBugCounts,
                practiceCompletions,
                recoveryStamps,
                updated
        );
    }

    private Map<String, Integer> incrementCount(Map<String, Integer> source, String entryId) {
        Map<String, Integer> updated = new TreeMap<>(source);
        updated.merge(entryId, 1, Integer::sum);
        return updated;
    }
}
