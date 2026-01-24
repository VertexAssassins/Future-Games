package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import core.CameraManager;

public class BloodSplatter {

    private static BufferedImage spriteSheet;

    private final double x, y;
    private int frame = 0;
    private int ticks = 0;
    private final int frameDelay = 6; // how long each frame lasts
    private boolean finished = false;

    public BloodSplatter(double x, double y) {
        this.x = x;
        this.y = y;

        if (spriteSheet == null) {
            try {
                spriteSheet = ImageIO.read(
                    getClass().getResourceAsStream("/enemies/effects/BloodSplatter.png")
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void update() {
        ticks++;
        if (ticks >= frameDelay) {
            ticks = 0;
            frame++;
            if (frame >= 3) { // 3 frames in the sheet
                finished = true;
            }
        }
    }

    public void render(Graphics2D g, CameraManager camera) {
        if (finished) return;

        int frameWidth = spriteSheet.getWidth() / 3;
        int frameHeight = spriteSheet.getHeight();

        double camX = camera.getOffsetX();
        double camY = camera.getOffsetY();

        int mapW = Constants.MAP_WIDTH;
        int mapH = Constants.MAP_HEIGHT;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                double wrappedX = x + dx * mapW;
                double wrappedY = y + dy * mapH;

                int screenX = (int)(wrappedX - frameWidth / 2 - camX);
                int screenY = (int)(wrappedY - frameHeight / 2 - camY);

                g.drawImage(
                    spriteSheet.getSubimage(frame * frameWidth, 0, frameWidth, frameHeight),
                    screenX,
                    screenY,
                    null
                );
            }
        }
    }
}
