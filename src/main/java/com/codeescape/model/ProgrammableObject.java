package com.codeescape.model;

import java.util.Objects;

public class ProgrammableObject {
    private final String id;
    private final String displayName;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Puzzle puzzle;
    private final Chest targetChest;
    private boolean activated;

    public ProgrammableObject(
            String id,
            String displayName,
            double x,
            double y,
            double width,
            double height,
            Puzzle puzzle,
            Chest targetChest
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.puzzle = Objects.requireNonNull(puzzle, "puzzle");
        this.targetChest = targetChest;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public boolean isActivated() {
        return activated;
    }

    public void activate() {
        activated = true;
        if (targetChest != null) {
            targetChest.unlock();
        }
    }

    public boolean intersects(Player player) {
        return player.intersects(x, y, width, height);
    }
}
