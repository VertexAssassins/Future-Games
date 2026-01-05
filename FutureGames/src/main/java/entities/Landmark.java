package entities;

import java.awt.*;

import core.CameraManager;
import utils.Constants;

public class Landmark {
    private final int worldX, worldY;
    private final int size = 50;

    public Landmark(int worldX, int worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public void draw(Graphics g, CameraManager camera) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int wrappedX = (worldX + dx * Constants.MAP_WIDTH);
                int wrappedY = (worldY + dy * Constants.MAP_HEIGHT);
                int drawX = wrappedX - camera.getOffsetX();
                int drawY = wrappedY - camera.getOffsetY();

                g.setColor(Color.RED);
                g.fillRect(drawX, drawY, size, size);
            }
        }
    }
}