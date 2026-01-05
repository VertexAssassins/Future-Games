package utils;

import java.awt.image.BufferedImage;

public class Animation {
    private BufferedImage[] frames;
    private int index = 0;
    private int counter = 0;
    private int speed;

    public Animation(BufferedImage[] frames, int speed) {
        this.frames = frames;
        this.speed = speed;
    }

    public void update() {
        counter++;
        if (counter >= speed) {
            counter = 0;
            index = (index + 1) % frames.length;
        }
    }

    public BufferedImage getCurrentFrame() {
        return frames[index];
    }

    public void reset() {
        index = 0;
        counter = 0;
    }
}