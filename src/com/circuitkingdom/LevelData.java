package com.circuitkingdom;

/**
 * Data-driven definitions for the 9 districts of Circuit Kingdom.
 * LevelGenerator uses these parameters to procedurally build each
 * sprawling level and its boss arena, with difficulty ramping by index.
 */
public final class LevelData {

    public final String districtName;
    public final String bossName;
    public final int bgColor;
    public final int platformColor;
    public final int accentColor;
    public final int enemyType;
    public final int bossType;
    public final int levelLength;   // px, grows with district index
    public final int enemyCount;
    public final int gapChancePct;  // chance a segment is a gap, grows with index
    public final int bossHealth;
    public final String introText;

    private LevelData(String districtName, String bossName, int bgColor, int platformColor,
                       int accentColor, int enemyType, int bossType, int levelLength,
                       int enemyCount, int gapChancePct, int bossHealth, String introText) {
        this.districtName = districtName;
        this.bossName = bossName;
        this.bgColor = bgColor;
        this.platformColor = platformColor;
        this.accentColor = accentColor;
        this.enemyType = enemyType;
        this.bossType = bossType;
        this.levelLength = levelLength;
        this.enemyCount = enemyCount;
        this.gapChancePct = gapChancePct;
        this.bossHealth = bossHealth;
        this.introText = introText;
    }

    private static final LevelData[] LEVELS = new LevelData[] {
        new LevelData("Spark Plains", "Coilzilla",
            0x9FE6A0, 0x2E7D32, 0xFFD700,
            GameConstants.ENEMY_SPARKBUG, GameConstants.BOSS_COILZILLA,
            2400, 8, 10, 3,
            "District 1: Spark Plains\nGlitch's crawlers swarm the fields.\nCoilzilla guards the first Energy Core."),

        new LevelData("Magnet Mines", "Magnessa",
            0x6E6E8F, 0x4A4A66, 0x66CCFF,
            GameConstants.ENEMY_MAGNETOID, GameConstants.BOSS_MAGNESSA,
            2600, 9, 14, 3,
            "District 2: Magnet Mines\nFloating drones pull at your circuits.\nMagnessa waits in the deep shaft."),

        new LevelData("Steam Factory", "Pressurus",
            0x8C7A6B, 0x5A4632, 0xFF8844,
            GameConstants.ENEMY_PISTONOID, GameConstants.BOSS_PRESSURUS,
            2800, 10, 16, 4,
            "District 3: Steam Factory\nPistons hiss and hammer the floor.\nPressurus vents its fury ahead."),

        new LevelData("Ice Coolant Caves", "Glacior",
            0xCFEFFF, 0x5FA8D3, 0xFFFFFF,
            GameConstants.ENEMY_FROSTLING, GameConstants.BOSS_GLACIOR,
            2900, 10, 18, 4,
            "District 4: Ice Coolant Caves\nThe floors are slick and fast.\nGlacior slides where you least expect."),

        new LevelData("Wind Turbine Heights", "Turbolt",
            0xB8E0FF, 0x3D6E8F, 0xE0E0E0,
            GameConstants.ENEMY_GUSTLING, GameConstants.BOSS_TURBOLT,
            3000, 11, 20, 5,
            "District 5: Wind Turbine Heights\nGusts sweep flyers across the gaps.\nTurbolt commands the sky itself."),

        new LevelData("Fire Core Foundry", "Magmaraug",
            0x3A1A1A, 0x552222, 0xFF5522,
            GameConstants.ENEMY_EMBERIMP, GameConstants.BOSS_MAGMARAUG,
            3100, 12, 20, 5,
            "District 6: Fire Core Foundry\nImps hurl embers from the forges.\nMagmaraug rules the molten core."),

        new LevelData("Water Reservoir", "Tidalor",
            0x264E86, 0x1A3A66, 0x66E0FF,
            GameConstants.ENEMY_BUBBLET, GameConstants.BOSS_TIDALOR,
            3200, 12, 22, 6,
            "District 7: Water Reservoir\nThe currents rise and fall without warning.\nTidalor surges beneath the surface."),

        new LevelData("Gravity Void", "Nullgrav",
            0x1A1A2E, 0x33334D, 0xAA66FF,
            GameConstants.ENEMY_VOIDLING, GameConstants.BOSS_NULLGRAV,
            3300, 13, 24, 6,
            "District 8: Gravity Void\nNothing here falls the way it should.\nNullgrav bends the world at will."),

        new LevelData("Overlord's Core", "Glitch Prime",
            0x101018, 0x2A2A3A, 0xFF33CC,
            GameConstants.ENEMY_BYTESENTRY, GameConstants.BOSS_GLITCHPRIME,
            3000, 14, 22, 10,
            "District 9: The Reactor Core\nThis is it, Zap. Glitch is close.\nReclaim the Great Reactor for good."),
    };

    public static LevelData get(int levelIndexZeroBased) {
        return LEVELS[levelIndexZeroBased];
    }
}
