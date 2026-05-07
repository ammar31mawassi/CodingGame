package com.codeescape.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    }
}
