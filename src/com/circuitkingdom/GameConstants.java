package com.circuitkingdom;

/**
 * Shared constants for Circuit Kingdom.
 * Kept as plain ints (no enums) for CLDC 1.1 / old javac compatibility.
 */
public final class GameConstants {

    private GameConstants() { }

    // ---- Game states ----
    public static final int STATE_TITLE          = 0;
    public static final int STATE_STORY           = 1;
    public static final int STATE_LEVEL_INTRO      = 2;
    public static final int STATE_PLAYING          = 3;
    public static final int STATE_PAUSED           = 4;
    public static final int STATE_LEVEL_COMPLETE   = 5;
    public static final int STATE_BOSS_INTRO       = 6;
    public static final int STATE_GAME_OVER        = 7;
    public static final int STATE_WIN              = 8;

    // ---- Physics ----
    public static final int GRAVITY            = 1;
    public static final int JUMP_VELOCITY       = -11;
    public static final int DOUBLE_JUMP_VELOCITY = -9;
    public static final int MOVE_SPEED          = 3;
    public static final int MAX_FALL_SPEED      = 11;
    public static final int BOUNCE_VELOCITY     = -7; // bounce after stomping enemy/boss

    // ---- World ----
    public static final int NUM_LEVELS = 9;
    public static final int GROUND_MARGIN = 30; // px from bottom of screen used as HUD/floor buffer

    // ---- Enemy types (0-8), one signature enemy per district ----
    public static final int ENEMY_SPARKBUG   = 0; // Spark Plains - crawler
    public static final int ENEMY_MAGNETOID  = 1; // Magnet Mines - floating puller
    public static final int ENEMY_PISTONOID  = 2; // Steam Factory - jumper
    public static final int ENEMY_FROSTLING  = 3; // Ice Coolant Caves - fast slider
    public static final int ENEMY_GUSTLING   = 4; // Wind Turbine Heights - flying sine-mover
    public static final int ENEMY_EMBERIMP   = 5; // Fire Core Foundry - fireball thrower
    public static final int ENEMY_BUBBLET    = 6; // Water Reservoir - vertical floater
    public static final int ENEMY_VOIDLING   = 7; // Gravity Void - teleporter
    public static final int ENEMY_BYTESENTRY = 8; // Overlord's Core - elite guard

    // ---- Powerup types ----
    public static final int POWER_LIFE        = 0; // Battery Cell - extra life
    public static final int POWER_INVINCIBLE  = 1; // Overcharge Spark - temp invincibility + speed
    public static final int POWER_SHIELD      = 2; // Shield Capacitor - absorbs one hit
    public static final int POWER_DOUBLEJUMP  = 3; // Jump Coil - temp double jump
    public static final int POWER_SHARD       = 4; // Energy Shard - score only

    // ---- Boss types (0-8), matches level index ----
    public static final int BOSS_COILZILLA  = 0;
    public static final int BOSS_MAGNESSA   = 1;
    public static final int BOSS_PRESSURUS  = 2;
    public static final int BOSS_GLACIOR    = 3;
    public static final int BOSS_TURBOLT    = 4;
    public static final int BOSS_MAGMARAUG  = 5;
    public static final int BOSS_TIDALOR    = 6;
    public static final int BOSS_NULLGRAV   = 7;
    public static final int BOSS_GLITCHPRIME = 8;

    // ---- Timing (frames @ ~25fps) ----
    public static final int FRAME_DELAY_MS   = 40;
    public static final int INVINCIBLE_TIME  = 200; // frames of overcharge
    public static final int DOUBLEJUMP_TIME  = 250;
    public static final int HIT_INVULN_TIME  = 50;  // player invuln frames after taking damage
    public static final int BOSS_HIT_FLASH   = 20;

    public static final int STARTING_LIVES = 3;
}
