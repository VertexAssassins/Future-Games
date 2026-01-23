package core;

import java.awt.*;

public abstract class WorldObject {

    protected double x, y;          // world position
    protected int width, height;    // size in pixels
    protected double collisionRadius = 0;

    public WorldObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(double delta);
    public abstract void draw(Graphics2D g2, double camX, double camY, int screenW, int screenH, int worldWidth, int worldHeight);
}