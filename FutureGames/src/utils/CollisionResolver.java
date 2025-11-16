package utils;

import entities.Enemy;
import entities.Projectile;

public class CollisionResolver {
    private static final double COLLISION_BUFFER = 6.0;

    public static boolean checkProjectileHit(Projectile p, Enemy e, double x1, double y1, double x2, double y2) {
        double ex = e.getCenterX();
        double ey = e.getCenterY();
        double radius = e.getColliderRadius() + p.getRadius() + COLLISION_BUFFER;

        double extendFactor = 1.05; // To account for high speed projectiles
        double dx = (x2 - x1) * extendFactor;
        double dy = (y2 - y1) * extendFactor;
        double lengthSq = dx * dx + dy * dy;
        if (lengthSq == 0) return false;

        double t = ((ex - x1) * dx + (ey - y1) * dy) / lengthSq;
        t = Math.max(0, Math.min(1, t));

        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;

        double distX = closestX - ex;
        double distY = closestY - ey;
        double distSq = distX * distX + distY * distY;

        return distSq <= radius * radius;
    }

    public static boolean checkInitialOverlap(Projectile p, Enemy e) {
        double ex = e.getCenterX();
        double ey = e.getCenterY();
        double radius = e.getColliderRadius() + p.getRadius() + COLLISION_BUFFER;

        double dx = p.getX() - ex;
        double dy = p.getY() - ey;
        return dx * dx + dy * dy <= radius * radius;
    }
}