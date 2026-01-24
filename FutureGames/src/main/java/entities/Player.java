package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import core.PersistenceManager;
import utils.Constants;
import utils.PlayerAnimationSet;
import utils.Sound;
import weapons.WeaponType;
import utils.Animation;
import core.GameWorld;

public class Player {
    private GameWorld world;
    private double x = 300, y = 200;
    private final int size = 80;
    public double maxHealth;
    private int points;
    private Sound[] hurtSounds;
    private Sound dashSound;

    private float hitOverlayAlpha = 0f;
    private final float maxOverlayAlpha = 0.8f;
    private final float overlayFadeSpeed = 0.1f; // fade per frame

    // Base stats
    private double baseSpeed = 2.0;
    public double baseHealth = 100.0;

    private long lastHitTime = 0;
    private long damageCooldown = 100; // milliseconds
    private Color color = Color.GREEN;

    //Effective stats
    private double speed;
    public double health;

    // Movement
    private boolean up, down, left, right;

    private boolean isDashing = false;
    private long dashStartTime = 0;
    // Dash charges
    private int maxDashCharges = 2;
    private int dashCharges = 2;

    private final long dashRechargeTime = 2500; // 2.5 seconds per charge
    private long lastDashUsedTime = 0;          // when the last charge was consumed
    private final long dashDuration = 200; // milliseconds
    private final double dashSpeed = 25.0;
    private double dashVX = 0;
    private double dashVY = 0;
    private double dashDecay = 0.85; // how quickly dash slows down

    // Dash charge animation (32‑frame spritesheet)
    private BufferedImage[] dashChargeFrames;  // load 32 frames
    private int dashChargeFrame = 0;           // current frame index
    private boolean playingChargeAnim = false; // whether animation is running

    // Timing
    private long chargeAnimStartTime = 0;
    private final long chargeDuration = 2500; // matches dashRechargeTime

    private double knockbackVX = 0;
    private double knockbackVY = 0;
    private double knockbackDecay = 0.85; // decay factor per frame

    private Animation currentAnimation;
    private Map<String, PlayerAnimationSet> animationSets = new HashMap<>();
    private PlayerAnimationSet currentSet;

    // --- GLOBAL PLAYER MODIFIERS (affected by cards) ---
    public double damageMultiplier = 1.0;
    public double speedMultiplier = 1.0;
    public double pointsMultiplier = 1.0;
    public double maxHealthBonus = 0.0;   // additive bonus (5/10/50)

    private boolean facingRight = true;

    public Player() {
        recalcStats();
        health = maxHealth;   // start fully healed

        points = PersistenceManager.load("points", 0) + 1000;

        loadAnimations();
        loadDashChargeFrames();
        setWeaponAnimation(WeaponType.PISTOL);
        currentAnimation = currentSet.idle;

        hurtSounds = new Sound[] {
            new Sound("/player/grunts/grunts-1.wav"),
            new Sound("/player/grunts/grunts-2.wav"),
            new Sound("/player/grunts/grunts-3.wav")
        };
        dashSound = new Sound("/player/dash/dash.wav");
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setWorld(GameWorld world) { this.world = world; }

    public void startDash() {
        // Must have a charge
        if (dashCharges <= 0) return;

        // Must not already be dashing
        if (isDashing) return;

        double dx = getMovementX();
        double dy = getMovementY();
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return; // no direction input

        dx /= length;
        dy /= length;

        dashVX = dx * dashSpeed;
        dashVY = dy * dashSpeed;

        if (Math.abs(dashVX) < 0.05) dashVX = 0;
        if (Math.abs(dashVY) < 0.05) dashVY = 0;

        isDashing = true;
        dashStartTime = System.currentTimeMillis();
        // Play sound
        if (dashSound != null) {
            dashSound.play();
        }

        // Consume a charge
        dashCharges--;
        lastDashUsedTime = System.currentTimeMillis();
    }

    public boolean isDashing() {
        return isDashing;
    }

    public BufferedImage getDashChargeFrame() {
        return dashChargeFrames[dashChargeFrame];
    }

    public int getDashCharges() {
        return dashCharges;
    }

    public int getMaxDashCharges() {
        return maxDashCharges;
    }

    private double getMovementX() {
        double dx = 0;
        if (left) dx -= 1;
        if (right) dx += 1;
        return dx;
    }

    private double getMovementY() {
        double dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        return dy;
    }

    public void recalcStats() {
        speed = baseSpeed * speedMultiplier;
        maxHealth = baseHealth + maxHealthBonus;

        // If max health increases, heal the player proportionally
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public void increaseSpeed(double percent) {
        speedMultiplier += percent / 100.0;
        recalcStats();
    }

    public void increaseMaxHealth(double amount) {
        maxHealthBonus += amount;
        recalcStats();
    }

    public void increaseDamage(double percent) {
        damageMultiplier += percent / 100.0;
    }

    public void increasePointsGained(double percent) {
        pointsMultiplier += percent / 100.0;
    }

    public void takeDamage(double amount) {
        long now = System.currentTimeMillis();
        if (now - lastHitTime >= damageCooldown) {

            health -= amount;
            hitOverlayAlpha = maxOverlayAlpha;
            if (health < 0) health = 0;

            lastHitTime = now;
            color = Color.RED;

            playRandomHurtSound();
        }
    }

    private void playRandomHurtSound() {
        int index = (int)(Math.random() * hurtSounds.length);
        hurtSounds[index].play();
    }

    public void heal(double amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public void addPoints(int amount) {
        points += (int)(amount * pointsMultiplier);
        PersistenceManager.save("points", points); // persist immediately
    }

    public boolean isAlive() {
        return health > 0;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public double getHealth() { return health; }
    public int getPoints() { return points; }
    public void resetPoints() { points = 0; }
    public GameWorld getWorld() { return world; }

    private void loadDashChargeFrames() {
        try {
            BufferedImage sheet = ImageIO.read(
                getClass().getResourceAsStream("/player/dash/dashCharge.png")
            );

            int rows = 6;
            int cols = 6;
            int totalFrames = 32;

            int frameWidth = sheet.getWidth() / cols;
            int frameHeight = sheet.getHeight() / rows;

            dashChargeFrames = new BufferedImage[totalFrames];

            int index = 0;

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {

                    if (index >= totalFrames)
                        break;

                    dashChargeFrames[index] = sheet.getSubimage(
                        x * frameWidth,
                        y * frameHeight,
                        frameWidth,
                        frameHeight
                    );

                    index++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load dash charge spritesheet!");
        }
    }

    public void update() {
        if (isDashing) {
            long now = System.currentTimeMillis();
            if (now - dashStartTime >= dashDuration) {
                isDashing = false;
                dashVX = 0;
                dashVY = 0;
            } else {
                double nextX = x + dashVX;
                double nextY = y + dashVY;

                double pr = getColliderRadius();
                
                if (!world.collidesCircle(nextX, nextY, pr)) {
                    x = nextX;
                    y = nextY;
                } else {
                    // Stop dash immediately on collision
                    isDashing = false;
                    dashVX = 0;
                    dashVY = 0;
                    return;
                }

                dashVX *= dashDecay;
                dashVY *= dashDecay;
                return;
            }
        }

        // --- DASH RECHARGE LOGIC ---
        if (dashCharges < maxDashCharges) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastDashUsedTime;

            // Each charge takes 2.5 seconds
            int chargesToRestore = (int)(elapsed / dashRechargeTime);

            if (chargesToRestore > 0) {
                dashCharges = Math.min(maxDashCharges, dashCharges + chargesToRestore);

                // Move the timer forward by the amount restored
                lastDashUsedTime += chargesToRestore * dashRechargeTime;
            }
        }

        // --- DASH CHARGE ANIMATION LOGIC ---
        if (dashCharges < maxDashCharges) {

            // Start animation if not already playing
            if (!playingChargeAnim) {
                playingChargeAnim = true;
                chargeAnimStartTime = System.currentTimeMillis();
                dashChargeFrame = 0;
            }

            // Progress animation based on time
            long now = System.currentTimeMillis();
            long elapsed = now - chargeAnimStartTime;

            // Map elapsed time to 32 frames
            double progress = Math.min(1.0, (double) elapsed / chargeDuration);
            dashChargeFrame = (int)(progress * 31); // 0–31

            // If animation finished AND a charge was restored, restart for next charge
            if (progress >= 1.0) {
                playingChargeAnim = false; // will restart next update if still not full
            }

        } else {
            // Fully charged → freeze on last frame
            playingChargeAnim = false;
            dashChargeFrame = 31;
        }

        int dx = 0, dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;

        if (hitOverlayAlpha > 0f) {
            hitOverlayAlpha -= overlayFadeSpeed;
        if (hitOverlayAlpha < 0f) hitOverlayAlpha = 0f;
        }

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            double normX = dx / length;
            double normY = dy / length;

            double nextX = x + normX * speed;
            double nextY = y + normY * speed;

            double pr = getColliderRadius();
            if (!world.collidesCircle(nextX, nextY, pr)) {
                x = nextX;
                y = nextY;
            }
        }

        // Apply knockback velocity
        double nextX = x + knockbackVX;
        double nextY = y + knockbackVY;

        double pr = getColliderRadius();

        if (!world.collidesCircle(nextX, nextY, pr)) {
            x = nextX;
            y = nextY;
        } else {
            knockbackVX = 0;
            knockbackVY = 0;
        }

        // Decay knockback velocity
        knockbackVX *= knockbackDecay;
        knockbackVY *= knockbackDecay;

        if (Math.abs(knockbackVX) < 0.1) knockbackVX = 0;
        if (Math.abs(knockbackVY) < 0.1) knockbackVY = 0;

        x = core.WorldManager.wrapX(x);
        y = core.WorldManager.wrapY(y);

        boolean moving = (up || down || left || right);

        if (moving) {
            currentAnimation = currentSet.walk;
        } else {
            currentAnimation = currentSet.idle;
        }

        currentAnimation.update();
    }

    private void loadAnimations() {
        animationSets.put("pistol", new PlayerAnimationSet(
            loadAnimation("/player/pistol/idle.png", 2, 10),
            loadAnimation("/player/pistol/walk.png", 4, 6)
        ));

        animationSets.put("revolver", new PlayerAnimationSet(
            loadAnimation("/player/revolver/idle.png", 2, 10),
            loadAnimation("/player/revolver/walk.png", 4, 6)
        ));

        animationSets.put("shotgun", new PlayerAnimationSet(
            loadAnimation("/player/shotgun/idle.png", 2, 10),
            loadAnimation("/player/shotgun/walk.png", 4, 6)
        ));

        animationSets.put("smg", new PlayerAnimationSet(
            loadAnimation("/player/smg/idle.png", 2, 10),
            loadAnimation("/player/smg/walk.png", 4, 6)
        ));

        animationSets.put("assaultrifle", new PlayerAnimationSet(
            loadAnimation("/player/assaultrifle/idle.png", 2, 10),
            loadAnimation("/player/assaultrifle/walk.png", 4, 6)
        ));

        animationSets.put("autoshotgun", new PlayerAnimationSet(
            loadAnimation("/player/autoshotgun/idle.png", 2, 10),
            loadAnimation("/player/autoshotgun/walk.png", 4, 6)
        ));

        animationSets.put("lmg", new PlayerAnimationSet(
            loadAnimation("/player/lmg/idle.png", 2, 10),
            loadAnimation("/player/lmg/walk.png", 4, 6)
        ));
    }

    public void setWeaponAnimation(WeaponType type) {
    String key = switch (type) {
        case PISTOL -> "pistol";
        case REVOLVER -> "revolver";
        case SHOTGUN -> "shotgun";
        case SMG -> "smg";
        case ASSAULTRIFLE -> "assaultrifle";
        case AUTOSHOTGUN -> "autoshotgun";
        case LMG -> "lmg"; // or "minigun" if you rename folder
    };

    PlayerAnimationSet set = animationSets.get(key);

    if (set == null) {
        System.err.println("No animation set for weapon: " + key);
        return;
    }

    currentSet = set;
    currentSet.idle.reset();
    currentSet.walk.reset();
}

    private Animation loadAnimation(String path, int frameCount, int speed) {
        try {
            BufferedImage sheet = ImageIO.read(getClass().getResource(path));
            int frameWidth = sheet.getWidth() / frameCount;
            int frameHeight = sheet.getHeight();

            BufferedImage[] frames = new BufferedImage[frameCount];

            for (int i = 0; i < frameCount; i++) {
                frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }

            return new Animation(frames, speed);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateFacingDirection(double mouseX) {
        // Player is always drawn at screen center
        double playerScreenX = Constants.SCREEN_WIDTH / 2;

        facingRight = mouseX >= playerScreenX;
    }

    public void draw(Graphics g, core.CameraManager camera) {
        int drawX = (int)(x - camera.getOffsetX());
        int drawY = (int)(y - camera.getOffsetY());

        if (currentAnimation != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            BufferedImage frame = currentAnimation.getCurrentFrame();
            int w = frame.getWidth();
            int h = frame.getHeight();

            int xTopLeft = drawX - w / 2;
            int yTopLeft = drawY - h / 2;

            if (facingRight) {
                g2d.drawImage(frame, xTopLeft, yTopLeft, null);
            } else {
                g2d.drawImage(frame,
                    xTopLeft + w, yTopLeft,
                    xTopLeft,     yTopLeft + h,
                    0, 0, w, h,
                    null
                );
            }

            // Tint overlay (also use w/h)
            if (hitOverlayAlpha > 0f) {
                BufferedImage tinted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D tg = tinted.createGraphics();

                tg.drawImage(frame, 0, 0, w, h, null);
                tg.setComposite(AlphaComposite.SrcAtop.derive(hitOverlayAlpha));
                tg.setColor(Color.RED);
                tg.fillRect(0, 0, w, h);
                tg.dispose();

                g2d.drawImage(tinted, xTopLeft, yTopLeft, null);
            }

            g2d.dispose();
        } else {
            g.setColor(color);
            g.fillRect(drawX - size / 2, drawY - size / 2, size, size);
        }
    }

    public void applyKnockback(double sourceX, double sourceY, double damage) {
        double percent = damage / maxHealth;
        double strength;

        if (percent >= 0.5) strength = 20.0;
        else if (percent >= 0.25) strength = 12.0;
        else if (percent >= 0.1) strength = 6.0;
        else strength = 3.0;

        double dx = getX() - sourceX;
        double dy = getY() - sourceY;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;

        knockbackVX = (dx / length) * strength;
        knockbackVY = (dy / length) * strength;
    }

    public void setDirection(String key, boolean pressed) {
        switch (key) {
            case "W" -> up = pressed;
            case "S" -> down = pressed;
            case "A" -> left = pressed;
            case "D" -> right = pressed;
        }
    }

    public double getColliderRadius() {
        return size * 0.15; // or 0.55 if you want a slight buffer
    }

    public void reset() {
        recalcStats();
        health = maxHealth;
        x = 300;
        y = 200;
    }
}