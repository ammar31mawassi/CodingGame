package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.Door;
import com.codeescape.model.Token;
import com.codeescape.model.TokenType;
import com.codeescape.model.Wall;
import com.codeescape.util.Constants;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public final class GridRoomBuilder {
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 4;
    public static final int LEFT = 8;
    public static final int TOP_DOOR = 16;
    public static final int RIGHT_DOOR = 32;
    public static final int BOTTOM_DOOR = 64;
    public static final int LEFT_DOOR = 128;

    private static final double DEFAULT_ORIGIN_X = 72;
    private static final double DEFAULT_ORIGIN_Y = 78;
    private static final double DEFAULT_CELL_WIDTH = 82;
    private static final double DEFAULT_CELL_HEIGHT = 78;
    private static final double DEFAULT_WALL_THICKNESS = 14;
    private static final double CHEST_WIDTH = 46;
    private static final double CHEST_HEIGHT = 34;
    private static final double TOKEN_HEIGHT = 28;
    private static final double MIN_CELL_PADDING = 24;

    private GridRoomBuilder() {
    }

    public static Builder builder(int columns, int rows) {
        return new Builder(columns, rows);
    }

    public static ObjectSpec chest() {
        return new ObjectSpec(ObjectKind.CHEST, "", TokenType.CODE, false);
    }

    public static ObjectSpec lockedRoomChest() {
        return new ObjectSpec(ObjectKind.CHEST, "", TokenType.CODE, true);
    }

    public static ObjectSpec token(String value) {
        return new ObjectSpec(ObjectKind.TOKEN, value, TokenType.CODE, false);
    }

    public static ObjectSpec specialToken(String value, TokenType tokenType) {
        return new ObjectSpec(ObjectKind.TOKEN, value, tokenType, false);
    }

    public static final class Builder {
        private final int columns;
        private final int rows;
        private final int[][] cells;
        private final Map<Cell, List<ObjectSpec>> objectsByCell = new HashMap<>();
        private final Set<Cell> lockedCells = new HashSet<>();
        private double originX = DEFAULT_ORIGIN_X;
        private double originY = DEFAULT_ORIGIN_Y;
        private double cellWidth = DEFAULT_CELL_WIDTH;
        private double cellHeight = DEFAULT_CELL_HEIGHT;
        private double wallThickness = DEFAULT_WALL_THICKNESS;
        private Cell spawnCell = new Cell(0, 0);
        private DoorEdge challengeDoorEdge;

        private Builder(int columns, int rows) {
            if (columns < 2 || rows < 2) {
                throw new IllegalArgumentException("Grid room must have at least 2 columns and 2 rows.");
            }
            this.columns = columns;
            this.rows = rows;
            this.cells = new int[rows][columns];
        }

        public Builder origin(double x, double y) {
            originX = x;
            originY = y;
            return this;
        }

        public Builder cellSize(double width, double height) {
            cellWidth = width;
            cellHeight = height;
            return this;
        }

        public Builder wallThickness(double thickness) {
            wallThickness = thickness;
            return this;
        }

        public Builder spawnCell(int column, int row) {
            spawnCell = cell(column, row);
            return this;
        }

        public Builder cellFlags(int column, int row, int flags) {
            cells[row][column] |= flags;
            return this;
        }

        public Builder wall(int column, int row, Side side) {
            return cellFlags(column, row, side.wallFlag);
        }

        public Builder challengeDoor(int column, int row, Side side) {
            Cell cell = cell(column, row);
            challengeDoorEdge = new DoorEdge(cell, side);
            cells[row][column] |= side.doorFlag;
            return this;
        }

        public Builder object(int column, int row, ObjectSpec objectSpec) {
            Cell cell = cell(column, row);
            objectsByCell.computeIfAbsent(cell, key -> new ArrayList<>()).add(objectSpec);
            if (objectSpec.lockedRoomOnly()) {
                lockedCells.add(cell);
            }
            return this;
        }

        public Builder lockedCell(int column, int row) {
            lockedCells.add(cell(column, row));
            return this;
        }

        public GridRoomLayout build() {
            validateCellSize();
            GridRoomLayout layout = createLayout();
            validateLayout(layout);
            return layout;
        }

        private GridRoomLayout createLayout() {
            List<Wall> walls = createWalls();
            List<Chest> chests = new ArrayList<>();
            List<Token> tokens = new ArrayList<>();
            for (Map.Entry<Cell, List<ObjectSpec>> entry : objectsByCell.entrySet()) {
                addCellObjects(entry.getKey(), entry.getValue(), chests, tokens);
            }
            Door challengeDoor = challengeDoorEdge == null ? null : createDoor(challengeDoorEdge.cell(), challengeDoorEdge.side());
            return new GridRoomLayout(walls, chests, tokens, challengeDoor);
        }

        private List<Wall> createWalls() {
            List<Wall> walls = new ArrayList<>();
            Set<String> wallKeys = new HashSet<>();
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    Cell current = new Cell(column, row);
                    for (Side side : Side.values()) {
                        if (hasDoor(current, side)) {
                            continue;
                        }
                        if (hasWall(current, side) && wallKeys.add(wallKey(current, side))) {
                            walls.add(createWall(current, side));
                        }
                    }
                }
            }
            return walls;
        }

        private void addCellObjects(
                Cell cell,
                List<ObjectSpec> objectSpecs,
                List<Chest> chests,
                List<Token> tokens
        ) {
            if (objectSpecs.size() > 4) {
                throw new IllegalStateException("Cell " + cell + " has too many objects.");
            }

            double[][] offsets = objectOffsets(objectSpecs.size());
            for (int i = 0; i < objectSpecs.size(); i++) {
                ObjectSpec objectSpec = objectSpecs.get(i);
                double centerX = cellCenterX(cell) + offsets[i][0];
                double centerY = cellCenterY(cell) + offsets[i][1];
                if (objectSpec.kind() == ObjectKind.CHEST) {
                    chests.add(new Chest(centerX - CHEST_WIDTH / 2.0, centerY - CHEST_HEIGHT / 2.0, CHEST_WIDTH, CHEST_HEIGHT));
                } else {
                    double tokenWidth = tokenWidth(objectSpec.value());
                    tokens.add(new Token(
                            objectSpec.value(),
                            centerX - tokenWidth / 2.0,
                            centerY - TOKEN_HEIGHT / 2.0,
                            tokenWidth,
                            TOKEN_HEIGHT,
                            objectSpec.tokenType()
                    ));
                }
            }
        }

        private void validateCellSize() {
            if (cellWidth < Constants.PLAYER_WIDTH + MIN_CELL_PADDING
                    || cellHeight < Constants.PLAYER_HEIGHT + MIN_CELL_PADDING) {
                throw new IllegalStateException("Grid cells must be wider than the player plus movement padding.");
            }
        }

        private void validateLayout(GridRoomLayout layout) {
            validateObjectsWithinGrid(layout);
            validateNoOverlaps(layout);
            Set<Cell> reachableBeforeDoors = reachableCells(false);
            Set<Cell> reachableAfterDoors = reachableCells(true);
            validateObjectsReachability(reachableBeforeDoors, reachableAfterDoors);
            validateChallengeDoorReachability(reachableBeforeDoors, reachableAfterDoors);
        }

        private void validateObjectsWithinGrid(GridRoomLayout layout) {
            double maxX = originX + columns * cellWidth;
            double maxY = originY + rows * cellHeight;
            for (Chest chest : layout.chests()) {
                if (!withinGrid(chest.getX(), chest.getY(), chest.getWidth(), chest.getHeight(), maxX, maxY)) {
                    throw new IllegalStateException("A chest is outside the grid layout.");
                }
            }
            for (Token token : layout.tokens()) {
                if (!withinGrid(token.getX(), token.getY(), token.getWidth(), token.getHeight(), maxX, maxY)) {
                    throw new IllegalStateException("A token is outside the grid layout.");
                }
            }
        }

        private void validateNoOverlaps(GridRoomLayout layout) {
            List<Rect> rects = new ArrayList<>();
            for (Chest chest : layout.chests()) {
                rects.add(new Rect(chest.getX(), chest.getY(), chest.getWidth(), chest.getHeight()));
            }
            for (Token token : layout.tokens()) {
                rects.add(new Rect(token.getX(), token.getY(), token.getWidth(), token.getHeight()));
            }
            for (int i = 0; i < rects.size(); i++) {
                for (int j = i + 1; j < rects.size(); j++) {
                    if (rects.get(i).intersects(rects.get(j))) {
                        throw new IllegalStateException("Grid objects overlap.");
                    }
                }
            }
        }

        private void validateObjectsReachability(Set<Cell> reachableBeforeDoors, Set<Cell> reachableAfterDoors) {
            for (Map.Entry<Cell, List<ObjectSpec>> entry : objectsByCell.entrySet()) {
                boolean lockedObject = entry.getValue().stream().anyMatch(ObjectSpec::lockedRoomOnly)
                        || lockedCells.contains(entry.getKey());
                if (!reachableAfterDoors.contains(entry.getKey())) {
                    throw new IllegalStateException("Object cell " + entry.getKey() + " is unreachable. Reachable cells: " + reachableAfterDoors);
                }
                if (lockedObject && reachableBeforeDoors.contains(entry.getKey())) {
                    throw new IllegalStateException("Locked-room object cell " + entry.getKey() + " is reachable before the door opens.");
                }
            }
        }

        private void validateChallengeDoorReachability(Set<Cell> reachableBeforeDoors, Set<Cell> reachableAfterDoors) {
            if (challengeDoorEdge == null) {
                return;
            }

            Cell firstSide = challengeDoorEdge.cell();
            Cell secondSide = neighbor(firstSide, challengeDoorEdge.side());
            if (!isInside(secondSide)) {
                throw new IllegalStateException("Challenge door must connect two grid cells.");
            }

            boolean firstBefore = reachableBeforeDoors.contains(firstSide);
            boolean secondBefore = reachableBeforeDoors.contains(secondSide);
            boolean firstAfter = reachableAfterDoors.contains(firstSide);
            boolean secondAfter = reachableAfterDoors.contains(secondSide);
            if (firstBefore == secondBefore) {
                throw new IllegalStateException("Challenge door must separate reachable space from a locked room.");
            }
            if (!firstAfter || !secondAfter) {
                throw new IllegalStateException("Both sides of the challenge door must be reachable after it opens.");
            }
        }

        private Set<Cell> reachableCells(boolean doorsOpen) {
            Set<Cell> visited = new HashSet<>();
            Queue<Cell> queue = new ArrayDeque<>();
            queue.add(spawnCell);
            visited.add(spawnCell);

            while (!queue.isEmpty()) {
                Cell current = queue.remove();
                for (Side side : Side.values()) {
                    Cell next = neighbor(current, side);
                    if (!isInside(next) || visited.contains(next) || edgeBlocksMovement(current, side, doorsOpen)) {
                        continue;
                    }
                    visited.add(next);
                    queue.add(next);
                }
            }
            return visited;
        }

        private boolean edgeBlocksMovement(Cell cell, Side side, boolean doorsOpen) {
            Cell next = neighbor(cell, side);
            Side opposite = side.opposite();
            boolean hasDoor = hasDoor(cell, side) || (isInside(next) && hasDoor(next, opposite));
            if (hasDoor) {
                return !doorsOpen;
            }

            return hasWall(cell, side) || (isInside(next) && hasWall(next, opposite));
        }

        private boolean hasWall(Cell cell, Side side) {
            return (cells[cell.row()][cell.column()] & side.wallFlag) != 0;
        }

        private boolean hasDoor(Cell cell, Side side) {
            return (cells[cell.row()][cell.column()] & side.doorFlag) != 0;
        }

        private Wall createWall(Cell cell, Side side) {
            double x = cellX(cell);
            double y = cellY(cell);
            return switch (side) {
                case TOP -> new Wall(x, y - wallThickness / 2.0, cellWidth, wallThickness);
                case RIGHT -> new Wall(x + cellWidth - wallThickness / 2.0, y, wallThickness, cellHeight);
                case BOTTOM -> new Wall(x, y + cellHeight - wallThickness / 2.0, cellWidth, wallThickness);
                case LEFT -> new Wall(x - wallThickness / 2.0, y, wallThickness, cellHeight);
            };
        }

        private Door createDoor(Cell cell, Side side) {
            double x = cellX(cell);
            double y = cellY(cell);
            double horizontalDoorWidth = Math.max(48, cellWidth - 24);
            double verticalDoorHeight = Math.max(48, cellHeight - 24);
            return switch (side) {
                case TOP -> new Door(
                        x + (cellWidth - horizontalDoorWidth) / 2.0,
                        y - wallThickness / 2.0,
                        horizontalDoorWidth,
                        wallThickness
                );
                case RIGHT -> new Door(
                        x + cellWidth - wallThickness / 2.0,
                        y + (cellHeight - verticalDoorHeight) / 2.0,
                        wallThickness,
                        verticalDoorHeight
                );
                case BOTTOM -> new Door(
                        x + (cellWidth - horizontalDoorWidth) / 2.0,
                        y + cellHeight - wallThickness / 2.0,
                        horizontalDoorWidth,
                        wallThickness
                );
                case LEFT -> new Door(
                        x - wallThickness / 2.0,
                        y + (cellHeight - verticalDoorHeight) / 2.0,
                        wallThickness,
                        verticalDoorHeight
                );
            };
        }

        private String wallKey(Cell cell, Side side) {
            double x = cellX(cell);
            double y = cellY(cell);
            return switch (side) {
                case TOP -> "h:" + x + ":" + y;
                case RIGHT -> "v:" + (x + cellWidth) + ":" + y;
                case BOTTOM -> "h:" + x + ":" + (y + cellHeight);
                case LEFT -> "v:" + x + ":" + y;
            };
        }

        private double[][] objectOffsets(int objectCount) {
            return switch (objectCount) {
                case 1 -> new double[][]{{0, 0}};
                case 2 -> new double[][]{{-cellWidth * 0.2, 0}, {cellWidth * 0.2, 0}};
                case 3 -> new double[][]{{-cellWidth * 0.22, -cellHeight * 0.12}, {cellWidth * 0.22, -cellHeight * 0.12}, {0, cellHeight * 0.2}};
                case 4 -> new double[][]{{-cellWidth * 0.22, -cellHeight * 0.16}, {cellWidth * 0.22, -cellHeight * 0.16}, {-cellWidth * 0.22, cellHeight * 0.18}, {cellWidth * 0.22, cellHeight * 0.18}};
                default -> new double[0][0];
            };
        }

        private boolean withinGrid(double x, double y, double width, double height, double maxX, double maxY) {
            return x >= originX
                    && y >= originY
                    && x + width <= maxX
                    && y + height <= maxY;
        }

        private Cell neighbor(Cell cell, Side side) {
            return switch (side) {
                case TOP -> new Cell(cell.column(), cell.row() - 1);
                case RIGHT -> new Cell(cell.column() + 1, cell.row());
                case BOTTOM -> new Cell(cell.column(), cell.row() + 1);
                case LEFT -> new Cell(cell.column() - 1, cell.row());
            };
        }

        private Cell cell(int column, int row) {
            Cell cell = new Cell(column, row);
            if (!isInside(cell)) {
                throw new IllegalArgumentException("Cell is outside the grid: " + cell);
            }
            return cell;
        }

        private boolean isInside(Cell cell) {
            return cell.column() >= 0 && cell.column() < columns && cell.row() >= 0 && cell.row() < rows;
        }

        private double cellX(Cell cell) {
            return originX + cell.column() * cellWidth;
        }

        private double cellY(Cell cell) {
            return originY + cell.row() * cellHeight;
        }

        private double cellCenterX(Cell cell) {
            return cellX(cell) + cellWidth / 2.0;
        }

        private double cellCenterY(Cell cell) {
            return cellY(cell) + cellHeight / 2.0;
        }

        private double tokenWidth(String value) {
            return Math.max(46, value.length() * 11 + 24);
        }
    }

    public enum Side {
        TOP(GridRoomBuilder.TOP, GridRoomBuilder.TOP_DOOR),
        RIGHT(GridRoomBuilder.RIGHT, GridRoomBuilder.RIGHT_DOOR),
        BOTTOM(GridRoomBuilder.BOTTOM, GridRoomBuilder.BOTTOM_DOOR),
        LEFT(GridRoomBuilder.LEFT, GridRoomBuilder.LEFT_DOOR);

        private final int wallFlag;
        private final int doorFlag;

        Side(int wallFlag, int doorFlag) {
            this.wallFlag = wallFlag;
            this.doorFlag = doorFlag;
        }

        private Side opposite() {
            return switch (this) {
                case TOP -> BOTTOM;
                case RIGHT -> LEFT;
                case BOTTOM -> TOP;
                case LEFT -> RIGHT;
            };
        }
    }

    public enum ObjectKind {
        CHEST,
        TOKEN
    }

    public record ObjectSpec(ObjectKind kind, String value, TokenType tokenType, boolean lockedRoomOnly) {
        public ObjectSpec {
            Objects.requireNonNull(kind, "kind");
            value = value == null ? "" : value;
            tokenType = tokenType == null ? TokenType.CODE : tokenType;
        }
    }

    public record GridRoomLayout(List<Wall> walls, List<Chest> chests, List<Token> tokens, Door challengeDoor) {
        public GridRoomLayout {
            walls = List.copyOf(walls);
            chests = List.copyOf(chests);
            tokens = List.copyOf(tokens);
        }
    }

    private record Cell(int column, int row) {
    }

    private record DoorEdge(Cell cell, Side side) {
    }

    private record Rect(double x, double y, double width, double height) {
        private boolean intersects(Rect other) {
            return x < other.x + other.width
                    && x + width > other.x
                    && y < other.y + other.height
                    && y + height > other.y;
        }
    }
}
