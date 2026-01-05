package entities;

import java.awt.*;
import java.util.List;
import utils.Quadtree;
import utils.CollisionResolver;
import utils.Constants;

public class Projectile {
    private double x, y;
    private final double angle;
    private final double speed;
    private final double range;
    private final double damage;
    private double traveled = 0;

    private final int size = 10;

    public Projectile(double x, double y, double angle, double speed, double range, double damage) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.range = range;
        this.damage = damage;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return size; }

    public Rectangle getSweptAABB() {
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        return getSweptAABB(dx, dy);
    }

    public Rectangle getSweptAABB(double dx, double dy) {
        double buffer = 4.0; // small expansion
        double minX = Math.min(x, x + dx) - getRadius() - buffer;
        double minY = Math.min(y, y + dy) - getRadius() - buffer;

        minX = Math.max(0, minX);
        minY = Math.max(0, minY);

        double width = Math.abs(dx) + getRadius() * 2 + buffer * 2;
        double height = Math.abs(dy) + getRadius() * 2 + buffer * 2;
        return new Rectangle((int) minX, (int) minY, (int) width, (int) height);
    }

    public boolean update(List<Enemy> enemies, Quadtree quadtree) {
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        double distance = Math.sqrt(dx * dx + dy * dy);
        List<Enemy> candidates = quadtree.query(getSweptAABB(dx, dy));

        if (checkInitialOverlap(candidates)) return false;
        if (!moveWithCollision(candidates, dx, dy, distance)) return false;
        return !checkFinalOverlap(candidates);
    }

    public boolean move() {
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return moveWithCollision(List.of(), dx, dy, distance);
    }

    private boolean moveWithCollision(List<Enemy> candidates, double dx, double dy, double distance) {
        double stepSize = Math.min(1.0, getRadius() * 0.5);
        int steps = Math.max(10, (int) (distance / stepSize));
        double stepDx = dx / steps;
        double stepDy = dy / steps;

        double currX = x;
        double currY = y;

        currX = wrap(currX, Constants.MAP_WIDTH);
        currY = wrap(currY, Constants.MAP_HEIGHT);

        for (int i = 0; i < steps; i++) {
            double nextX = currX + stepDx;
            double nextY = currY + stepDy;

            for (Enemy e : candidates) {
                if (CollisionResolver.checkProjectileHit(this, e, currX, currY, nextX, nextY)) {
                    applyDamage(e, currX, currY);
                    return false;
                }
            }

            currX = nextX;
            currY = nextY;
            traveled += stepSize;
            if (traveled >= range) return false;
        }

        x = currX;
        y = currY;
        return true;
    }

    private double wrap(double value, double max) {
        if (value < 0) return value + max;
        if (value >= max) return value - max;
        return value;
    }

    public boolean checkCollisions(List<Enemy> candidates) {
        return checkInitialOverlap(candidates) || checkFinalOverlap(candidates);
    }

    private boolean checkInitialOverlap(List<Enemy> candidates) {
        for (Enemy e : candidates) {
            if (CollisionResolver.checkInitialOverlap(this, e)) {
                applyDamage(e, x, y);
                return true;
            }
        }
        return false;
    }

    private boolean checkFinalOverlap(List<Enemy> candidates) {
        for (Enemy e : candidates) {
            double dx = x - e.getCenterX();
            double dy = y - e.getCenterY();
            double distSq = dx * dx + dy * dy;
            double collisionDist = getRadius() + e.getColliderRadius();

            if (distSq < collisionDist * collisionDist) {
                applyDamage(e, x, y);
                return true;
            }
        }
        return false;
    }

    private void applyDamage(Enemy e, double hitX, double hitY) {
        e.takeDamage(damage);
        e.applyKnockback(hitX, hitY, damage, e.getHealth() + damage);
    }

    public void draw(Graphics g, double cameraX, double cameraY) {
        int mapWidth = Constants.MAP_WIDTH;
        int mapHeight = Constants.MAP_HEIGHT;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                double wrappedX = x + dx * mapWidth;
                double wrappedY = y + dy * mapHeight;

                int drawX = (int) (wrappedX - cameraX);
                int drawY = (int) (wrappedY - cameraY);

                // Skip off-screen copies
                if (drawX + size < 0 || drawY + size < 0 || drawX > mapWidth || drawY > mapHeight) continue;

                g.setColor(Color.YELLOW);
                g.fillOval(drawX - size / 2, drawY - size / 2, size, size);

                g.setColor(Color.RED);
                g.drawLine(drawX, drawY,
                    (int) (wrappedX + Math.cos(angle) * speed - cameraX),
                    (int) (wrappedY + Math.sin(angle) * speed - cameraY));

                Rectangle box = getSweptAABB();
                g.setColor(Color.CYAN);
                g.drawRect(box.x - (int) cameraX, box.y - (int) cameraY, box.width, box.height);
            }
        }
    }
}