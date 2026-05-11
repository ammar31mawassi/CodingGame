package com.codeescape.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridRoomBuilderTest {
    @Test
    void createsWallsDoorsAndObjectsFromGridCells() {
        GridRoomBuilder.GridRoomLayout layout = GridRoomBuilder.builder(2, 2)
                .cellFlags(0, 0, GridRoomBuilder.TOP)
                .wall(0, 1, GridRoomBuilder.Side.RIGHT)
                .wall(1, 0, GridRoomBuilder.Side.BOTTOM)
                .challengeDoor(1, 0, GridRoomBuilder.Side.LEFT)
                .object(1, 0, GridRoomBuilder.lockedRoomChest())
                .build();

        assertTrue(layout.walls().size() > 1);
        assertEquals(1, layout.chests().size());
        assertNotNull(layout.challengeDoor());
    }

    @Test
    void rejectsNormalObjectsBehindWalls() {
        GridRoomBuilder.Builder builder = GridRoomBuilder.builder(2, 2)
                .wall(0, 0, GridRoomBuilder.Side.RIGHT)
                .wall(0, 1, GridRoomBuilder.Side.RIGHT)
                .object(1, 0, GridRoomBuilder.chest());

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(exception.getMessage().contains("unreachable"));
    }

    @Test
    void rejectsLockedObjectsReachableBeforeDoorOpens() {
        GridRoomBuilder.Builder builder = GridRoomBuilder.builder(2, 2)
                .object(1, 0, GridRoomBuilder.lockedRoomChest());

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(exception.getMessage().contains("reachable before the door opens"));
    }

    @Test
    void rejectsDoorsThatDoNotSeparateLockedSpace() {
        GridRoomBuilder.Builder builder = GridRoomBuilder.builder(3, 2)
                .wall(0, 0, GridRoomBuilder.Side.RIGHT)
                .wall(0, 1, GridRoomBuilder.Side.RIGHT)
                .challengeDoor(1, 0, GridRoomBuilder.Side.RIGHT);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(exception.getMessage().contains("separate reachable space"));
    }

    @Test
    void rejectsCellsTooSmallForPlayerClearance() {
        GridRoomBuilder.Builder builder = GridRoomBuilder.builder(2, 2)
                .cellSize(30, 30);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(exception.getMessage().contains("movement padding"));
    }

    @Test
    void roomLayoutBuilderCreatesValidatedLockedRoomMaze() {
        GridRoomBuilder.GridRoomLayout layout = RoomLayoutBuilder.gridLockedRoomMaze(10, 3);

        assertEquals(10, layout.chests().size());
        assertNotNull(layout.challengeDoor());
        assertTrue(layout.walls().size() > 10);
    }
}
