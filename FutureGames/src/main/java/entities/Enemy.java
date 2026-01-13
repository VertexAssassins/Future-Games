package entities;

import java.awt.*;

import utils.Constants;
import utils.Sound;

import java.awt.image.BufferedImage;

import core.CameraManager;
import core.WorldManager;

public class Enemy {
protected double worldX, worldY;
protected BufferedImage sprite;

    private double knockbackVX = 0;
    private double knockbackVY = 0;
    private double knockbackDecay = 0.85; // decay factor per frame

    private float hitOverlayAlpha = 0f;
    private final float maxOverlayAlpha = 0.8f;
    private final float overlayFadeSpeed = 0.1f; // fade per frame

    private static final Sound IMPACT_SOUND = new Sound("/enemies/audio/hit.wav");

    // Baseline stats
    protected double baseSpeed;
    protected double baseDamage;
    protected boolean isMelee;
    protected boolean isProjectile;
    protected double baseHealth;
    protected long baseAttackCooldown; // default 1 second
    protected int basePoints;

    // Effective stats (after modifiers)
    protected double speed;
    protected double damage;
    protected double health;
    protected long attackCooldown;
    protected int points;

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
    protected boolean facingRight = true;

    public double getX() { return worldX; }
    public double getY() { return worldY; }

    public Enemy(double worldX, double worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
        burstRemaining = burstCount;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public double getColliderRadius() {
        return size * 0.55; // 10% larger than half-size (0.5 * size * 1.1 = 0.55 * size)
    }

    protected void applyGlobalModifiers(double speedMult, double damageMult, double healthMult, double cooldownMult, int pointsMult) {
        speed = baseSpeed * speedMult;
        damage = baseDamage * damageMult;
        health = baseHealth * healthMult;
        attackCooldown = (long)(baseAttackCooldown * cooldownMult);
        points = basePoints * pointsMult;
    }

    public void attemptAttack(Player player) {
    long now = System.currentTimeMillis();
    double dx = worldX - player.getX();
    double dy = worldY - player.getY();
    double distance = Math.sqrt(dx * dx + dy * dy);
    double collisionDistance = getColliderRadius() + player.getColliderRadius();

        if (distance < collisionDistance) {
            if (burstRemaining > 0 && now - lastBurstTime >= burstInterval) {
                player.takeDamage(damage);
                player.applyKnockback(worldX, worldY, damage);
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

    protected void updateFacingDirection(double vx) {
        if (vx > 0) facingRight = true;
        else if (vx < 0) facingRight = false;
    }

    public void updateMovement(Player player) {
        double dx = getWrappedDelta(player.getX(), worldX, Constants.MAP_WIDTH);
        double dy = getWrappedDelta(player.getY(), worldY, Constants.MAP_HEIGHT);

        if (hitOverlayAlpha > 0f) {
            hitOverlayAlpha -= overlayFadeSpeed;
            if (hitOverlayAlpha < 0f) hitOverlayAlpha = 0f;
        }

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            double normX = dx / length;
            double normY = dy / length;

            worldX += (normX * speed);
            worldY += (normY * speed);

            updateFacingDirection(normX);
        }

        worldX = WorldManager.wrapX(worldX);
        worldY = WorldManager.wrapY(worldY);
    }

    public void applyKnockbackMovement() {
        worldX += knockbackVX;
        worldY += knockbackVY;

        knockbackVX *= knockbackDecay;
        knockbackVY *= knockbackDecay;

        if (Math.abs(knockbackVX) < 0.1) knockbackVX = 0;
        if (Math.abs(knockbackVY) < 0.1) knockbackVY = 0;

        worldX = WorldManager.wrapX(worldX);
        worldY = WorldManager.wrapY(worldY);
    }

    public void applyKnockback(double sourceX, double sourceY, double damage, double maxHealth) {
        double threshold = 0.25 * maxHealth;
        if (damage < threshold) return;

        double scale = damage / threshold; // 1.0 = 25%, 2.0 = 50%, etc.
        double dx = worldX - sourceX;
        double dy = worldY - sourceY;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;

        double knockbackStrength = 5.0 * scale; // tune this constant

        knockbackVX = (dx / length) * knockbackStrength;
        knockbackVY = (dy / length) * knockbackStrength;
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

            //DEBUG COLLIDER RADIUS
                int r = (int) getColliderRadius();
                int centerX = drawX;
                int centerY = drawY;

                g.setColor(Color.RED);
                g.drawOval(centerX - r, centerY - r, r * 2, r * 2);
            // END DEBUG

                if (sprite != null) {
                    Graphics2D g2d = (Graphics2D) g.create();

                    // Draw base sprite
                    int w = size;
                    int h = size;

                    // Center the sprite
                    int x = drawX - size / 2;
                    int y = drawY - size / 2;

                    if (facingRight) {
                        g2d.drawImage(sprite, x, y, w, h, null);
                    } else {
                        g2d.drawImage(sprite,
                            x + w, y,      // dest top-left
                            x,     y + h,  // dest bottom-right
                            0, 0, sprite.getWidth(), sprite.getHeight(),
                            null
                        );
                    }

                    // Apply red tint only to non-transparent pixels
                    if (hitOverlayAlpha > 0f) {
                        BufferedImage tinted = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D tg = tinted.createGraphics();

                        tg.drawImage(sprite, 0, 0, size, size, null);
                        tg.setComposite(AlphaComposite.SrcAtop.derive(hitOverlayAlpha));
                        tg.setColor(Color.RED);
                        tg.fillRect(0, 0, size, size);
                        tg.dispose();

                        g2d.drawImage(tinted, x, y, w, h, null);
                    }

                    g2d.dispose();
                } else {
                    g.setColor(color);
                    g.fillRect(drawX, drawY, size, size);
                }
            }
        }
    }

    public double getDamage() {
        return baseDamage;
    }

    public double getHealth() {
        return health;
    }

    public int getPoints() {
        return points;
    }

    public double getCenterX() { return worldX; }
    public double getCenterY() { return worldY; }

    public Rectangle getBounds() {
        int r = (int)getColliderRadius();
        return new Rectangle((int)getCenterX() - r, (int)getCenterY() - r, r * 2, r * 2);
    }

    public void takeDamage(double amount) {
        health -= amount;
        hitOverlayAlpha = maxOverlayAlpha;

        float pitch = 1.0f + (float)(Math.random() * 0.1 - 0.10); // ±5%
        IMPACT_SOUND.play(pitch);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void onDeath(Player player) {
        player.addPoints(points);
        System.out.println("Enemy died at (" + worldX + ", " + worldY + ")");
    }
}