package com.codeescape.engine;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelManagerTest {
    @Test
    void loadsEightOrderedLevels() {
        LevelManager levelManager = new LevelManager();

        levelManager.loadLevels();

        assertEquals(8, levelManager.getLevels().size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), levelManager.getLevels().stream()
                .map(level -> level.getLevelNumber())
                .toList());
    }
}
