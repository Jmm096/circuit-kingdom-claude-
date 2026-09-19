package com.circuitkingdom;

import java.util.Random;

/**
 * Builds a full, sprawling level (main path + occasional floating bonus
 * platforms + enemies + powerups + boss arena) from a LevelData definition.
 * Difficulty (gap size/frequency, enemy density, boss health) is driven by
 * the district index, so later districts are harder.
 */
public final class LevelGenerator {

    private LevelGenerator() { }

    public static LevelResult generate(int levelIndexZeroBased, int screenWidth, int screenHeight) {
        LevelData def = LevelData.get(levelIndexZeroBased);
        LevelResult result = new LevelResult();

        int groundY = screenHeight - GameConstants.GROUND_MARGIN;
        result.groundY = groundY;
        result.levelLength = def.levelLength;

        // Seed is stable per level so replays of the same district feel consistent.
        Random rng = new Random(1000 + levelIndexZeroBased * 77);

        int x = 0;
        int y = groundY - 20;
        boolean lastWasGap = true; // force a platform first

        // Safe starting platform
        Platform start = new Platform(0, groundY, 120, 20);
        result.platforms.addElement(start);
        result.playerStartX = 20;
        result.playerStartY = groundY - 40;
        x = 120;
        y = groundY;

        int maxGapWidth = 55 + Math.min(levelIndexZeroBased, 6) * 6; // grows slightly, stays jumpable
        int minSeg = 70;
        int maxSeg = 130;

        while (x < def.levelLength - 200) {
            int segWidth = minSeg + rng.nextInt(maxSeg - minSeg);
            boolean isGap = (!lastWasGap) && (rng.nextInt(100) < def.gapChancePct);

            if (isGap) {
                int gapWidth = 40 + rng.nextInt(Math.max(1, maxGapWidth - 40));
                x += gapWidth;
                lastWasGap = true;
                continue;
            }

            // Random-walk the platform height, but keep steps jumpable/climbable.
            int step = -40 + rng.nextInt(81); // -40..+40
            y += step;
            if (y > groundY) y = groundY;
            if (y < groundY - 110) y = groundY - 110;

            Platform p = new Platform(x, y, segWidth, 16);
            result.platforms.addElement(p);

            // Chance of an enemy riding this platform.
            if (result.enemies.size() < def.enemyCount && rng.nextInt(100) < 55) {
                int margin = 10;
                int patrolMin = x + margin;
                int patrolMax = x + segWidth - margin - 12;
                int ex = (patrolMax > patrolMin) ? patrolMin + rng.nextInt(patrolMax - patrolMin + 1) : x + margin;
                Enemy e = new Enemy(def.enemyType, ex, y - 16, patrolMin, Math.max(patrolMin, patrolMax));
                result.enemies.addElement(e);
            }

            // Occasional floating bonus platform above the path (adds verticality/sprawl).
            if (rng.nextInt(100) < 22) {
                int fx = x + rng.nextInt(Math.max(1, segWidth));
                int fy = y - 55 - rng.nextInt(35);
                int fw = 40 + rng.nextInt(40);
                Platform floater = new Platform(fx, fy, fw, 14);
                result.platforms.addElement(floater);
                if (rng.nextInt(100) < 60) {
                    result.powerups.addElement(makePowerup(rng, fx + fw / 2 - 5, fy - 14));
                }
            }

            // Chance of a powerup/shard sitting on the main platform.
            if (rng.nextInt(100) < 35) {
                int px = x + 10 + rng.nextInt(Math.max(1, segWidth - 20));
                result.powerups.addElement(makePowerup(rng, px, y - 14));
            }

            x += segWidth;
            lastWasGap = false;
        }

        // Guarantee at least one life powerup per district so runs stay winnable.
        boolean hasLife = false;
        for (int i = 0; i < result.powerups.size(); i++) {
            Powerup pu = (Powerup) result.powerups.elementAt(i);
            if (pu.type == GameConstants.POWER_LIFE) { hasLife = true; break; }
        }
        if (!hasLife && result.platforms.size() > 2) {
            Platform mid = (Platform) result.platforms.elementAt(result.platforms.size() / 2);
            result.powerups.addElement(new Powerup(GameConstants.POWER_LIFE, mid.x + mid.width / 2, mid.y - 14));
        }

        // ---- Boss arena: a wide flat platform at the end of the district ----
        int arenaStart = def.levelLength;
        int arenaWidth = 320;
        Platform arena = new Platform(arenaStart, groundY, arenaWidth, 20);
        result.platforms.addElement(arena);
        result.bossArenaStartX = arenaStart;
        result.goalX = arenaStart + arenaWidth - 20;

        Boss boss = new Boss(def.bossType, def.bossName,
                arenaStart + arenaWidth - 60, groundY - 40,
                arenaStart + 20, arenaStart + arenaWidth - 40,
                def.bossHealth);
        result.boss = boss;

        return result;
    }

    private static Powerup makePowerup(Random rng, int x, int y) {
        int roll = rng.nextInt(100);
        int type;
        if (roll < 10) type = GameConstants.POWER_LIFE;
        else if (roll < 30) type = GameConstants.POWER_INVINCIBLE;
        else if (roll < 50) type = GameConstants.POWER_SHIELD;
        else if (roll < 65) type = GameConstants.POWER_DOUBLEJUMP;
        else type = GameConstants.POWER_SHARD;
        return new Powerup(type, x, y);
    }
}
