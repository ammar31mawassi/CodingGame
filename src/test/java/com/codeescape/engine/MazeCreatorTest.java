package com.codeescape.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazeCreatorTest {
    @Test
    void createsDeterministicMazeLayout() {
        MazeCreator creator = new MazeCreator();

        MazeCreator.MazeLayout first = creator.create(6, 3, 72, 78, 170, 158, 14, 19, 404L);
        MazeCreator.MazeLayout second = creator.create(6, 3, 72, 78, 170, 158, 14, 19, 404L);

        assertEquals(first.walls().size(), second.walls().size());
        assertEquals(first.chests().size(), second.chests().size());
        assertEquals(first.walls().get(0).getX(), second.walls().get(0).getX());
        assertEquals(19, first.chests().size());
        assertFalse(first.walls().isEmpty());
        assertTrue(first.walls().stream().anyMatch(wall -> wall.getY() == 71 && wall.getHeight() == 14));
        assertTrue(first.chests().stream().allMatch(chest -> chest.getX() >= 72
                && chest.getY() >= 78
                && chest.getX() + chest.getWidth() <= 72 + 6 * 170
                && chest.getY() + chest.getHeight() <= 78 + 3 * 158));
    }
}
