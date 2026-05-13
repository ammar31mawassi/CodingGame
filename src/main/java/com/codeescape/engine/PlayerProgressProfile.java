package com.codeescape.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record PlayerProgressProfile(
        Set<AchievementId> achievements,
        Set<String> unlockedNotebookEntries,
        Map<Integer, MedalRank> levelMedals
) {
    public PlayerProgressProfile {
        achievements = Collections.unmodifiableSet(EnumSet.copyOf(achievements == null || achievements.isEmpty()
                ? EnumSet.noneOf(AchievementId.class)
                : achievements));
        unlockedNotebookEntries = Collections.unmodifiableSet(new TreeSet<>(Objects.requireNonNullElse(unlockedNotebookEntries, Set.of())));
        levelMedals = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNullElse(levelMedals, Map.of())));
    }

    public static PlayerProgressProfile empty() {
        return new PlayerProgressProfile(EnumSet.noneOf(AchievementId.class), Set.of(), Map.of());
    }

    public boolean hasAchievement(AchievementId achievementId) {
        return achievements.contains(achievementId);
    }

    public PlayerProgressProfile withAchievement(AchievementId achievementId) {
        EnumSet<AchievementId> updated = achievements.isEmpty()
                ? EnumSet.noneOf(AchievementId.class)
                : EnumSet.copyOf(achievements);
        updated.add(achievementId);
        return new PlayerProgressProfile(updated, unlockedNotebookEntries, levelMedals);
    }

    public PlayerProgressProfile withNotebookEntry(String entryId) {
        Set<String> updated = new TreeSet<>(unlockedNotebookEntries);
        updated.add(entryId);
        return new PlayerProgressProfile(achievements, updated, levelMedals);
    }

    public PlayerProgressProfile withNotebookEntries(Collection<String> entryIds) {
        Set<String> updated = new TreeSet<>(unlockedNotebookEntries);
        updated.addAll(entryIds);
        return new PlayerProgressProfile(achievements, updated, levelMedals);
    }

    public PlayerProgressProfile withLevelMedal(int levelNumber, MedalRank medalRank) {
        MedalRank current = levelMedals.get(levelNumber);
        if (current != null && current.ordinal() >= medalRank.ordinal()) {
            return this;
        }

        Map<Integer, MedalRank> updated = new LinkedHashMap<>(levelMedals);
        updated.put(levelNumber, medalRank);
        return new PlayerProgressProfile(achievements, unlockedNotebookEntries, updated);
    }
}
