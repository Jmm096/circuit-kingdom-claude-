package com.circuitkingdom;

import java.util.Vector;

/** Holds everything GameCanvas needs to run one generated level. */
public class LevelResult {
    public Vector platforms = new Vector();
    public Vector enemies = new Vector();
    public Vector powerups = new Vector();
    public Boss boss;
    public int levelLength;
    public int groundY;
    public int playerStartX;
    public int playerStartY;
    public int bossArenaStartX;
    public int goalX;
}
