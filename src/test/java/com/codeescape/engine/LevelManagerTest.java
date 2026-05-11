package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.Level;
import com.codeescape.model.Token;
import com.codeescape.model.TokenType;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.io.TempDir;
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

    @Test
    void savedLayoutOverrideChangesPlayableLevel(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        int[][] cells = new int[RoomLayoutBuilder.GRID_ROWS][RoomLayoutBuilder.GRID_COLUMNS];
        cells[0][0] = GridRoomBuilder.TOP;
        store.save(new LevelLayoutOverride(
                1,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                cells,
                List.of(
                        new LevelLayoutOverride.ChestPlacement(2, 0, false),
                        new LevelLayoutOverride.ChestPlacement(3, 0, false)
                ),
                List.of()
        ));

        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();

        Level editedLevel = levelManager.getLevel(1);

        assertEquals(1, editedLevel.getRoom().getWalls().size());
        assertEquals(2, editedLevel.getRoom().getChests().size());
    }

    @Test
    void savedContentOverrideChangesTokensGoalHelperAndAcceptedAnswers(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        int[][] cells = new int[RoomLayoutBuilder.GRID_ROWS][RoomLayoutBuilder.GRID_COLUMNS];
        store.save(new LevelLayoutOverride(
                1,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                cells,
                List.of(new LevelLayoutOverride.ChestPlacement(2, 0, false)),
                List.of(),
                "Edited Goal",
                "Build either edited answer.",
                "Edited helper",
                List.of("int x = 7;", "int y = 8;"),
                true,
                List.of(
                        new LevelLayoutOverride.TokenPlacement(
                                "int",
                                TokenType.CODE,
                                LevelLayoutOverride.TokenPlacementKind.VISIBLE,
                                1,
                                1,
                                0
                        ),
                        new LevelLayoutOverride.TokenPlacement(
                                "Goal",
                                TokenType.GOAL,
                                LevelLayoutOverride.TokenPlacementKind.CHEST,
                                0,
                                0,
                                0
                        )
                )
        ));

        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();

        Level editedLevel = levelManager.getLevel(1);

        assertEquals("Edited Goal", editedLevel.getRoom().getPuzzle().getTitle());
        assertEquals("Build either edited answer.", editedLevel.getRoom().getPuzzle().getInstructions());
        assertEquals("Edited helper", editedLevel.getGoalHelper());
        assertEquals(List.of("int"), editedLevel.getRoom().getTokens().stream().map(Token::getValue).toList());
        assertEquals(1, editedLevel.getRoom().getChestRewards().size());
        assertTrue(editedLevel.getRoom().getPuzzle().checkAnswer("int y=8;").isValid());
        assertFalse(editedLevel.getRoom().getPuzzle().checkAnswer("int x = 5;").isValid());
    }
}
