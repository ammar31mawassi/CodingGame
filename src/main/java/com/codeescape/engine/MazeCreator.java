package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.Wall;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MazeCreator {
    public MazeLayout create(
            int columns,
            int rows,
            double originX,
            double originY,
            double cellWidth,
            double cellHeight,
            double wallThickness,
            int chestCount,
            long seed
    ) {
        if (columns < 2 || rows < 2) {
            throw new IllegalArgumentException("Maze must have at least 2 columns and 2 rows.");
        }

        boolean[][] visited = new boolean[rows][columns];
        boolean[][] eastWalls = filledEastWalls(columns, rows);
        boolean[][] southWalls = filledSouthWalls(columns, rows);
        carveFrom(0, 0, visited, eastWalls, southWalls, new Random(seed));

        List<Wall> walls = createWalls(columns, rows, originX, originY, cellWidth, cellHeight, wallThickness, eastWalls, southWalls);
        List<Chest> chests = createChests(columns, rows, originX, originY, cellWidth, cellHeight, chestCount);
        return new MazeLayout(walls, chests);
    }

    private boolean[][] filledEastWalls(int columns, int rows) {
        boolean[][] walls = new boolean[rows][columns - 1];
        for (boolean[] row : walls) {
            for (int i = 0; i < row.length; i++) {
                row[i] = true;
            }
        }
        return walls;
    }

    private boolean[][] filledSouthWalls(int columns, int rows) {
        boolean[][] walls = new boolean[rows - 1][columns];
        for (boolean[] row : walls) {
            for (int i = 0; i < row.length; i++) {
                row[i] = true;
            }
        }
        return walls;
    }

    private void carveFrom(
            int column,
            int row,
            boolean[][] visited,
            boolean[][] eastWalls,
            boolean[][] southWalls,
            Random random
    ) {
        visited[row][column] = true;
        List<Direction> directions = new ArrayList<>(List.of(Direction.values()));
        Collections.shuffle(directions, random);

        for (Direction direction : directions) {
            int nextColumn = column + direction.deltaColumn;
            int nextRow = row + direction.deltaRow;
            if (nextRow < 0 || nextRow >= visited.length || nextColumn < 0 || nextColumn >= visited[0].length) {
                continue;
            }
            if (visited[nextRow][nextColumn]) {
                continue;
            }

            removeWallBetween(column, row, nextColumn, nextRow, eastWalls, southWalls);
            carveFrom(nextColumn, nextRow, visited, eastWalls, southWalls, random);
        }
    }

    private void removeWallBetween(
            int column,
            int row,
            int nextColumn,
            int nextRow,
            boolean[][] eastWalls,
            boolean[][] southWalls
    ) {
        if (nextColumn > column) {
            eastWalls[row][column] = false;
        } else if (nextColumn < column) {
            eastWalls[row][nextColumn] = false;
        } else if (nextRow > row) {
            southWalls[row][column] = false;
        } else if (nextRow < row) {
            southWalls[nextRow][column] = false;
        }
    }

    private List<Wall> createWalls(
            int columns,
            int rows,
            double originX,
            double originY,
            double cellWidth,
            double cellHeight,
            double wallThickness,
            boolean[][] eastWalls,
            boolean[][] southWalls
    ) {
        List<Wall> walls = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns - 1; column++) {
                if (eastWalls[row][column]) {
                    double x = originX + (column + 1) * cellWidth - wallThickness / 2.0;
                    double y = originY + row * cellHeight;
                    walls.add(new Wall(x, y, wallThickness, cellHeight));
                }
            }
        }
        for (int row = 0; row < rows - 1; row++) {
            for (int column = 0; column < columns; column++) {
                if (southWalls[row][column]) {
                    double x = originX + column * cellWidth;
                    double y = originY + (row + 1) * cellHeight - wallThickness / 2.0;
                    walls.add(new Wall(x, y, cellWidth, wallThickness));
                }
            }
        }
        return walls;
    }

    private List<Chest> createChests(
            int columns,
            int rows,
            double originX,
            double originY,
            double cellWidth,
            double cellHeight,
            int chestCount
    ) {
        List<Chest> chests = new ArrayList<>();
        for (int row = 0; row < rows && chests.size() < chestCount; row++) {
            for (int column = 0; column < columns && chests.size() < chestCount; column++) {
                chests.add(new Chest(
                        originX + column * cellWidth + cellWidth / 2.0 - 23,
                        originY + row * cellHeight + cellHeight / 2.0 - 17,
                        46,
                        34
                ));
            }
        }

        int extraIndex = 0;
        while (chests.size() < chestCount) {
            int column = extraIndex % columns;
            int row = extraIndex / columns;
            chests.add(new Chest(
                    originX + column * cellWidth + 18,
                    originY + rows * cellHeight + 18 + row * 46,
                    46,
                    34
            ));
            extraIndex++;
        }
        return chests;
    }

    private enum Direction {
        NORTH(0, -1),
        EAST(1, 0),
        SOUTH(0, 1),
        WEST(-1, 0);

        private final int deltaColumn;
        private final int deltaRow;

        Direction(int deltaColumn, int deltaRow) {
            this.deltaColumn = deltaColumn;
            this.deltaRow = deltaRow;
        }
    }

    public record MazeLayout(List<Wall> walls, List<Chest> chests) {
        public MazeLayout {
            walls = List.copyOf(walls);
            chests = List.copyOf(chests);
        }
    }
}
