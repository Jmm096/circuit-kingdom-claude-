package com.circuitkingdom;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

/**
 * Circuit Kingdom - entry point MIDlet.
 * A Mario-style platformer: 9 districts, 9 bosses, powerups and lives.
 */
public class PlatformerMIDlet extends MIDlet {

    private GameCanvas canvas;

    public PlatformerMIDlet() {
    }

    protected void startApp() {
        if (canvas == null) {
            canvas = new GameCanvas();
            Display.getDisplay(this).setCurrent(canvas);
        }
        canvas.start();
    }

    protected void pauseApp() {
        // no persistent resources to release
    }

    protected void destroyApp(boolean unconditional) {
        if (canvas != null) {
            canvas.stopGame();
        }
    }
}
