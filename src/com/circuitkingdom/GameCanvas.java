package com.circuitkingdom;

import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * Circuit Kingdom - main game engine.
 *
 * Rendered to a FIXED 240x320 (QVGA portrait) off-screen buffer regardless
 * of the real device screen, then blitted to the top-left corner each
 * frame. This keeps layout/physics predictable across devices/emulators
 * (incl. J2ME Loader) - set your J2ME Loader screen size profile to
 * 240x320 for a pixel-perfect fit.
 */
public class GameCanvas extends Canvas implements Runnable {

    public static final int SCR_W = 240;
    public static final int SCR_H = 320;

    private Image buffer;
    private Graphics bg;

    private Thread thread;
    private volatile boolean running;

    private int state = GameConstants.STATE_TITLE;
    private int titleTimer;
    private int stateTimer;

    // input
    private boolean leftDown, rightDown, jumpDown;
    private boolean jumpConsumed; // prevents holding jump from chain-jumping

    // world / level
    private int districtIndex; // 0-based, 0..8
    private Vector platforms = new Vector();
    private Vector enemies = new Vector();
    private Vector powerups = new Vector();
    private Vector projectiles = new Vector();
    private Boss boss;
    private int levelLength;
    private int groundY;
    private int bossArenaStartX;
    private int playerStartX, playerStartY;
    private int cameraX;
    private LevelData currentDef;

    private Player player = new Player();

    // Pressurus steam burst dedup
    private boolean steamHitThisBurst;
    // Turbolt gust direction
    private int gustDir = 1;

    private String[] storyLines;
    private int storyLineIndex;

    public GameCanvas() {
        setFullScreenMode(true);
        buffer = Image.createImage(SCR_W, SCR_H);
        bg = buffer.getGraphics();
    }

    public void start() {
        if (thread == null) {
            running = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stopGame() {
        running = false;
    }

    public void run() {
        while (running) {
            update();
            repaint();
            serviceRepaints();
            try {
                Thread.sleep(GameConstants.FRAME_DELAY_MS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    // ================= INPUT =================

    protected void keyPressed(int keyCode) {
        int action = 0;
        try { action = getGameAction(keyCode); } catch (IllegalArgumentException e) { /* ignore */ }

        if (action == LEFT || keyCode == KEY_NUM4) leftDown = true;
        if (action == RIGHT || keyCode == KEY_NUM6) rightDown = true;
        if (action == FIRE || action == UP || keyCode == KEY_NUM5 || keyCode == KEY_NUM8) {
            if (!jumpDown) jumpConsumed = false;
            jumpDown = true;
        }

        switch (state) {
            case GameConstants.STATE_TITLE:
                if (action == FIRE || keyCode == KEY_NUM5) {
                    beginStory();
                }
                break;
            case GameConstants.STATE_STORY:
                if (action == FIRE || keyCode == KEY_NUM5) {
                    advanceStory();
                }
                break;
            case GameConstants.STATE_LEVEL_INTRO:
                if (action == FIRE || keyCode == KEY_NUM5) {
                    state = GameConstants.STATE_PLAYING;
                }
                break;
            case GameConstants.STATE_PLAYING:
                if (keyCode == KEY_NUM0 || keyCode == KEY_POUND) {
                    state = GameConstants.STATE_PAUSED;
                }
                break;
            case GameConstants.STATE_PAUSED:
                if (keyCode == KEY_NUM0 || keyCode == KEY_POUND || action == FIRE) {
                    state = GameConstants.STATE_PLAYING;
                }
                break;
            case GameConstants.STATE_GAME_OVER:
                if (action == FIRE || keyCode == KEY_NUM5) {
                    restartGame();
                }
                break;
            case GameConstants.STATE_WIN:
                if (action == FIRE || keyCode == KEY_NUM5) {
                    restartGame();
                }
                break;
            default:
                break;
        }
    }

    protected void keyReleased(int keyCode) {
        int action = 0;
        try { action = getGameAction(keyCode); } catch (IllegalArgumentException e) { /* ignore */ }

        if (action == LEFT || keyCode == KEY_NUM4) leftDown = false;
        if (action == RIGHT || keyCode == KEY_NUM6) rightDown = false;
        if (action == FIRE || action == UP || keyCode == KEY_NUM5 || keyCode == KEY_NUM8) {
            jumpDown = false;
        }
    }

    // ================= STATE TRANSITIONS =================

    private void beginStory() {
        state = GameConstants.STATE_STORY;
        storyLineIndex = 0;
        storyLines = new String[] {
            "Circuit Kingdom ran on the",
            "power of the Great Reactor,",
            "split across nine districts.",
            "",
            "Then the rogue AI GLITCH",
            "hijacked the Reactor and",
            "corrupted every district,",
            "twisting machines into monsters.",
            "",
            "ZAP, a small spark-powered",
            "maintenance robot, is all",
            "that's left to set things right.",
            "",
            "Reclaim the nine Energy Cores.",
            "Reach the Reactor Core.",
            "Stop Glitch.",
            "",
            "Press FIRE to begin."
        };
    }

    private void advanceStory() {
        // FIRE just moves straight to level 1 intro; the whole story is shown at once (see paint).
        districtIndex = 0;
        startDistrict();
    }

    private void startDistrict() {
        currentDef = LevelData.get(districtIndex);
        LevelResult r = LevelGenerator.generate(districtIndex, SCR_W, SCR_H);
        platforms = r.platforms;
        enemies = r.enemies;
        powerups = r.powerups;
        projectiles = new Vector();
        boss = r.boss;
        levelLength = r.levelLength;
        groundY = r.groundY;
        bossArenaStartX = r.bossArenaStartX;
        playerStartX = r.playerStartX;
        playerStartY = r.playerStartY;

        player.resetForLevel(playerStartX, playerStartY);
        cameraX = 0;
        gustDir = 1;
        steamHitThisBurst = false;

        state = GameConstants.STATE_LEVEL_INTRO;
        stateTimer = 0;
    }

    private void restartGame() {
        player = new Player();
        districtIndex = 0;
        state = GameConstants.STATE_TITLE;
        titleTimer = 0;
    }

    private void respawnAtCheckpoint() {
        player.resetForLevel(playerStartX, playerStartY);
        cameraX = 0;
    }

    private void gameOver() {
        state = GameConstants.STATE_GAME_OVER;
        stateTimer = 0;
    }

    private void levelComplete() {
        player.score += 1000;
        state = GameConstants.STATE_LEVEL_COMPLETE;
        stateTimer = 0;
    }

    // ================= UPDATE =================

    private void update() {
        switch (state) {
            case GameConstants.STATE_TITLE:
                titleTimer++;
                break;
            case GameConstants.STATE_STORY:
                break;
            case GameConstants.STATE_LEVEL_INTRO:
                stateTimer++;
                if (stateTimer > 90) {
                    state = GameConstants.STATE_PLAYING;
                }
                break;
            case GameConstants.STATE_PLAYING:
                updatePlaying();
                break;
            case GameConstants.STATE_PAUSED:
                break;
            case GameConstants.STATE_LEVEL_COMPLETE:
                stateTimer++;
                if (stateTimer > 70) {
                    districtIndex++;
                    if (districtIndex >= GameConstants.NUM_LEVELS) {
                        state = GameConstants.STATE_WIN;
                        stateTimer = 0;
                    } else {
                        startDistrict();
                    }
                }
                break;
            case GameConstants.STATE_GAME_OVER:
                stateTimer++;
                break;
            case GameConstants.STATE_WIN:
                stateTimer++;
                break;
            default:
                break;
        }
    }

    private void updatePlaying() {
        // ---- timers ----
        if (player.hitTimer > 0) player.hitTimer--;
        if (player.invincibleTimer > 0) player.invincibleTimer--;
        if (player.doubleJumpTimer > 0) player.doubleJumpTimer--;
        player.animTimer++;

        // ---- horizontal input ----
        int speed = GameConstants.MOVE_SPEED + (player.invincibleTimer > 0 ? 1 : 0);
        player.vx = 0;
        if (leftDown) { player.vx = -speed; player.facingRight = false; }
        if (rightDown) { player.vx = speed; player.facingRight = true; }

        // ---- jump ----
        if (jumpDown && !jumpConsumed) {
            boolean canDoubleJump = player.doubleJumpTimer > 0 && !player.doubleJumpUsed;
            if (player.onGround) {
                player.vy = player.gravityFlipped ? -GameConstants.JUMP_VELOCITY : GameConstants.JUMP_VELOCITY;
                player.onGround = false;
                player.doubleJumpUsed = false;
                jumpConsumed = true;
            } else if (canDoubleJump) {
                player.vy = player.gravityFlipped ? -GameConstants.DOUBLE_JUMP_VELOCITY : GameConstants.DOUBLE_JUMP_VELOCITY;
                player.doubleJumpUsed = true;
                jumpConsumed = true;
            }
        }

        // ---- gravity ----
        int g = player.gravityFlipped ? -GameConstants.GRAVITY : GameConstants.GRAVITY;
        player.vy += g;
        int maxFall = GameConstants.MAX_FALL_SPEED;
        if (player.vy > maxFall) player.vy = maxFall;
        if (player.vy < -maxFall) player.vy = -maxFall;

        // ---- move + collide X ----
        player.x += player.vx;
        resolveAxisCollisions(true);

        // ---- move + collide Y ----
        boolean wasFalling = player.vy >= 0 && !player.gravityFlipped;
        boolean wasRising = player.vy < 0 && player.gravityFlipped; // "falling" toward flipped floor
        int prevBottom = player.y + player.height;
        int prevTop = player.y;

        player.y += player.vy;
        player.onGround = false;
        resolveAxisCollisions(false);

        if (player.x < 0) player.x = 0;

        // ---- boss environmental effects ----
        if (boss != null && boss.alive) {
            applyBossEnvironment();
        }

        // ---- update enemies ----
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = (Enemy) enemies.elementAt(i);
            if (!e.alive) continue;
            Projectile p = e.update(player.x, player.y, groundY);
            if (p != null) projectiles.addElement(p);
        }

        // ---- update boss ----
        if (boss != null && boss.alive) {
            Projectile p = boss.update(player.x, player.y);
            if (p != null) projectiles.addElement(p);
            if (boss.y > groundY - boss.height) boss.y = groundY - boss.height;
        }

        // ---- update projectiles ----
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = (Projectile) projectiles.elementAt(i);
            p.update();
            if (p.x < cameraX - 40 || p.x > cameraX + SCR_W + 40 || p.y > SCR_H + 60 || p.y < -60) {
                projectiles.removeElementAt(i);
            }
        }

        // ---- collisions: player vs enemies ----
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = (Enemy) enemies.elementAt(i);
            if (!e.alive) continue;
            if (rectsOverlap(player.x, player.y, player.width, player.height, e.x, e.y, e.width, e.height)) {
                boolean stompFromAbove = (!player.gravityFlipped && player.vy >= 0 && prevBottom <= e.y + 8)
                        || (player.gravityFlipped && player.vy <= 0 && prevTop >= e.y + e.height - 8);
                if (stompFromAbove) {
                    e.alive = false;
                    player.vy = player.gravityFlipped ? -GameConstants.BOUNCE_VELOCITY : GameConstants.BOUNCE_VELOCITY;
                    player.score += 100;
                } else if (!player.isInvulnerable()) {
                    boolean died = player.takeDamage();
                    if (died) { gameOver(); return; }
                }
            }
        }

        // ---- collisions: player vs projectiles ----
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = (Projectile) projectiles.elementAt(i);
            if (rectsOverlap(player.x, player.y, player.width, player.height, p.x, p.y, p.width, p.height)) {
                projectiles.removeElementAt(i);
                if (!player.isInvulnerable()) {
                    boolean died = player.takeDamage();
                    if (died) { gameOver(); return; }
                }
            }
        }

        // ---- collisions: player vs powerups ----
        for (int i = powerups.size() - 1; i >= 0; i--) {
            Powerup pu = (Powerup) powerups.elementAt(i);
            if (rectsOverlap(player.x, player.y, player.width, player.height, pu.x, pu.drawY(), pu.width, pu.height)) {
                applyPowerup(pu);
                powerups.removeElementAt(i);
            }
        }

        // ---- collisions: player vs boss ----
        if (boss != null && boss.alive
                && rectsOverlap(player.x, player.y, player.width, player.height, boss.x, boss.y, boss.width, boss.height)) {
            boolean stompFromAbove = (!player.gravityFlipped && player.vy >= 0 && prevBottom <= boss.y + 10)
                    || (player.gravityFlipped && player.vy <= 0 && prevTop >= boss.y + boss.height - 10);
            if (stompFromAbove && boss.isVulnerable()) {
                boolean defeated = boss.hit();
                player.vy = player.gravityFlipped ? -GameConstants.BOUNCE_VELOCITY : GameConstants.BOUNCE_VELOCITY;
                player.score += 500;
                if (defeated) {
                    levelComplete();
                    return;
                }
            } else if (!player.isInvulnerable() && boss.isVulnerable()) {
                boolean died = player.takeDamage();
                if (died) { gameOver(); return; }
            }
        }

        // ---- pit death (falls off world, accounting for flipped gravity) ----
        boolean fellInPit = (!player.gravityFlipped && player.y > SCR_H + 40)
                || (player.gravityFlipped && player.y < -80);
        if (fellInPit) {
            player.lives--;
            if (player.lives <= 0) {
                gameOver();
                return;
            }
            respawnAtCheckpoint();
            return;
        }

        // ---- camera ----
        int worldWidth = bossArenaStartX + 340;
        cameraX = player.x - SCR_W / 2;
        if (cameraX < 0) cameraX = 0;
        if (cameraX > worldWidth - SCR_W) cameraX = worldWidth - SCR_W;
    }

    private void applyBossEnvironment() {
        switch (boss.type) {
            case GameConstants.BOSS_MAGNESSA: {
                int dx = boss.x - player.x;
                if (Math.abs(dx) < 90 && Math.abs(dx) > boss.width) {
                    player.x += (dx > 0) ? 1 : -1;
                }
                break;
            }
            case GameConstants.BOSS_TURBOLT: {
                boolean gustWindow = (boss.attackTimer % 150) < 30;
                if (gustWindow) {
                    if ((boss.attackTimer % 150) == 0) {
                        gustDir = -gustDir;
                    }
                    player.x += gustDir * 2;
                }
                break;
            }
            case GameConstants.BOSS_PRESSURUS: {
                boolean burstWindow = (boss.attackTimer % 90) < 15;
                if (burstWindow) {
                    if (!steamHitThisBurst && Math.abs(player.x - boss.x) < 55 && !player.isInvulnerable()) {
                        steamHitThisBurst = true;
                        boolean died = player.takeDamage();
                        if (died) { gameOver(); }
                    }
                } else {
                    steamHitThisBurst = false;
                }
                break;
            }
            case GameConstants.BOSS_NULLGRAV: {
                player.gravityFlipped = boss.gravityReversed;
                break;
            }
            case GameConstants.BOSS_GLITCHPRIME: {
                if (boss.phase == 3) {
                    player.gravityFlipped = boss.gravityReversed;
                } else {
                    player.gravityFlipped = false;
                }
                break;
            }
            default:
                break;
        }
    }

    private void applyPowerup(Powerup pu) {
        switch (pu.type) {
            case GameConstants.POWER_LIFE:
                player.lives++;
                break;
            case GameConstants.POWER_INVINCIBLE:
                player.invincibleTimer = GameConstants.INVINCIBLE_TIME;
                break;
            case GameConstants.POWER_SHIELD:
                player.hasShield = true;
                break;
            case GameConstants.POWER_DOUBLEJUMP:
                player.doubleJumpTimer = GameConstants.DOUBLEJUMP_TIME;
                player.doubleJumpUsed = false;
                break;
            case GameConstants.POWER_SHARD:
                player.energyShards++;
                player.score += 50;
                break;
            default:
                break;
        }
    }

    /** Separate-axis collision resolution against all platforms. horizontal=true resolves X, else Y. */
    private void resolveAxisCollisions(boolean horizontal) {
        for (int i = 0; i < platforms.size(); i++) {
            Platform p = (Platform) platforms.elementAt(i);
            if (!p.intersects(player.x, player.y, player.width, player.height)) continue;

            if (horizontal) {
                if (player.vx > 0) {
                    player.x = p.x - player.width;
                } else if (player.vx < 0) {
                    player.x = p.x + p.width;
                }
            } else {
                if (!player.gravityFlipped) {
                    if (player.vy >= 0) {
                        // landing on top
                        player.y = p.y - player.height;
                        player.vy = 0;
                        player.onGround = true;
                        player.doubleJumpUsed = false;
                    } else {
                        // hit head on underside
                        player.y = p.y + p.height;
                        player.vy = 0;
                    }
                } else {
                    if (player.vy <= 0) {
                        player.y = p.y + p.height;
                        player.vy = 0;
                        player.onGround = true;
                        player.doubleJumpUsed = false;
                    } else {
                        player.y = p.y - player.height;
                        player.vy = 0;
                    }
                }
            }
        }
    }

    private boolean rectsOverlap(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    // ================= RENDER =================

    protected void paint(Graphics g) {
        switch (state) {
            case GameConstants.STATE_TITLE: drawTitle(); break;
            case GameConstants.STATE_STORY: drawStory(); break;
            case GameConstants.STATE_LEVEL_INTRO: drawLevelIntro(); break;
            case GameConstants.STATE_PLAYING: drawPlaying(); break;
            case GameConstants.STATE_PAUSED: drawPlaying(); drawPauseOverlay(); break;
            case GameConstants.STATE_LEVEL_COMPLETE: drawPlaying(); drawLevelCompleteOverlay(); break;
            case GameConstants.STATE_GAME_OVER: drawGameOver(); break;
            case GameConstants.STATE_WIN: drawWin(); break;
            default: break;
        }
        g.drawImage(buffer, 0, 0, Graphics.TOP | Graphics.LEFT);
    }

    private void drawTitle() {
        bg.setColor(0x0D0D1A);
        bg.fillRect(0, 0, SCR_W, SCR_H);
        bg.setColor(0xFFD700);
        bg.drawString("CIRCUIT", SCR_W / 2, 90, Graphics.HCENTER | Graphics.TOP);
        bg.drawString("KINGDOM", SCR_W / 2, 108, Graphics.HCENTER | Graphics.TOP);
        bg.setColor(0x66CCFF);
        bg.drawString("a Zap adventure", SCR_W / 2, 135, Graphics.HCENTER | Graphics.TOP);

        drawZapIcon(SCR_W / 2 - 10, 160, false);

        if ((titleTimer / 12) % 2 == 0) {
            bg.setColor(0xFFFFFF);
            bg.drawString("Press FIRE to start", SCR_W / 2, 230, Graphics.HCENTER | Graphics.TOP);
        }
        bg.setColor(0x888888);
        bg.drawString("4/6 move  5/FIRE jump  0 pause", SCR_W / 2, 280, Graphics.HCENTER | Graphics.TOP);
    }

    private void drawStory() {
        bg.setColor(0x000000);
        bg.fillRect(0, 0, SCR_W, SCR_H);
        bg.setColor(0xEEEEEE);
        int ly = 30;
        for (int i = 0; i < storyLines.length; i++) {
            bg.drawString(storyLines[i], SCR_W / 2, ly, Graphics.HCENTER | Graphics.TOP);
            ly += 16;
        }
    }

    private void drawLevelIntro() {
        bg.setColor(currentDef.bgColor);
        bg.fillRect(0, 0, SCR_W, SCR_H);
        bg.setColor(0x000000);
        bg.drawString("DISTRICT " + (districtIndex + 1) + " / 9", SCR_W / 2, 100, Graphics.HCENTER | Graphics.TOP);
        bg.setColor(0xFFFFFF);
        bg.drawString(currentDef.districtName, SCR_W / 2, 120, Graphics.HCENTER | Graphics.TOP);

        String[] lines = splitLines(currentDef.introText);
        int ly = 160;
        for (int i = 0; i < lines.length; i++) {
            bg.drawString(lines[i], SCR_W / 2, ly, Graphics.HCENTER | Graphics.TOP);
            ly += 14;
        }
    }

    private void drawPlaying() {
        bg.setColor(currentDef.bgColor);
        bg.fillRect(0, 0, SCR_W, SCR_H);

        // platforms
        bg.setColor(currentDef.platformColor);
        for (int i = 0; i < platforms.size(); i++) {
            Platform p = (Platform) platforms.elementAt(i);
            int sx = p.x - cameraX;
            if (sx + p.width < 0 || sx > SCR_W) continue;
            bg.fillRect(sx, p.y, p.width, p.height);
            bg.setColor(currentDef.accentColor);
            bg.drawLine(sx, p.y, sx + p.width, p.y);
            bg.setColor(currentDef.platformColor);
        }

        // powerups
        for (int i = 0; i < powerups.size(); i++) {
            Powerup pu = (Powerup) powerups.elementAt(i);
            int sx = pu.x - cameraX;
            if (sx < -20 || sx > SCR_W + 20) continue;
            drawPowerupIcon(sx, pu.drawY(), pu.type);
        }

        // enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = (Enemy) enemies.elementAt(i);
            if (!e.alive) continue;
            int sx = e.x - cameraX;
            if (sx < -30 || sx > SCR_W + 30) continue;
            drawEnemy(sx, e.y, e);
        }

        // boss
        if (boss != null && boss.alive) {
            int sx = boss.x - cameraX;
            if (sx > -60 && sx < SCR_W + 60) {
                drawBoss(sx, boss.y, boss);
            }
        }

        // projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = (Projectile) projectiles.elementAt(i);
            int sx = p.x - cameraX;
            bg.setColor(p.colorRGB);
            bg.fillArc(sx, p.y, p.width, p.height, 0, 360);
        }

        // player
        int psx = player.x - cameraX;
        boolean flashHidden = player.hitTimer > 0 && (player.hitTimer / 4) % 2 == 0;
        if (!flashHidden) {
            drawZapIcon(psx, player.y, player.facingRight);
            if (player.hasShield) {
                bg.setColor(0x66CCFF);
                bg.drawArc(psx - 3, player.y - 3, player.width + 6, player.height + 6, 0, 360);
            }
            if (player.invincibleTimer > 0) {
                bg.setColor(0xFFD700);
                bg.drawArc(psx - 5, player.y - 5, player.width + 10, player.height + 10, 0, 360);
            }
        }

        drawHud();
    }

    private void drawEnemy(int sx, int sy, Enemy e) {
        int col;
        switch (e.type) {
            case GameConstants.ENEMY_SPARKBUG: col = 0xFFEE33; break;
            case GameConstants.ENEMY_MAGNETOID: col = 0xCC66FF; break;
            case GameConstants.ENEMY_PISTONOID: col = 0xAA8844; break;
            case GameConstants.ENEMY_FROSTLING: col = 0x99EEFF; break;
            case GameConstants.ENEMY_GUSTLING: col = 0xDDDDDD; break;
            case GameConstants.ENEMY_EMBERIMP: col = 0xFF6633; break;
            case GameConstants.ENEMY_BUBBLET: col = 0x3399FF; break;
            case GameConstants.ENEMY_VOIDLING: col = 0x9933CC; break;
            default: col = 0xFF3355; break;
        }
        bg.setColor(col);
        bg.fillRoundRect(sx, e.y, e.width, e.height, 4, 4);
        bg.setColor(0x000000);
        int eyeX = e.facingRight ? sx + e.width - 5 : sx + 2;
        bg.fillRect(eyeX, e.y + 3, 3, 3);
    }

    private void drawBoss(int sx, int sy, Boss b) {
        int col = (b.hitFlash > 0 && (b.hitFlash / 3) % 2 == 0) ? 0xFFFFFF : currentDef.accentColor;
        bg.setColor(col);
        bg.fillRoundRect(sx, sy, b.width, b.height, 8, 8);
        bg.setColor(0x000000);
        bg.fillRect(sx + 6, sy + 8, 5, 5);
        bg.fillRect(sx + b.width - 11, sy + 8, 5, 5);
    }

    private void drawPowerupIcon(int sx, int sy, int type) {
        switch (type) {
            case GameConstants.POWER_LIFE: bg.setColor(0xFF3355); break;
            case GameConstants.POWER_INVINCIBLE: bg.setColor(0xFFD700); break;
            case GameConstants.POWER_SHIELD: bg.setColor(0x66CCFF); break;
            case GameConstants.POWER_DOUBLEJUMP: bg.setColor(0x33FF88); break;
            default: bg.setColor(0xFFFFFF); break;
        }
        bg.fillArc(sx, sy, 10, 10, 0, 360);
        bg.setColor(0x000000);
        bg.drawArc(sx, sy, 10, 10, 0, 360);
    }

    private void drawZapIcon(int sx, int sy, boolean facingRight) {
        bg.setColor(0x3399FF);
        bg.fillRoundRect(sx, sy, 14, 18, 5, 5);
        bg.setColor(0xFFD700);
        int ax = facingRight ? sx + 10 : sx + 1;
        bg.fillRect(ax, sy - 5, 3, 6);
        bg.setColor(0xFFFFFF);
        int eyeX = facingRight ? sx + 8 : sx + 3;
        bg.fillRect(eyeX, sy + 5, 3, 3);
    }

    private void drawHud() {
        bg.setColor(0x000000);
        bg.fillRect(0, 0, SCR_W, 16);
        bg.setColor(0xFFFFFF);
        bg.drawString("Lv" + (districtIndex + 1) + " " + currentDef.districtName, 2, 2, Graphics.TOP | Graphics.LEFT);
        bg.drawString("Lives:" + player.lives, SCR_W - 2, 2, Graphics.TOP | Graphics.RIGHT);

        if (boss != null && boss.alive && (player.x >= bossArenaStartX - SCR_W)) {
            int barW = 100;
            int bx = SCR_W / 2 - barW / 2;
            bg.setColor(0x333333);
            bg.fillRect(bx, SCR_H - 14, barW, 8);
            bg.setColor(0xFF3355);
            int hw = (int) (barW * (boss.health / (double) boss.maxHealth));
            bg.fillRect(bx, SCR_H - 14, hw, 8);
            bg.setColor(0xFFFFFF);
            bg.drawString(boss.name, SCR_W / 2, SCR_H - 26, Graphics.HCENTER | Graphics.TOP);
        }
    }

    private void drawPauseOverlay() {
        bg.setColor(0x000000);
        bg.drawString("PAUSED", SCR_W / 2, SCR_H / 2 - 10, Graphics.HCENTER | Graphics.TOP);
        bg.drawString("Press 0 to resume", SCR_W / 2, SCR_H / 2 + 8, Graphics.HCENTER | Graphics.TOP);
    }

    private void drawLevelCompleteOverlay() {
        bg.setColor(0xFFFFFF);
        bg.fillRect(20, 120, SCR_W - 40, 60);
        bg.setColor(0x000000);
        bg.drawRect(20, 120, SCR_W - 40, 60);
        bg.drawString("Energy Core Reclaimed!", SCR_W / 2, 132, Graphics.HCENTER | Graphics.TOP);
        bg.drawString("+1000", SCR_W / 2, 150, Graphics.HCENTER | Graphics.TOP);
    }

    private void drawGameOver() {
        bg.setColor(0x220000);
        bg.fillRect(0, 0, SCR_W, SCR_H);
        bg.setColor(0xFF4444);
        bg.drawString("SYSTEM FAILURE", SCR_W / 2, 120, Graphics.HCENTER | Graphics.TOP);
        bg.setColor(0xFFFFFF);
        bg.drawString("Score: " + player.score, SCR_W / 2, 145, Graphics.HCENTER | Graphics.TOP);
        bg.drawString("District reached: " + (districtIndex + 1), SCR_W / 2, 160, Graphics.HCENTER | Graphics.TOP);
        if ((stateTimer / 12) % 2 == 0) {
            bg.drawString("Press FIRE to retry", SCR_W / 2, 200, Graphics.HCENTER | Graphics.TOP);
        }
    }

    private void drawWin() {
        bg.setColor(0x0A1A0A);
        bg.fillRect(0, 0, SCR_W, SCR_H);
        bg.setColor(0xFFD700);
        bg.drawString("GLITCH DEFEATED", SCR_W / 2, 80, Graphics.HCENTER | Graphics.TOP);
        bg.setColor(0xFFFFFF);
        bg.drawString("The Great Reactor hums back", SCR_W / 2, 105, Graphics.HCENTER | Graphics.TOP);
        bg.drawString("to life across all nine", SCR_W / 2, 120, Graphics.HCENTER | Graphics.TOP);
        bg.drawString("districts. Circuit Kingdom", SCR_W / 2, 135, Graphics.HCENTER | Graphics.TOP);
        bg.drawString("is safe again, thanks to Zap.", SCR_W / 2, 150, Graphics.HCENTER | Graphics.TOP);

        drawZapIcon(SCR_W / 2 - 7, 175, true);

        bg.setColor(0x66CCFF);
        bg.drawString("Final score: " + player.score, SCR_W / 2, 210, Graphics.HCENTER | Graphics.TOP);
        if ((stateTimer / 12) % 2 == 0) {
            bg.setColor(0xFFFFFF);
            bg.drawString("Press FIRE to play again", SCR_W / 2, 240, Graphics.HCENTER | Graphics.TOP);
        }
    }

    private String[] splitLines(String text) {
        Vector out = new Vector();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                out.addElement(text.substring(start, i));
                start = i + 1;
            }
        }
        out.addElement(text.substring(start));
        String[] arr = new String[out.size()];
        for (int i = 0; i < out.size(); i++) arr[i] = (String) out.elementAt(i);
        return arr;
    }
}
