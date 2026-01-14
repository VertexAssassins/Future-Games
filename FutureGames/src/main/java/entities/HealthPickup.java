package entities;

import java.awt.*;

import core.CameraManager;
import utils.Constants;

public class HealthPickup {

    private double x, y;
    private int amount; // 5, 10, 15, 20
    private final int size = 20;

    public HealthPickup(double x, double y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, size, size);
    }

    public void draw(Graphics g, CameraManager camera) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                int wrappedX = (int)(x + dx * Constants.MAP_WIDTH);
                int wrappedY = (int)(y + dy * Constants.MAP_HEIGHT);

                int drawX = wrappedX - camera.getOffsetX();
                int drawY = wrappedY - camera.getOffsetY();

                g.setColor(Color.PINK);
                g.fillOval(drawX, drawY, size, size);

                g.setColor(Color.WHITE);
                g.drawString("+" + amount, drawX + 3, drawY + 15);
            }
        }
    }
}