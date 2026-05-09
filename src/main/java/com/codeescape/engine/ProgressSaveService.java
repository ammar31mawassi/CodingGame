package com.codeescape.engine;

import com.codeescape.model.GameMode;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ProgressSaveService {
    static final int SCHEMA_VERSION = 1;
    static final String KEY_SCHEMA_VERSION = "schemaVersion";
    static final String KEY_GAME_MODE = "gameMode";
    static final String KEY_CURRENT_LEVEL = "currentLevelNumber";
    static final String KEY_HIGHEST_UNLOCKED_LEVEL = "highestUnlockedLevel";
    static final String KEY_BUG_COUNT = "bugCount";
    static final String KEY_TUTORIAL_SEEN = "tutorialSeen";
    static final String KEY_GAME_FINISHED = "gameFinished";

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
                    preferences.getBoolean(KEY_GAME_FINISHED, false)
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
}
