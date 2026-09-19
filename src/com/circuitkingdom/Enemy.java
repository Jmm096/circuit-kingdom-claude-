package com.circuitkingdom;

/**
 * A regular (non-boss) enemy. Behavior is chosen by 'type' inside update().
 * Kept as one flexible class (rather than a subclass per enemy) to stay
 * lightweight, in the spirit of old J2ME memory budgets.
 */
public class Enemy {
    public int x, y, vx, vy;
    public int width = 14, height = 14;
    public int type;
    public boolean alive = true;
    public int patrolMinX, patrolMaxX;
    public boolean facingRight = false;
    public int animTimer;
    public int specialTimer; // used for teleport / fire cooldown / sine wave phase
    public int baseY;        // used for bob/sine movement
    public int health = 1;

    public Enemy(int type, int x, int y, int patrolMinX, int patrolMaxX) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.baseY = y;
        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;
        this.specialTimer = (x * 7) % 60; // stagger timers so enemies don't sync

        switch (type) {
            case GameConstants.ENEMY_SPARKBUG:
                vx = 1; width = 12; height = 10;
                break;
            case GameConstants.ENEMY_MAGNETOID:
                vx = 0; width = 16; height = 16;
                break;
            case GameConstants.ENEMY_PISTONOID:
                vx = 1; width = 14; height = 16;
                break;
            case GameConstants.ENEMY_FROSTLING:
                vx = 3; width = 14; height = 12;
                break;
            case GameConstants.ENEMY_GUSTLING:
                vx = 2; width = 16; height = 12;
                break;
            case GameConstants.ENEMY_EMBERIMP:
                vx = 1; width = 13; height = 15;
                break;
            case GameConstants.ENEMY_BUBBLET:
                vx = 0; width = 14; height = 14;
                break;
            case GameConstants.ENEMY_VOIDLING:
                vx = 0; width = 14; height = 16;
                break;
            case GameConstants.ENEMY_BYTESENTRY:
                vx = 1; width = 16; height = 18; health = 2;
                break;
            default:
                vx = 1;
        }
    }

    /**
     * Update this enemy's position/AI. Returns a Projectile if this enemy
     * wants to fire one this frame, otherwise null.
     */
    public Projectile update(int playerX, int playerY, int groundY) {
        animTimer++;
        Projectile fired = null;

        switch (type) {
            case GameConstants.ENEMY_SPARKBUG: // simple ground patrol
            case GameConstants.ENEMY_PISTONOID: {
                x += vx;
                if (type == GameConstants.ENEMY_PISTONOID) {
                    // hop up and down rhythmically
                    y = baseY - Math.abs((animTimer % 40) - 20) / 2;
                }
                if (x < patrolMinX || x > patrolMaxX) {
                    vx = -vx;
                }
                facingRight = vx > 0;
                break;
            }
            case GameConstants.ENEMY_MAGNETOID: {
                // hovers, gently pulls toward player's x when in range
                y = baseY + (int) (Math.sin(animTimer / 8.0) * 6);
                int dx = playerX - x;
                if (Math.abs(dx) < 60) {
                    x += (dx > 0) ? 1 : -1;
                }
                if (x < patrolMinX) x = patrolMinX;
                if (x > patrolMaxX) x = patrolMaxX;
                break;
            }
            case GameConstants.ENEMY_FROSTLING: {
                x += vx;
                if (x < patrolMinX || x > patrolMaxX) {
                    vx = -vx;
                }
                facingRight = vx > 0;
                break;
            }
            case GameConstants.ENEMY_GUSTLING: {
                x += vx;
                y = baseY + (int) (Math.sin(animTimer / 10.0) * 14);
                if (x < patrolMinX || x > patrolMaxX) {
                    vx = -vx;
                }
                facingRight = vx > 0;
                break;
            }
            case GameConstants.ENEMY_EMBERIMP: {
                x += vx;
                if (x < patrolMinX || x > patrolMaxX) {
                    vx = -vx;
                }
                facingRight = vx > 0;
                specialTimer--;
                if (specialTimer <= 0 && Math.abs(playerX - x) < 90) {
                    specialTimer = 90;
                    int dir = (playerX < x) ? -2 : 2;
                    fired = new Projectile(x + width / 2, y, dir, 0, 0xFF5522, false);
                }
                break;
            }
            case GameConstants.ENEMY_BUBBLET: {
                y = baseY + (int) (Math.sin(animTimer / 14.0) * 20);
                break;
            }
            case GameConstants.ENEMY_VOIDLING: {
                specialTimer--;
                if (specialTimer <= 0) {
                    specialTimer = 70;
                    // teleport within patrol range
                    int range = patrolMaxX - patrolMinX;
                    if (range > 0) {
                        x = patrolMinX + ((animTimer * 37) % range);
                    }
                }
                break;
            }
            case GameConstants.ENEMY_BYTESENTRY: {
                x += vx;
                if (x < patrolMinX || x > patrolMaxX) {
                    vx = -vx;
                }
                facingRight = vx > 0;
                break;
            }
            default:
                break;
        }
        return fired;
    }
}
