package com.circuitkingdom;

/**
 * A district boss. Each bossType (0-8) has its own signature attack pattern,
 * implemented as a case in update(). Glitch Prime (type 8) combines several
 * earlier patterns and escalates as its health drops (phases).
 */
public class Boss {
    public int x, y, vx, vy;
    public int width = 34, height = 34;
    public int type;
    public int maxHealth;
    public int health;
    public boolean alive = true;
    public int hitFlash;          // frames remaining of "just got stomped" flash
    public int attackTimer;
    public int phase = 1;         // used by Glitch Prime
    public int arenaMinX, arenaMaxX;
    public int baseY;
    public boolean facingRight = false;
    public boolean gravityReversed;
    public int gravityReverseTimer;
    public String name;

    public Boss(int type, String name, int x, int y, int arenaMinX, int arenaMaxX, int health) {
        this.type = type;
        this.name = name;
        this.x = x;
        this.y = y;
        this.baseY = y;
        this.arenaMinX = arenaMinX;
        this.arenaMaxX = arenaMaxX;
        this.maxHealth = health;
        this.health = health;
        this.vx = 1;
    }

    public boolean isVulnerable() {
        return hitFlash <= 0;
    }

    /** Deal one stomp of damage. Returns true if the boss was defeated by this hit. */
    public boolean hit() {
        health--;
        hitFlash = GameConstants.BOSS_HIT_FLASH;
        if (health <= 0) {
            alive = false;
            return true;
        }
        return false;
    }

    /**
     * Advance boss AI. Returns a Projectile if the boss wants to fire one, else null.
     * playerX/Y given so bosses can aim/react.
     */
    public Projectile update(int playerX, int playerY) {
        if (hitFlash > 0) {
            hitFlash--;
        }
        attackTimer++;
        Projectile fired = null;

        // health ratio drives Glitch Prime's phase escalation
        if (type == GameConstants.BOSS_GLITCHPRIME) {
            double ratio = (double) health / (double) maxHealth;
            if (ratio > 0.66) phase = 1;
            else if (ratio > 0.33) phase = 2;
            else phase = 3;
        }

        switch (type) {
            case GameConstants.BOSS_COILZILLA: {
                // jumps in arcs toward the player periodically
                x += vx;
                if (x < arenaMinX || x > arenaMaxX) vx = -vx;
                if (attackTimer % 70 == 0) {
                    vy = -13;
                }
                break;
            }
            case GameConstants.BOSS_MAGNESSA: {
                // stays roughly central, pulses a pull field (handled by GameCanvas using x/width)
                y = baseY + (int) (Math.sin(attackTimer / 12.0) * 8);
                if (attackTimer % 100 < 50) {
                    x += (x < arenaMinX + (arenaMaxX - arenaMinX) / 2) ? 1 : -1;
                } else {
                    x += (x < arenaMinX + (arenaMaxX - arenaMinX) / 2) ? -1 : 1;
                }
                break;
            }
            case GameConstants.BOSS_PRESSURUS: {
                x += vx;
                if (x < arenaMinX || x > arenaMaxX) vx = -vx;
                // steam burst handled by GameCanvas checking attackTimer % 90 window
                break;
            }
            case GameConstants.BOSS_GLACIOR: {
                if (attackTimer % 110 == 0) {
                    vx = (x < arenaMinX + (arenaMaxX - arenaMinX) / 2) ? 6 : -6; // fast slide
                }
                x += vx;
                if (vx != 0) {
                    vx = (vx > 1) ? vx - 1 : (vx < -1 ? vx + 1 : 0);
                }
                if (x < arenaMinX) x = arenaMinX;
                if (x > arenaMaxX) x = arenaMaxX;
                if (attackTimer % 60 == 0) {
                    fired = new Projectile(x + width / 2, y + height, 0, 3, 0x66DDFF, false);
                }
                break;
            }
            case GameConstants.BOSS_TURBOLT: {
                // mostly stationary, gusts pushed via GameCanvas reading attackTimer
                y = baseY + (int) (Math.sin(attackTimer / 20.0) * 4);
                break;
            }
            case GameConstants.BOSS_MAGMARAUG: {
                x += vx;
                if (x < arenaMinX || x > arenaMaxX) vx = -vx;
                if (attackTimer % 55 == 0) {
                    int dir = (playerX < x) ? -3 : 3;
                    fired = new Projectile(x + width / 2, y + height / 2, dir, 0, 0xFF3300, false);
                }
                break;
            }
            case GameConstants.BOSS_TIDALOR: {
                y = baseY + (int) (Math.sin(attackTimer / 18.0) * 40);
                x += vx;
                if (x < arenaMinX || x > arenaMaxX) vx = -vx;
                break;
            }
            case GameConstants.BOSS_NULLGRAV: {
                x += vx;
                if (x < arenaMinX || x > arenaMaxX) vx = -vx;
                gravityReverseTimer--;
                if (gravityReverseTimer <= 0) {
                    gravityReversed = !gravityReversed;
                    gravityReverseTimer = 130;
                }
                break;
            }
            case GameConstants.BOSS_GLITCHPRIME: {
                x += vx;
                if (x < arenaMinX || x > arenaMaxX) vx = -vx;

                int jumpEvery = (phase == 1) ? 80 : (phase == 2 ? 60 : 45);
                int fireEvery = (phase == 1) ? 70 : (phase == 2 ? 50 : 35);

                if (attackTimer % jumpEvery == 0) {
                    vy = -12;
                }
                if (attackTimer % fireEvery == 0) {
                    int dir = (playerX < x) ? -3 : 3;
                    fired = new Projectile(x + width / 2, y + height / 2, dir, 0, 0xCC33FF, false);
                }
                if (phase == 3) {
                    gravityReverseTimer--;
                    if (gravityReverseTimer <= 0) {
                        gravityReversed = !gravityReversed;
                        gravityReverseTimer = 150;
                    }
                }
                break;
            }
            default:
                break;
        }
        return fired;
    }
}
