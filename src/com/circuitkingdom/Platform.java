package com.circuitkingdom;

/** A solid rectangular platform the player and enemies can stand on. */
public class Platform {
    public int x, y, width, height;

    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean intersects(int rx, int ry, int rw, int rh) {
        return rx < x + width && rx + rw > x && ry < y + height && ry + rh > y;
    }
}
