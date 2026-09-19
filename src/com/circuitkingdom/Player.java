package com.circuitkingdom;

/** Zap - the player-controlled spark robot. */
public class Player {
    public int x, y, vx, vy;
    public int width = 14, height = 18;
    public boolean onGround;
    public boolean facingRight = true;

    public int lives;
    public int score;
    public int energyShards;

    public boolean hasShield;
    public int invincibleTimer;   // Overcharge Spark
    public int doubleJumpTimer;   // Jump Coil
    public boolean doubleJumpUsed;
    public int hitTimer;          // brief invulnerability after taking damage
    public int animTimer;
    public boolean gravityFlipped; // used vs Nullgrav / Glitch Prime

    public Player() {
        lives = GameConstants.STARTING_LIVES;
        score = 0;
    }

    public boolean isInvulnerable() {
        return hitTimer > 0 || invincibleTimer > 0;
    }

    public void resetForLevel(int startX, int startY) {
        x = startX;
        y = startY;
        vx = 0;
        vy = 0;
        onGround = false;
        hasShield = false;
        invincibleTimer = 0;
        doubleJumpTimer = 0;
        doubleJumpUsed = false;
        hitTimer = 60; // brief grace period on spawn
        gravityFlipped = false;
    }

    /** Returns true if the player actually died (no shield/invincibility to absorb it). */
    public boolean takeDamage() {
        if (isInvulnerable()) {
            return false;
        }
        if (hasShield) {
            hasShield = false;
            hitTimer = GameConstants.HIT_INVULN_TIME;
            return false;
        }
        lives--;
        hitTimer = GameConstants.HIT_INVULN_TIME;
        return true;
    }
}
