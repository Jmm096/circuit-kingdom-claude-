package com.circuitkingdom;

/** A simple straight/falling projectile fired by Emberimp enemies, Glacior, Magmaraug, Glitch Prime. */
public class Projectile {
    public int x, y, vx, vy;
    public int width = 6, height = 6;
    public boolean alive = true;
    public int colorRGB;
    public boolean fallsWithGravity;

    public Projectile(int x, int y, int vx, int vy, int colorRGB, boolean fallsWithGravity) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.colorRGB = colorRGB;
        this.fallsWithGravity = fallsWithGravity;
    }

    public void update() {
        x += vx;
        y += vy;
        if (fallsWithGravity) {
            vy += 1;
        }
    }
}
