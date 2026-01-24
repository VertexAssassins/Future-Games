package entities;

import java.awt.*;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import utils.BloodSplatter;
import utils.Constants;
import utils.Sound;
import utils.FlowField;
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

    private final double pathOffsetAngle = Math.random() * Math.PI * 2;
    private final double pathOffsetStrength = 0.2 + Math.random() * 0.3;

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

    public void updateMovement(Player player, List<Enemy> nearby, FlowField flowField) {

        // 1. Direction to player (ground truth)
        double dx = wrappedDelta(player.getX(), worldX, Constants.MAP_WIDTH);
        double dy = wrappedDelta(player.getY(), worldY, Constants.MAP_HEIGHT);

        double distToPlayer = Math.sqrt(dx*dx + dy*dy);
        if (distToPlayer == 0) distToPlayer = 0.0001;

        double toPlayerX = dx / distToPlayer;
        double toPlayerY = dy / distToPlayer;


        // 2. Base from flow field (global path)
        double baseX = flowField.sampleDirX(worldX, worldY);
        double baseY = flowField.sampleDirY(worldX, worldY);

        // if flow field has no info here, fall back to direct chase
        double baseLen = Math.sqrt(baseX*baseX + baseY*baseY);
        if (baseLen < 0.1) {
            baseX = toPlayerX;
            baseY = toPlayerY;
        } else {
            baseX /= baseLen;
            baseY /= baseLen;
        }

        // 2a. enemy-specific path variation (small, stable)
        double ox = Math.cos(pathOffsetAngle) * pathOffsetStrength; // e.g. 0.1–0.3
        double oy = Math.sin(pathOffsetAngle) * pathOffsetStrength;

        baseX += ox;
        baseY += oy;

        // 2b. speed-based flow weight (0.4–1.0)
        double maxSpeed = speed;          // per enemy type
        double speedFactor = Math.min(1.0, speed / maxSpeed);
        double flowWeight = 0.2 + speedFactor * 0.5;

        // 2c. tiny jitter (just enough to break symmetry)
        double jitter = 0.05 * speedFactor;
        baseX += (Math.random() * 2 - 1) * jitter;
        baseY += (Math.random() * 2 - 1) * jitter;

        // re-normalize base direction
        baseLen = Math.sqrt(baseX*baseX + baseY*baseY);
        if (baseLen > 0.0001) {
            baseX /= baseLen;
            baseY /= baseLen;
        } else {
            baseX = toPlayerX;
            baseY = toPlayerY;
        }


        // 3. Separation (local crowding)
        double sepX = 0, sepY = 0;
        for (Enemy other : nearby) {
            if (other == this) continue;

            double rx = wrappedDelta(worldX, other.worldX, Constants.MAP_WIDTH);
            double ry = wrappedDelta(worldY, other.worldY, Constants.MAP_HEIGHT);
            double d = Math.sqrt(rx*rx + ry*ry);

            if (d > 0 && d < desiredSpacing) {
                double push = (desiredSpacing - d) / desiredSpacing;
                sepX += (rx / d) * push;
                sepY += (ry / d) * push;
            }
        }

        // 4. Orbit + noise
        double orbitX = 0, orbitY = 0;
        if (distToPlayer < orbitRadius) {
            orbitX = -toPlayerY;
            orbitY =  toPlayerX;
        }

        double noiseX = (Math.random() * 2 - 1) * noiseStrength;
        double noiseY = (Math.random() * 2 - 1) * noiseStrength;

        // 5. Combine steering
        double vx =
            baseX * flowWeight +
            sepX  * separationStrength +
            orbitX * orbitStrength +
            noiseX;

        double vy =
            baseY * flowWeight +
            sepY  * separationStrength +
            orbitY * orbitStrength +
            noiseY;

        // HARD CONSTRAINT: don't move away from player
        double dotToPlayer = vx * toPlayerX + vy * toPlayerY;
        if (dotToPlayer < 0) {
            double blend = 0.7;
            vx = vx * (1.0 - blend) + toPlayerX * blend;
            vy = vy * (1.0 - blend) + toPlayerY * blend;
        }

        double len = Math.sqrt(vx*vx + vy*vy);
        if (len > 0) {
            vx /= len;
            vy /= len;
        }

        // -----------------------------------------
        // 6. Local obstacle avoidance as steering
        // -----------------------------------------
        double lookAhead = Math.max(30, speed * 12);
        double radius = getColliderRadius();
        double sideOffset = radius * 1.2;

        double aheadX = worldX + vx * lookAhead;
        double aheadY = worldY + vy * lookAhead;

        boolean hitAhead = world.collidesCircle(aheadX, aheadY, radius);

        if (hitAhead) {
            // try left/right steering
            double perpX = -vy;
            double perpY =  vx;

            double leftX  = worldX + perpX * sideOffset;
            double leftY  = worldY + perpY * sideOffset;
            double rightX = worldX - perpX * sideOffset;
            double rightY = worldY - perpY * sideOffset;

            boolean leftBlocked  = world.collidesCircle(leftX,  leftY,  radius);
            boolean rightBlocked = world.collidesCircle(rightX, rightY, radius);

            double avoidStrength = 0.8;

            if (!leftBlocked && rightBlocked) {
                vx += perpX * avoidStrength;
                vy += perpY * avoidStrength;
            } else if (!rightBlocked && leftBlocked) {
                vx -= perpX * avoidStrength;
                vy -= perpY * avoidStrength;
            } else {
                // both free or both blocked: pick side that still goes toward player
                double leftDot  = (vx + perpX) * toPlayerX + (vy + perpY) * toPlayerY;
                double rightDot = (vx - perpX) * toPlayerX + (vy - perpY) * toPlayerY;
                if (leftDot > rightDot) {
                    vx += perpX * avoidStrength;
                    vy += perpY * avoidStrength;
                } else {
                    vx -= perpX * avoidStrength;
                    vy -= perpY * avoidStrength;
                }
            }

            // renormalize and re‑enforce "toward player"
            len = Math.sqrt(vx*vx + vy*vy);
            if (len > 0) {
                vx /= len;
                vy /= len;
            }
            dotToPlayer = vx * toPlayerX + vy * toPlayerY;
            if (dotToPlayer < 0) {
                vx = toPlayerX;
                vy = toPlayerY;
            }
        }


        // -----------------------------------------
        // 7. Stuck detection + small safe nudge
        // -----------------------------------------
        double movedDist = Math.hypot(worldX - lastX, worldY - lastY);
        if (movedDist < 0.2) {
            stuckFrames++;
        } else {
            stuckFrames = 0;
        }

        lastX = worldX;
        lastY = worldY;

        if (stuckFrames > 25) {
            // try a few directions that still have positive dot to player
            double bestX = toPlayerX;
            double bestY = toPlayerY;

            for (int i = 0; i < 12; i++) {
                double angle = (Math.PI * 2.0 * i) / 12.0;
                double cx = Math.cos(angle);
                double cy = Math.sin(angle);

                if (cx * toPlayerX + cy * toPlayerY <= 0) continue; // don't go backwards

                double nx = worldX + cx * radius * 2.5;
                double ny = worldY + cy * radius * 2.5;

                if (!world.collidesCircle(nx, ny, radius)) {
                    bestX = cx;
                    bestY = cy;
                    break;
                }
            }

            double nx2 = worldX + bestX * radius * 2.0;
            double ny2 = worldY + bestY * radius * 2.0;
            if (!world.collidesCircle(nx2, ny2, radius)) {
                worldX = nx2;
                worldY = ny2;
            }

            stuckFrames = 0;
        }


        // -----------------------------------------
        // 8. Apply movement with simple sliding
        // -----------------------------------------
        double tryX = WorldManager.wrapX(worldX + vx * speed);
        double tryY = WorldManager.wrapY(worldY + vy * speed);

        boolean canMoveX = !world.collidesCircle(tryX, worldY, radius);
        boolean canMoveY = !world.collidesCircle(worldX, tryY, radius);

        if (canMoveX) worldX = tryX;
        if (canMoveY) worldY = tryY;

        if (!canMoveX && !canMoveY) {
            // slide along perpendicular, but still prefer toward player
            double perpX = -vy;
            double perpY =  vx;

            double slideX1 = WorldManager.wrapX(worldX + perpX * speed);
            double slideY1 = WorldManager.wrapY(worldY + perpY * speed);
            double slideX2 = WorldManager.wrapX(worldX - perpX * speed);
            double slideY2 = WorldManager.wrapY(worldY - perpY * speed);

            boolean s1Free = !world.collidesCircle(slideX1, slideY1, radius);
            boolean s2Free = !world.collidesCircle(slideX2, slideY2, radius);

            if (s1Free || s2Free) {
                double d1 = (slideX1 - worldX) * toPlayerX + (slideY1 - worldY) * toPlayerY;
                double d2 = (slideX2 - worldX) * toPlayerX + (slideY2 - worldY) * toPlayerY;

                if (s1Free && (!s2Free || d1 >= d2)) {
                    worldX = slideX1;
                    worldY = slideY1;
                } else {
                    worldX = slideX2;
                    worldY = slideY2;
                }
            }
        }

        updateFacingDirection(toPlayerX);
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