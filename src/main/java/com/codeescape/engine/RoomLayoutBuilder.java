package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.Door;
import com.codeescape.util.Constants;
import java.util.List;

public final class RoomLayoutBuilder {
    public static final double GRID_ORIGIN_X = 72;
    public static final double GRID_ORIGIN_Y = 78;
    public static final double GRID_CELL_WIDTH = 82;
    public static final double GRID_CELL_HEIGHT = 78;
    public static final double WALL_THICKNESS = 14;
    public static final int GRID_COLUMNS = 12;
    public static final int GRID_ROWS = 6;

    private RoomLayoutBuilder() {
    }

    public static GridRoomBuilder.GridRoomLayout gridTrainingMaze(int chestCount) {
        GridRoomBuilder.Builder builder = baseGrid();
        addMazeCorridorWalls(builder);
        addChests(builder, chestCount, 0);
        return builder.build();
    }

    public static GridRoomBuilder.GridRoomLayout gridLockedRoomMaze(int chestCount, int lockedChestCount) {
        GridRoomBuilder.Builder builder = baseGrid();
        addMazeCorridorWalls(builder);
        addLockedRoom(builder);
        addChests(builder, chestCount, Math.max(0, lockedChestCount));
        return builder.build();
    }

    public static GridRoomBuilder.GridRoomLayout gridObjectLockMaze() {
        GridRoomBuilder.Builder builder = baseGrid();
        addMazeCorridorWalls(builder);
        addLockedRoom(builder);
        return builder.build();
    }

    public static GridRoomBuilder.GridRoomLayout fromOverride(LevelLayoutOverride override) {
        GridRoomBuilder.Builder builder = baseGrid();
        int[][] cells = override.copyCells();
        for (int row = 0; row < Math.min(GRID_ROWS, override.rows()); row++) {
            for (int column = 0; column < Math.min(GRID_COLUMNS, override.columns()); column++) {
                if (cells[row][column] != 0) {
                    builder.cellFlags(column, row, cells[row][column]);
                }
            }
        }

        if (!override.questionDoors().isEmpty()) {
            LevelLayoutOverride.QuestionDoorPlacement door = override.questionDoors().get(0);
            builder.challengeDoor(door.column(), door.row(), door.side());
        }

        for (LevelLayoutOverride.ChestPlacement chest : override.chests()) {
            builder.object(
                    chest.column(),
                    chest.row(),
                    chest.lockedRoomOnly() ? GridRoomBuilder.lockedRoomChest() : GridRoomBuilder.chest()
            );
        }

        return builder.build();
    }

    public static Chest gridChest(int column, int row, double width, double height, boolean locked) {
        double centerX = GRID_ORIGIN_X + column * GRID_CELL_WIDTH + GRID_CELL_WIDTH / 2.0;
        double centerY = GRID_ORIGIN_Y + row * GRID_CELL_HEIGHT + GRID_CELL_HEIGHT / 2.0;
        return new Chest(centerX - width / 2.0, centerY - height / 2.0, width, height, locked);
    }

    private static GridRoomBuilder.Builder baseGrid() {
        return GridRoomBuilder.builder(GRID_COLUMNS, GRID_ROWS)
                .origin(GRID_ORIGIN_X, GRID_ORIGIN_Y)
                .cellSize(GRID_CELL_WIDTH, GRID_CELL_HEIGHT)
                .wallThickness(WALL_THICKNESS)
                .spawnCell(0, 0);
    }

    private static void addMazeCorridorWalls(GridRoomBuilder.Builder builder) {
        for (int row = 1; row <= 4; row++) {
            builder.wall(1, row, GridRoomBuilder.Side.RIGHT);
        }
        for (int row = 1; row <= 5; row++) {
            builder.wall(3, row, GridRoomBuilder.Side.RIGHT);
        }
        for (int column = 1; column <= 3; column++) {
            builder.wall(column, 1, GridRoomBuilder.Side.BOTTOM);
        }
        for (int row = 0; row <= 3; row++) {
            builder.wall(5, row, GridRoomBuilder.Side.RIGHT);
        }
        for (int column = 3; column <= 4; column++) {
            builder.wall(column, 3, GridRoomBuilder.Side.BOTTOM);
        }
        for (int row = 1; row <= 5; row++) {
            builder.wall(7, row, GridRoomBuilder.Side.RIGHT);
        }
        for (int column = 7; column <= 9; column++) {
            builder.wall(column, 4, GridRoomBuilder.Side.BOTTOM);
        }
    }

    private static void addLockedRoom(GridRoomBuilder.Builder builder) {
        for (int column = 9; column <= 10; column++) {
            builder.wall(column, 2, GridRoomBuilder.Side.TOP);
            builder.wall(column, 4, GridRoomBuilder.Side.BOTTOM);
        }
        builder.wall(9, 2, GridRoomBuilder.Side.LEFT);
        builder.challengeDoor(9, 3, GridRoomBuilder.Side.LEFT);
        builder.wall(9, 4, GridRoomBuilder.Side.LEFT);
        for (int row = 2; row <= 4; row++) {
            builder.wall(10, row, GridRoomBuilder.Side.RIGHT);
        }
        for (int row = 2; row <= 4; row++) {
            for (int column = 9; column <= 10; column++) {
                builder.lockedCell(column, row);
            }
        }
    }

    private static void addChests(GridRoomBuilder.Builder builder, int chestCount, int lockedChestCount) {
        List<int[]> normalCells = List.of(
                cell(2, 0), cell(4, 0), cell(6, 0), cell(8, 0), cell(11, 0),
                cell(0, 1), cell(2, 1), cell(4, 1), cell(6, 1), cell(8, 1), cell(11, 1),
                cell(0, 2), cell(3, 2), cell(4, 2), cell(6, 2), cell(8, 2), cell(11, 2),
                cell(0, 3), cell(2, 3), cell(4, 3), cell(6, 3), cell(8, 3), cell(11, 3),
                cell(0, 4), cell(2, 4), cell(4, 4), cell(6, 4), cell(8, 4), cell(11, 4),
                cell(1, 5), cell(2, 5), cell(4, 5), cell(5, 5), cell(6, 5), cell(8, 5), cell(11, 5)
        );
        List<int[]> lockedCells = List.of(
                cell(9, 2), cell(10, 2), cell(9, 3), cell(10, 3), cell(9, 4), cell(10, 4)
        );

        int lockedCount = Math.min(chestCount, Math.min(lockedChestCount, lockedCells.size()));
        int normalCount = chestCount - lockedCount;
        if (normalCount > normalCells.size()) {
            throw new IllegalArgumentException("Not enough grid cells for " + chestCount + " chests.");
        }

        for (int i = 0; i < normalCount; i++) {
            int[] cell = normalCells.get(i);
            builder.object(cell[0], cell[1], GridRoomBuilder.chest());
        }
        for (int i = 0; i < lockedCount; i++) {
            int[] cell = lockedCells.get(i);
            builder.object(cell[0], cell[1], GridRoomBuilder.lockedRoomChest());
        }
    }

    private static int[] cell(int column, int row) {
        return new int[]{column, row};
    }

    public static Door rightExitDoor() {
        return new Door(Constants.ROOM_WIDTH - 82, Constants.ROOM_HEIGHT / 2.0 - 59, 52, 118);
    }
}
