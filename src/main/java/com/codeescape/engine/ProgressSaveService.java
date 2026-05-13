package com.codeescape.engine;

import com.codeescape.model.GameMode;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ProgressSaveService {
    static final int SCHEMA_VERSION = 3;
    static final String KEY_SCHEMA_VERSION = "schemaVersion";
    static final String KEY_GAME_MODE = "gameMode";
    static final String KEY_CURRENT_LEVEL = "currentLevelNumber";
    static final String KEY_HIGHEST_UNLOCKED_LEVEL = "highestUnlockedLevel";
    static final String KEY_BUG_COUNT = "bugCount";
    static final String KEY_TUTORIAL_SEEN = "tutorialSeen";
    static final String KEY_GAME_FINISHED = "gameFinished";
    static final String KEY_ACHIEVEMENTS = "achievements";
    static final String KEY_NOTEBOOK_ENTRIES = "notebookEntries";
    static final String KEY_LEVEL_MEDALS = "levelMedals";

    private final Preferences preferences;

    public ProgressSaveService() {
        this(Preferences.userNodeForPackage(ProgressSaveService.class));
    }

    public ProgressSaveService(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
    }

    public Optional<SavedProgress> load(int finalLevelNumber) {
        if (finalLevelNumber < 1) {
            return Optional.empty();
        }

        try {
            if (preferences.getInt(KEY_SCHEMA_VERSION, -1) != SCHEMA_VERSION) {
                return Optional.empty();
            }

            SavedProgress progress = new SavedProgress(
                    GameMode.valueOf(preferences.get(KEY_GAME_MODE, "")),
                    preferences.getInt(KEY_CURRENT_LEVEL, -1),
                    preferences.getInt(KEY_HIGHEST_UNLOCKED_LEVEL, -1),
                    preferences.getInt(KEY_BUG_COUNT, -1),
                    preferences.getBoolean(KEY_TUTORIAL_SEEN, false),
                    preferences.getBoolean(KEY_GAME_FINISHED, false),
                    loadProfile()
            );

            if (!ProgressPolicy.isValid(progress, finalLevelNumber)) {
                return Optional.empty();
            }
            return Optional.of(progress);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return Optional.empty();
        }
    }

    public void save(SavedProgress progress, int finalLevelNumber) {
        if (!ProgressPolicy.isValid(progress, finalLevelNumber)) {
            throw new IllegalArgumentException("Cannot save invalid progress.");
        }

        preferences.putInt(KEY_SCHEMA_VERSION, SCHEMA_VERSION);
        preferences.put(KEY_GAME_MODE, progress.gameMode().name());
        preferences.putInt(KEY_CURRENT_LEVEL, progress.currentLevelNumber());
        preferences.putInt(KEY_HIGHEST_UNLOCKED_LEVEL, progress.highestUnlockedLevel());
        preferences.putInt(KEY_BUG_COUNT, progress.bugCount());
        preferences.putBoolean(KEY_TUTORIAL_SEEN, progress.tutorialSeen());
        preferences.putBoolean(KEY_GAME_FINISHED, progress.gameFinished());
        saveProfile(progress.profile());
        flush();
    }

    public PlayerProgressProfile loadProfile() {
        try {
            return new PlayerProgressProfile(
                    parseAchievements(preferences.get(KEY_ACHIEVEMENTS, "")),
                    parseNotebookEntries(preferences.get(KEY_NOTEBOOK_ENTRIES, "")),
                    parseLevelMedals(preferences.get(KEY_LEVEL_MEDALS, ""))
            );
        } catch (IllegalArgumentException exception) {
            return PlayerProgressProfile.empty();
        }
    }

    public void saveProfile(PlayerProgressProfile profile) {
        PlayerProgressProfile effectiveProfile = profile == null ? PlayerProgressProfile.empty() : profile;
        preferences.put(KEY_ACHIEVEMENTS, serializeAchievements(effectiveProfile));
        preferences.put(KEY_NOTEBOOK_ENTRIES, serializeNotebookEntries(effectiveProfile));
        preferences.put(KEY_LEVEL_MEDALS, serializeLevelMedals(effectiveProfile));
        flush();
    }

    public void clear() {
        try {
            preferences.clear();
            flush();
        } catch (BackingStoreException exception) {
            throw new IllegalStateException("Unable to clear saved progress.", exception);
        }
    }

    private void flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException exception) {
            throw new IllegalStateException("Unable to write saved progress.", exception);
        }
    }

    private String serializeAchievements(PlayerProgressProfile profile) {
        return profile.achievements().stream()
                .map(Enum::name)
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private String serializeNotebookEntries(PlayerProgressProfile profile) {
        return String.join(",", profile.unlockedNotebookEntries());
    }

    private String serializeLevelMedals(PlayerProgressProfile profile) {
        return profile.levelMedals().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ":" + entry.getValue().name())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private java.util.Set<AchievementId> parseAchievements(String encoded) {
        java.util.EnumSet<AchievementId> achievements = java.util.EnumSet.noneOf(AchievementId.class);
        if (encoded == null || encoded.isBlank()) {
            return achievements;
        }

        for (String token : encoded.split(",")) {
            if (!token.isBlank()) {
                achievements.add(AchievementId.valueOf(token.trim()));
            }
        }
        return achievements;
    }

    private java.util.Set<String> parseNotebookEntries(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return java.util.Set.of();
        }

        java.util.Set<String> entries = new java.util.TreeSet<>();
        for (String token : encoded.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isBlank()) {
                entries.add(trimmed);
            }
        }
        return entries;
    }

    private java.util.Map<Integer, MedalRank> parseLevelMedals(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return java.util.Map.of();
        }

        java.util.Map<Integer, MedalRank> medals = new java.util.LinkedHashMap<>();
        for (String token : encoded.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            String[] parts = trimmed.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid level medal value.");
            }
            medals.put(Integer.parseInt(parts[0]), MedalRank.valueOf(parts[1]));
        }
        return medals;
    }
}
