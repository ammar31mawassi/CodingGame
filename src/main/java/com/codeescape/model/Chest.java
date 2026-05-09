package com.codeescape.model;

public class Chest {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private boolean opened;
    private boolean locked;

    public Chest(double x, double y, double width, double height) {
        this(x, y, width, height, false);
    }

    public Chest(double x, double y, double width, double height, boolean locked) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.locked = locked;
    }

    public boolean isOpened() {
        return opened;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        locked = false;
    }

    public void open() {
        if (!locked) {
            opened = true;
        }
    }

    public boolean intersects(Player player) {
        return player.intersects(x, y, width, height);
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
}
