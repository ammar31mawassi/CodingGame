package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.Level;
import com.codeescape.model.Token;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelManagerTest {
    @Test
    void loadsStagedOrderedLevels() {
        LevelManager levelManager = new LevelManager();

        levelManager.loadLevels();

        assertEquals(19, levelManager.getLevels().size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19), levelManager.getLevels().stream()
                .map(level -> level.getLevelNumber())
                .toList());
        assertEquals("1-1", levelManager.getLevels().get(0).getDisplayId());
        assertEquals("5-3", levelManager.getLevels().get(18).getDisplayId());
    }

    @Test
    void replayingLevelCreatesFreshUnsolvedState() {
        LevelManager levelManager = new LevelManager();
        levelManager.loadLevels();

        Level firstPlay = levelManager.goToLevel(1);
        Token token = firstPlay.getRoom().getTokens().get(0);
        token.collect();
        firstPlay.getRoom().getDoor().unlock();
        firstPlay.complete();

        Level replay = levelManager.goToLevel(1);

        assertNotSame(firstPlay, replay);
        assertFalse(replay.isCompleted());
        assertTrue(replay.getRoom().getDoor().isLocked());
        assertFalse(replay.getRoom().getTokens().get(0).isCollected());
    }

    @Test
    void catalogLevelsDoNotShareChestState() {
        LevelManager levelManager = new LevelManager();
        levelManager.loadLevels();

        Level firstCatalogLevel = levelManager.getLevel(5);
        Chest chest = firstCatalogLevel.getRoom().getChests().get(0);
        chest.open();

        Level secondCatalogLevel = levelManager.getLevel(5);

        assertNotSame(firstCatalogLevel, secondCatalogLevel);
        assertFalse(secondCatalogLevel.getRoom().getChests().get(0).isOpened());
    }
}
