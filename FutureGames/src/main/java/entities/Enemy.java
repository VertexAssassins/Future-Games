package entities;

import java.awt.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import utils.Constants;
import utils.Sound;
import core.CameraManager;
import core.WorldManager;
import core.GameWorld;

public class Enemy {
    protected double worldX, worldY;
    protected GameWorld world;
    protected BufferedImage sprite;    

    private double desiredSpacing;  // how far enemies try to stay apart
    private double separationStrength;  // how strongly they push away
    private double noiseStrength;   // randomness in movement
    private double orbitRadius;     // distance where orbiting begins
    private double orbitStrength;   // how strongly they orbit
    private double chaseStrength;   // base chase force

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

    // Global modifiers
    public static double GLOBAL_SPEED_MULT = 1.0;
    public static double GLOBAL_DAMAGE_MULT = 1.0;
    public static double GLOBAL_HEALTH_MULT = 1.0;
    public static double GLOBAL_COOLDOWN_MULT = 1.0;
    public static double GLOBAL_POINTS_MULT = 1.0;

    public double getX() { return worldX; }
    public double getY() { return worldY; }

    public Enemy(double worldX, double worldY,
                double desiredSpacing,
                double separationStrength,
                double noiseStrength,
                double orbitRadius,
                double orbitStrength,
                double chaseStrength,
                GameWorld world) {

        if (world == null) {
            System.out.println("NULL WORLD ENEMY: " + getClass().getSimpleName());
            Thread.dumpStack();
        }

        this.worldX = worldX;
        this.worldY = worldY;

        this.desiredSpacing = desiredSpacing;
        this.separationStrength = separationStrength;
        this.noiseStrength = noiseStrength;
        this.orbitRadius = orbitRadius;
        this.orbitStrength = orbitStrength;
        this.chaseStrength = chaseStrength;

        this.world = world;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public double getColliderRadius() {
        return size * 0.55; // 10% larger than half-size (0.5 * size * 1.1 = 0.55 * size)
    }

    protected void applyGlobalModifiers(double speedMult, double damageMult, double healthMult, double cooldownMult, double pointsMult) {
        speed = baseSpeed * speedMult;
        damage = baseDamage * damageMult;
        health = baseHealth * healthMult;
        attackCooldown = (long)(baseAttackCooldown * cooldownMult);
        points = (int)(basePoints * pointsMult);
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

    public void updateMovement(Player player, List<Enemy> nearby) {

        // 1. Base chase direction
        double dx = wrappedDelta(player.getX(), worldX, Constants.MAP_WIDTH);
        double dy = wrappedDelta(player.getY(), worldY, Constants.MAP_HEIGHT);

        double distToPlayer = Math.sqrt(dx*dx + dy*dy);

        double toPlayerX = dx / distToPlayer;
        double toPlayerY = dy / distToPlayer;

        // -----------------------------
        // 2. Separation steering
        // -----------------------------
        double sepX = 0;
        double sepY = 0;

        for (Enemy other : nearby) {
            if (other == this) continue;

            double ox = wrappedDelta(worldX, other.worldX, Constants.MAP_WIDTH);
            double oy = wrappedDelta(worldY, other.worldY, Constants.MAP_HEIGHT);
            double d = Math.sqrt(ox*ox + oy*oy);

            if (d < desiredSpacing && d > 0) {
                double push = (desiredSpacing - d) / desiredSpacing;
                sepX += (ox / d) * push;
                sepY += (oy / d) * push;
            }
        }

        // -----------------------------
        // 3. Directional noise
        // -----------------------------
        double noiseX = (Math.random() * 2 - 1) * noiseStrength;
        double noiseY = (Math.random() * 2 - 1) * noiseStrength;

        // -----------------------------
        // 4. Orbiting behavior
        // -----------------------------
        double orbitX = 0;
        double orbitY = 0;

        if (distToPlayer < orbitRadius) {
            orbitX = -toPlayerY;
            orbitY = toPlayerX;
        }

        // -----------------------------
        // 5. Combine steering forces
        // -----------------------------
        double finalX =
                toPlayerX * chaseStrength +
                sepX * separationStrength +
                noiseX +
                orbitX * orbitStrength;

        double finalY =
                toPlayerY * chaseStrength +
                sepY * separationStrength +
                noiseY +
                orbitY * orbitStrength;

        // Normalize final vector
        double len = Math.sqrt(finalX*finalX + finalY*finalY);
        if (len > 0) {
            finalX /= len;
            finalY /= len;
        }

        updateFacingDirection(toPlayerX);

        // -----------------------------
        // 6. Apply movement
        // -----------------------------
        double nextX = WorldManager.wrapX(worldX + finalX * speed);
        double nextY = WorldManager.wrapY(worldY + finalY * speed);

        double r = getColliderRadius();

        if (!world.collidesCircle(nextX, nextY, r)) {
            worldX = nextX;
            worldY = nextY;
        }
    }


    public void applyKnockbackMovement() {
        double nextX = WorldManager.wrapX(worldX + knockbackVX);
        double nextY = WorldManager.wrapY(worldY + knockbackVY);

        double r = getColliderRadius();

        if (!world.collidesCircle(nextX, nextY, r)) {
            worldX = nextX;
            worldY = nextY;
        } else {
            knockbackVX = 0;
            knockbackVY = 0;
        }

        knockbackVX *= knockbackDecay;
        knockbackVY *= knockbackDecay;

        if (Math.abs(knockbackVX) < 0.1) knockbackVX = 0;
        if (Math.abs(knockbackVY) < 0.1) knockbackVY = 0;
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

    private double wrappedDelta(double target, double source, double mapSize) {
        double delta = target - source;

        if (delta >  mapSize / 2) delta -= mapSize;
        if (delta < -mapSize / 2) delta += mapSize;

        return delta;
    }

    protected void loadRandomSprite(String folderPath, String baseName) {
        int variant = (int)(Math.random() * 3) + 1; // 1–3
        String path = folderPath + baseName + "_" + variant + ".png";

        try {
            BufferedImage img = ImageIO.read(getClass().getResource(path));
            setSprite(img);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load enemy sprite: " + path);
        }
    }

    public void draw(Graphics g, CameraManager camera) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                double wrappedX = worldX + dx * Constants.MAP_WIDTH;
                double wrappedY = worldY + dy * Constants.MAP_HEIGHT;
                int drawX = (int)(wrappedX - camera.getOffsetX());
                int drawY = (int)(wrappedY - camera.getOffsetY());

            /*DEBUG COLLIDER RADIUS
                int r = (int) getColliderRadius();
                int centerX = drawX;
                int centerY = drawY;

                g.setColor(Color.RED);
                g.drawOval(centerX - r, centerY - r, r * 2, r * 2);
            END DEBUG */

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

    public void update() {
        // Fade hit overlay
        if (hitOverlayAlpha > 0f) {
            hitOverlayAlpha -= overlayFadeSpeed;
            if (hitOverlayAlpha < 0f) {
                hitOverlayAlpha = 0f;
            }
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void onDeath(Player player) {
        player.addPoints(points);
        System.out.println("Enemy died at (" + worldX + ", " + worldY + ")");
    }
}