package com.codeescape.model;

public class Door {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private boolean locked = true;

    public Door(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        locked = false;
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
