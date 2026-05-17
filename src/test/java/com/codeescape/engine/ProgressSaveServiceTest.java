package com.codeescape.engine;

import com.codeescape.model.GameMode;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressSaveServiceTest {
    private static final int FINAL_LEVEL = 24;
    private Preferences preferences;
    private ProgressSaveService service;

    @BeforeEach
    void setUp() {
        preferences = Preferences.userRoot().node("com/codeescape/test/progress-" + UUID.randomUUID());
        service = new ProgressSaveService(preferences);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        preferences.removeNode();
        Preferences.userRoot().flush();
    }

    @Test
    void savesAndLoadsProgress() {
        PlayerProgressProfile profile = PlayerProgressProfile.empty()
                .withAchievement(AchievementId.CLEAN_CODER)
                .withNotebookEntry("variable-declaration")
                .withLevelMedal(4, MedalRank.SILVER)
                .withHintUsage("variable-declaration")
                .withBugCount("variable-declaration")
                .withPracticeCompletion("variable-declaration")
                .withRecoveryStamp("variable-declaration")
                .withStageReward(StageMilestoneReward.STAGE_1_CLEAR);
        SavedProgress progress = new SavedProgress(GameMode.HARD, 4, 5, 2, true, false, profile);

        service.save(progress, FINAL_LEVEL);

        Optional<SavedProgress> loaded = service.load(FINAL_LEVEL);
        assertTrue(loaded.isPresent());
        assertEquals(progress, loaded.get());
    }

    @Test
    void clearsProgress() {
        service.save(new SavedProgress(GameMode.NORMAL, 2, 2, 0, true, false, PlayerProgressProfile.empty()), FINAL_LEVEL);

        service.clear();

        assertTrue(service.load(FINAL_LEVEL).isEmpty());
    }

    @Test
    void ignoresWrongSchemaVersion() {
        writeRawProgress(GameMode.NORMAL.name(), 2, 2, 0, true, false);
        preferences.putInt(ProgressSaveService.KEY_SCHEMA_VERSION, ProgressSaveService.SCHEMA_VERSION + 1);

        assertTrue(service.load(FINAL_LEVEL).isEmpty());
    }

    @Test
    void ignoresLegacyEightLevelSchema() {
        writeRawProgress(GameMode.NORMAL.name(), 8, 8, 0, true, true);
        preferences.putInt(ProgressSaveService.KEY_SCHEMA_VERSION, 1);

        assertTrue(service.load(FINAL_LEVEL).isEmpty());
    }

    @Test
    void ignoresBadModeName() {
        writeRawProgress("JAVA", 2, 2, 0, true, false);

        assertTrue(service.load(FINAL_LEVEL).isEmpty());
    }

    @Test
    void ignoresInvalidLevelNumbers() {
        writeRawProgress(GameMode.NORMAL.name(), 0, 2, 0, true, false);
        assertTrue(service.load(FINAL_LEVEL).isEmpty());

        writeRawProgress(GameMode.NORMAL.name(), FINAL_LEVEL + 1, FINAL_LEVEL + 1, 0, true, false);
        assertTrue(service.load(FINAL_LEVEL).isEmpty());

        writeRawProgress(GameMode.NORMAL.name(), 4, 3, 0, true, false);
        assertTrue(service.load(FINAL_LEVEL).isEmpty());
    }

    @Test
    void savesAndLoadsProfileWithoutCheckpoint() {
        PlayerProgressProfile profile = PlayerProgressProfile.empty()
                .withAchievement(AchievementId.HELPER_SCOUT)
                .withNotebookEntry("if-block")
                .withLevelMedal(6, MedalRank.GOLD)
                .withHintUsage("if-block")
                .withPracticeCompletion("if-block")
                .withStageReward(StageMilestoneReward.STAGE_2_GOLD);

        service.saveProfile(profile);

        assertEquals(profile, service.loadProfile());
    }

    @Test
    void loadsFinishedLegacyCampaignAfterLevelCountIncrease() {
        writeRawProgress(GameMode.NORMAL.name(), 19, 19, 0, true, true);

        Optional<SavedProgress> loaded = service.load(FINAL_LEVEL);

        assertTrue(loaded.isPresent());
        assertTrue(loaded.get().gameFinished());
        assertEquals(19, loaded.get().currentLevelNumber());
    }

    private void writeRawProgress(
            String gameMode,
            int currentLevelNumber,
            int highestUnlockedLevel,
            int bugCount,
            boolean tutorialSeen,
            boolean gameFinished
    ) {
        preferences.putInt(ProgressSaveService.KEY_SCHEMA_VERSION, ProgressSaveService.SCHEMA_VERSION);
        preferences.put(ProgressSaveService.KEY_GAME_MODE, gameMode);
        preferences.putInt(ProgressSaveService.KEY_CURRENT_LEVEL, currentLevelNumber);
        preferences.putInt(ProgressSaveService.KEY_HIGHEST_UNLOCKED_LEVEL, highestUnlockedLevel);
        preferences.putInt(ProgressSaveService.KEY_BUG_COUNT, bugCount);
        preferences.putBoolean(ProgressSaveService.KEY_TUTORIAL_SEEN, tutorialSeen);
        preferences.putBoolean(ProgressSaveService.KEY_GAME_FINISHED, gameFinished);
    }
}
