package entities;

import java.awt.Graphics;
import java.awt.Color;
import java.util.List;
import java.awt.Rectangle;
import utils.Quadtree;

import utils.CollisionResolver;

public class Projectile {
    private double x, y;
    private final double angle; // radians
    private final double speed;
    private final double range;
    private final double damage;
    private double traveled = 0;

    private final int size = 15; // simple circle for now

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return size * .6; }

    public Projectile(double x, double y, double angle, double speed, double range, double damage) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.range = range;
        this.damage = damage;
    }

    public Rectangle getSweptAABB(double dx, double dy) {
        double minX = Math.min(x, x + dx) - getRadius();
        double minY = Math.min(y, y + dy) - getRadius();
        double width = Math.abs(dx) + getRadius() * 2;
        double height = Math.abs(dy) + getRadius() * 2;

        return new Rectangle((int)minX, (int)minY, (int)width, (int)height);
    }

    public boolean update(List<Enemy> enemies, Quadtree quadtree) {
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        Rectangle sweepBox = getSweptAABB(dx, dy);
        List<Enemy> candidates = quadtree.query(sweepBox); // You’ll need access to the quadtree here

        // Initial overlap check
        for (Enemy e : candidates) {
            if (CollisionResolver.checkInitialOverlap(this, e)) {
                e.takeDamage(damage);
                e.applyKnockback(x, y, damage, e.getHealth() + damage);
                return false;
            }
        }

        double maxRadius = getMaxEnemyRadius(candidates);
        int steps = Math.max(6, (int)(speed / (maxRadius * 0.5)));
        double stepDx = dx / steps;
        double stepDy = dy / steps;

        for (int i = 0; i < steps; i++) {
            double nextX = x + stepDx;
            double nextY = y + stepDy;

            for (Enemy e : candidates) {
                if (CollisionResolver.checkProjectileHit(this, e, x, y, nextX, nextY)) {
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

    private double getMaxEnemyRadius(List<Enemy> enemies) {
        double max = 0;
        for (Enemy e : enemies) {
            double r = e.getColliderRadius();
            if (r > max) max = r;
        }
        return Math.max(1.0, max); // Clamp to avoid divide-by-zero
    }

    public void draw(Graphics g, double cameraX, double cameraY) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)(x - cameraX - size/2), (int)(y - cameraY - size/2), size, size);

        g.setColor(Color.RED);
        g.drawLine((int)(x - cameraX), (int)(y - cameraY),
                (int)(x + Math.cos(angle) * speed - cameraX),
                (int)(y + Math.sin(angle) * speed - cameraY));

        Rectangle box = getSweptAABB(Math.cos(angle) * speed, Math.sin(angle) * speed);
            g.setColor(Color.CYAN);
            g.drawRect(box.x - (int)cameraX, box.y - (int)cameraY, box.width, box.height);
    }
}
