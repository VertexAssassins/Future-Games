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

        int steps = Math.max(4, (int)(speed / 1.5));
        double stepDx = dx / steps;
        double stepDy = dy / steps;

        for (int i = 0; i < steps; i++) {
            double nextX = x + stepDx;
            double nextY = y + stepDy;

            for (Enemy e : enemies) {
                double ex = e.getCenterX();
                double ey = e.getCenterY();
                double radius = e.getColliderRadius() + size / 2 + 6.0;

                if (intersectsCircle(x, y, nextX, nextY, ex, ey, radius)) {
                    e.takeDamage(damage);
                    e.applyKnockback(x, y, damage, e.getHealth() + damage);
                    return false;
                }
            }

            x = nextX;
            y = nextY;
            traveled += Math.sqrt(stepDx * stepDx + stepDy * stepDy);

            if (traveled >= range) return false;
        }

        return true;
    }

    private boolean intersectsCircle(double x1, double y1, double x2, double y2, double cx, double cy, double radius) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        double lengthSq = dx * dx + dy * dy;
        if (lengthSq == 0) return false;

        double t = ((cx - x1) * dx + (cy - y1) * dy) / lengthSq;
        t = Math.max(0, Math.min(1, t)); // clamp to segment

        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;

        double distX = closestX - cx;
        double distY = closestY - cy;
        double distSq = distX * distX + distY * distY;

        return distSq <= radius * radius;
    }

    public void draw(Graphics g, double cameraX, double cameraY) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)(x - cameraX - size/2), (int)(y - cameraY - size/2), size, size);
    }
}
