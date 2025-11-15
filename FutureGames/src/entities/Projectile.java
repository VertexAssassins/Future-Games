package entities;

import java.awt.Graphics;
import java.awt.Color;
import java.util.List;

public class Projectile {
    private double x, y;
    private final double angle; // radians
    private final double speed;
    private final double range;
    private final double damage;
    private double traveled = 0;

    private final int size = 15; // simple circle for now

    public Projectile(double x, double y, double angle, double speed, double range, double damage) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.range = range;
        this.damage = damage;
    }

    public boolean update(List<Enemy> enemies) {
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;

        x += dx;
        y += dy;
        traveled += Math.sqrt(dx*dx + dy*dy);

        // Check collisions
        for (Enemy e : enemies) {
            double distSq = (e.getX() - x)*(e.getX() - x) + (e.getY() - y)*(e.getY() - y);
            double hitBuffer = 4.0;
            double combinedRadius = e.getColliderRadius() + size / 2 + hitBuffer;
            if (distSq <= combinedRadius * combinedRadius) {
                e.takeDamage(damage);
                return false; // projectile disappears on hit
            }
        }

        // Remove if exceeded range
        return traveled < range;
    }

    public void draw(Graphics g, double cameraX, double cameraY) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)(x - cameraX - size/2), (int)(y - cameraY - size/2), size, size);
    }
}
