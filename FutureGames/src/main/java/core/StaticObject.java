package core;

import java.awt.*;

public class StaticObject extends WorldObject {

    private Image sprite;

    public StaticObject(double x, double y, Image sprite) {
        super(x, y, sprite.getWidth(null), sprite.getHeight(null));
        this.sprite = sprite;

        // Use the smaller dimension as the circle diameter
        int diameter = Math.min(width, height);

        // Scale it down so the player can slide around it
        collisionRadius = diameter * 0.35;  // tweak this value
    }

    @Override
    public void update(double delta) {
        // static objects do nothing
    }

    @Override
    public void draw(Graphics2D g2,
                     double camX, double camY,
                     int screenW, int screenH,
                     int worldWidth, int worldHeight) {

        double wrappedX = x;
        double wrappedY = y;

        // Wrap horizontally
        if (wrappedX < camX - width) wrappedX += worldWidth;
        else if (wrappedX > camX + screenW) wrappedX -= worldWidth;

        // Wrap vertically
        if (wrappedY < camY - height) wrappedY += worldHeight;
        else if (wrappedY > camY + screenH) wrappedY -= worldHeight;

        int screenX = (int)(wrappedX - camX - width / 2);
        int screenY = (int)(wrappedY - camY - height / 2);

        g2.drawImage(sprite, screenX, screenY, null);

        if (GameWorld.DEBUG_COLLISION) {
            int cx = (int)(x - camX);
            int cy = (int)(y - camY);
            int r = (int)collisionRadius;

            g2.setColor(new Color(255, 0, 0, 120));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            g2.setColor(Color.RED);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }

        if (GameWorld.DEBUG_COLLISION) {
            int centerX = (int)(x - camX);
            int centerY = (int)(y - camY);

            g2.setColor(Color.YELLOW);
            g2.fillOval(centerX - 3, centerY - 3, 6, 6);
        }
    }

}
