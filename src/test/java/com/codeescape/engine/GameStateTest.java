package com.codeescape.engine;

import com.codeescape.model.Level;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {
    @Test
    void allLevelsStartPlayerInsideGridCellZeroZero(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        store.save(new LevelLayoutOverride(
                5,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                new int[][]{
                        {9, 1, 1, 1, 1, 3, 5, 5, 5, 3, 1, 3},
                        {8, 6, 4, 6, 0, 2, 4, 2, 0, 6, 64, 6},
                        {8, 2, 0, 2, 0, 2, 0, 6, 4, 2, 4, 2},
                        {8, 2, 0, 6, 4, 2, 4, 2, 0, 6, 0, 2},
                        {8, 2, 0, 2, 0, 0, 0, 6, 4, 4, 2, 2},
                        {12, 4, 4, 6, 4, 4, 4, 4, 4, 4, 6, 6}
                },
                List.of(),
                List.of()
        ));
        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();

        for (Level level : levelManager.getLevels()) {
            GameState gameState = new GameState();
            gameState.resetForLevel(level);

            assertEquals(
                    RoomLayoutBuilder.GRID_ORIGIN_X + (RoomLayoutBuilder.GRID_CELL_WIDTH - gameState.getPlayer().getWidth()) / 2.0,
                    gameState.getPlayer().getX(),
                    0.001,
                    level.getDisplayId() + " player x"
            );
            assertEquals(
                    RoomLayoutBuilder.GRID_ORIGIN_Y + (RoomLayoutBuilder.GRID_CELL_HEIGHT - gameState.getPlayer().getHeight()) / 2.0,
                    gameState.getPlayer().getY(),
                    0.001,
                    level.getDisplayId() + " player y"
            );
            assertFalse(new CollisionManager().hasWallCollision(gameState.getPlayer(), level.getRoom()), level.getDisplayId());
        }
    }

    @Test
    void scoutHintIsOneFreeHintPerLevel() {
        LevelManager levelManager = new LevelManager(LevelLayoutOverrideStore.disabled());
        levelManager.loadLevels();
        GameState gameState = new GameState();
        gameState.resetForLevel(levelManager.getLevel(1));

        assertTrue(gameState.canRevealScoutHintForCurrentLevel());
        assertTrue(gameState.revealScoutHint().isPresent());
        assertEquals(1, gameState.revealedHintCountForCurrentLevel());
        assertEquals(0, gameState.countedHintCountForCurrentLevel());

        assertFalse(gameState.canRevealScoutHintForCurrentLevel());
        assertTrue(gameState.revealScoutHint().isEmpty());

        assertTrue(gameState.revealNextHint().isPresent());
        assertEquals(2, gameState.revealedHintCountForCurrentLevel());
        assertEquals(1, gameState.countedHintCountForCurrentLevel());
    }
}
