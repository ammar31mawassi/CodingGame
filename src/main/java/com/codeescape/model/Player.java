package com.codeescape.model;

public class Player {
    private double x;
    private double y;
    private final double width;
    private final double height;
    private final double speed;

    public Player(double x, double y, double width, double height, double speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void moveUp() {
        y -= speed;
    }

    public void moveDown() {
        y += speed;
    }

    public void moveLeft() {
        x -= speed;
    }

    public void moveRight() {
        x += speed;
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

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean intersects(double otherX, double otherY, double otherWidth, double otherHeight) {
        return x < otherX + otherWidth
                && x + width > otherX
                && y < otherY + otherHeight
                && y + height > otherY;
    }
}
