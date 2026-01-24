package core;

import java.awt.*;

public abstract class WorldObject {

    protected double x, y;          // world position
    protected int width, height;    // size in pixels
    protected double collisionRadius = 0;

    public double centerX() { return x; };
    public double centerY() { return y; };
    public double getX() { return x; }
    public double getY() { return y; }
    public double getCollisionRadius() { return collisionRadius; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public WorldObject(double centerX, double centerY, int width, int height) {
        this.x = centerX;
        this.y = centerY;
        this.width = width;
        this.height = height;
    }

    public abstract void update(double delta);
    public abstract void draw(Graphics2D g2, double camX, double camY, int screenW, int screenH, int worldWidth, int worldHeight);
}