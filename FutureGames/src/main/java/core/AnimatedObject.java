package core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AnimatedObject extends WorldObject {

    private BufferedImage[] frames;
    private int frameIndex = 0;
    private double frameTime = 0;
    private double frameSpeed = 0.15; // seconds per frame

    public AnimatedObject(double x, double y, BufferedImage[] frames) {
        super(x, y, frames[0].getWidth(), frames[0].getHeight());
        this.frames = frames;
    }

    @Override
    public void update(double delta) {
        frameTime += delta;
        if (frameTime >= frameSpeed) {
            frameTime = 0;
            frameIndex = (frameIndex + 1) % frames.length;
        }
    }

    @Override
    public void draw(Graphics2D g2, double camX, double camY, int screenW, int screenH, int worldWidth, int worldHeight) {
        int screenX = (int)(x - camX);
        int screenY = (int)(y - camY);
        g2.drawImage(frames[frameIndex], screenX, screenY, null);
    }
}
