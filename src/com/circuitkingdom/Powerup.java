package com.circuitkingdom;

/** A collectible: Battery Cell, Overcharge Spark, Shield Capacitor, Jump Coil, or Energy Shard. */
public class Powerup {
    public int x, y;
    public int width = 10, height = 10;
    public int type;
    public boolean collected;
    public int bob;

    public Powerup(int type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.bob = (x * 3) % 30;
    }

    public int drawY() {
        return y + (int) (Math.sin(bob / 6.0) * 3);
    }
}
