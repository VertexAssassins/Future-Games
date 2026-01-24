package entities;

import java.awt.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import utils.BloodSplatter;
import utils.Constants;
import utils.Sound;
import utils.Vector2;
import core.CameraManager;
import core.GamePanel;
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

    private double lastX, lastY;
    private int stuckFrames = 0;

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
    private boolean deathHandled = false;

    // Global modifiers
    public static double GLOBAL_SPEED_MULT = 1.0;
    public static double GLOBAL_DAMAGE_MULT = 1.0;
    public static double GLOBAL_HEALTH_MULT = 1.0;
    public static double GLOBAL_COOLDOWN_MULT = 1.0;
    public static double GLOBAL_POINTS_MULT = 1.0;

    public double getX() { return worldX; }
    public double getY() { return worldY; }

    public boolean isDeathHandled() { return deathHandled; }
    public void markDeathHandled() { deathHandled = true; }

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

    public void updateMovement(Player player, GameWorld world, List<Enemy> nearbyEnemies) {

        // 1. Direction toward player
        Vector2 chase = computeChase(player, world);
        Vector2 separation = computeSeparation(nearbyEnemies);
        Vector2 noise = computeNoise();
        Vector2 orbit = computeOrbit(player, world);

        // Base direction is chase
        Vector2 dir = new Vector2(0, 0);
        dir.add(chase);
        dir.add(separation);
        dir.add(noise);
        dir.add(orbit);

        // Normalize final direction
        if (dir.length() > 0)
            dir = dir.normalized();

        double r = getColliderRadius();

        // 2. Try full movement
        double nextX = worldX + dir.x * speed;
        double nextY = worldY + dir.y * speed;

        boolean moved = false;

        // Try X movement
        if (!world.collidesCircle(nextX, worldY, r)) {
            worldX = nextX;
            moved = true;
        }

        // Try Y movement
        if (!world.collidesCircle(worldX, nextY, r)) {
            worldY = nextY;
            moved = true;
        }

        // 3. Slide along obstacle if blocked
        if (!moved) {
            Vector2 perp = new Vector2(-dir.y, dir.x).normalized();
            double px = worldX + perp.x * speed;
            double py = worldY + perp.y * speed;

            if (!world.collidesCircle(px, py, r)) {
                worldX = px;
                worldY = py;
                moved = true;
            }
        }

        // 4. If still stuck, try opposite perpendicular
        if (!moved) {
            Vector2 perp = new Vector2(dir.y, -dir.x).normalized();
            double px = worldX + perp.x * speed;
            double py = worldY + perp.y * speed;

            if (!world.collidesCircle(px, py, r)) {
                worldX = px;
                worldY = py;
                moved = true;
            }
        }

        // 5. Stuck detection
        if (Math.abs(worldX - lastX) < 0.3 && Math.abs(worldY - lastY) < 0.3)
            stuckFrames++;
        else
            stuckFrames = 0;

        lastX = worldX;
        lastY = worldY;

        // 6. Teleport unstuck if needed
        if (stuckFrames > 25) {
            teleportUnstuck(world);
        }

        // 7. Update facing
        updateFacingDirection(dir.x);
    }

    private Vector2 computeSeparation(List<Enemy> nearby) {
        Vector2 force = new Vector2(0, 0);

        for (Enemy e : nearby) {
            if (e == this) continue;

            double dx = wrappedDelta(worldX, e.worldX, world.getWidth());
            double dy = wrappedDelta(worldY, e.worldY, world.getHeight());
            double dist = Math.sqrt(dx*dx + dy*dy);

            if (dist > 0 && dist < desiredSpacing) {
                double t = (desiredSpacing - dist) / desiredSpacing; // 0..1
                force.add(new Vector2(dx / dist, dy / dist).mul(t));
            }
        }

        return force.mul(separationStrength);
    }

    private Vector2 computeNoise() {
        double angle = Math.random() * Math.PI * 2;
        return new Vector2(Math.cos(angle), Math.sin(angle)).mul(noiseStrength);
    }

    private Vector2 computeOrbit(Player player, GameWorld world) {
        double dx = wrappedDelta(player.getX(), worldX, world.getWidth());
        double dy = wrappedDelta(player.getY(), worldY, world.getHeight());
        double dist = Math.sqrt(dx*dx + dy*dy);

        if (dist > orbitRadius) return new Vector2(0, 0);

        // perpendicular direction
        Vector2 orbit = new Vector2(-dy, dx).normalized();
        double t = 1.0 - (dist / orbitRadius); // stronger when closer

        return orbit.mul(t * orbitStrength);
    }

    private Vector2 computeChase(Player player, GameWorld world) {
        double dx = wrappedDelta(player.getX(), worldX, world.getWidth());
        double dy = wrappedDelta(player.getY(), worldY, world.getHeight());
        return new Vector2(dx, dy).normalized().mul(chaseStrength);
    }

    private void teleportUnstuck(GameWorld world) {
        double r = getColliderRadius();

        for (int i = 0; i < 12; i++) {
            double angle = Math.random() * Math.PI * 2;
            double dist = 40 + Math.random() * 40;

            double tx = WorldManager.wrapX(worldX + Math.cos(angle) * dist);
            double ty = WorldManager.wrapY(worldY + Math.sin(angle) * dist);

            if (!world.collidesCircle(tx, ty, r)) {
                worldX = tx;
                worldY = ty;
                stuckFrames = 0;
                return;
            }
        }

        // Last resort
        worldY = WorldManager.wrapY(worldY - 30);
        stuckFrames = 0;
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

    public void onDeath(Player player, GamePanel panel) {
        player.addPoints(points);
        BloodSplatter splatter = new BloodSplatter(worldX, worldY);
        panel.getBloodEffects().add(splatter);
    }
}