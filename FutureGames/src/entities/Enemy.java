package entities;

import java.awt.*;
import core.CameraManager;
import core.WorldManager;
import utils.Constants;

public class Enemy {
protected double worldX, worldY;

    // Baseline stats
    protected double baseSpeed;
    protected double baseDamage;
    protected boolean isMelee;
    protected boolean isProjectile;
    protected double baseHealth;
    protected long baseAttackCooldown; // default 1 second

    // Effective stats (after modifiers)
    protected double speed;
    protected double damage;
    protected double health;
    protected long attackCooldown;

    //Burst logic
    protected int burstCount;          // Number of hits per burst
    protected int burstRemaining;      // Hits left in current burst
    protected long burstInterval;    // Time between hits in a burst (ms)
    protected long lastBurstTime;      // Last time a burst hit occurred
    protected long lastAttackCycleTime; // Last time a full burst cycle started

    // Visuals
    protected int size = 40;
    protected Color color = Color.MAGENTA;
    protected double colliderRadius = size * .75;

    public double getX() { return worldX; }
    public double getY() { return worldY; }
    public double getColliderRadius() { return colliderRadius; }

    public Enemy(double worldX, double worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
        burstRemaining = burstCount;
    }

    protected void applyGlobalModifiers(double speedMult, double damageMult, double healthMult, double cooldownMult) {
        speed = baseSpeed * speedMult;
        damage = baseDamage * damageMult;
        health = baseHealth * healthMult;
        attackCooldown = (long)(baseAttackCooldown * cooldownMult);
    }

    public void attemptAttack(Player player) {
    long now = System.currentTimeMillis();
    double dx = worldX - player.getX();
    double dy = worldY - player.getY();
    double distance = Math.sqrt(dx * dx + dy * dy);
    double collisionDistance = (size + player.getSize()) / 2.0;

        if (distance < collisionDistance) {
            if (burstRemaining > 0 && now - lastBurstTime >= burstInterval) {
                player.takeDamage(damage);
                lastBurstTime = now;
                burstRemaining--;
                //System.out.println("Enemy burst hit! Player health: " + player.getHealth());
            }

            if (burstRemaining == 0 && now - lastAttackCycleTime >= attackCooldown) {
                burstRemaining = burstCount;
                lastAttackCycleTime = now;
            }
        }
    }

    public void setPosition(double x, double y) {
    this.worldX = x;
    this.worldY = y;
    }

    public void update(Player player) {
        double dx = getWrappedDelta(player.getX(), worldX, Constants.MAP_WIDTH);
        double dy = getWrappedDelta(player.getY(), worldY, Constants.MAP_HEIGHT);

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            double normX = dx / length;
            double normY = dy / length;

            worldX += (normX * speed);
            worldY += (normY * speed);
        }

        worldX = WorldManager.wrapX(worldX);
        worldY = WorldManager.wrapY(worldY);
    }

    private int getWrappedDelta(double target, double source, int mapSize) {
    double direct = target - source;
    double wrapped = (direct + mapSize) % mapSize;
        if (wrapped > mapSize / 2) {
            wrapped -= mapSize;
        }
    return (int) wrapped;
    }

    public void draw(Graphics g, CameraManager camera) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                double wrappedX = worldX + dx * Constants.MAP_WIDTH;
                double wrappedY = worldY + dy * Constants.MAP_HEIGHT;
                int drawX = (int)(wrappedX - camera.getOffsetX());
                int drawY = (int)(wrappedY - camera.getOffsetY());

                g.setColor(Color.RED);
                int r = (int) getColliderRadius();
                g.drawOval(drawX + size / 2 - r, drawY + size / 2 - r, r * 2, r * 2);

                g.setColor(color);
                g.fillRect(drawX, drawY, size, size);
            }
        }
    }

    public double getDamage() {
        return baseDamage;
    }

    public double getHealth() {
        return health;
    }

    public void takeDamage(double amount) {
        health -= amount;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void onDeath() {
    // Placeholder for future effects
    System.out.println("Enemy died at (" + worldX + ", " + worldY + ")");
    }
}