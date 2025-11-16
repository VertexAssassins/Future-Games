package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import utils.Constants;

public class Player {
    private double x = 300, y = 200;
    private final int size = 80;
    private double maxHealth;

    private float hitOverlayAlpha = 0f;
    private final float maxOverlayAlpha = 0.8f;
    private final float overlayFadeSpeed = 0.1f; // fade per frame

    // Base stats
    private double baseSpeed = 4.0;
    private double baseHealth = 100.0;

    private long lastHitTime = 0;
    private long damageCooldown = 100; // milliseconds
    private Color color = Color.GREEN;

    //Effective stats
    private double speed;
    private double health;

    // Movement
    private boolean up, down, left, right;

    private double knockbackVX = 0;
    private double knockbackVY = 0;
    private double knockbackDecay = 0.85; // decay factor per frame

    private BufferedImage sprite;

    public Player() {
        applyModifiers(1.0, 1.0); // default: no modifiers

        try {
            sprite = ImageIO.read(getClass().getResource("/assets/player/handgun/player.png"));
        } catch (IOException e) {
            e.printStackTrace();
            // fallback: keep rectangle
            sprite = null;
        }
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void applyModifiers(double speedMult, double healthMult) {
        speed = baseSpeed * speedMult;
        health = baseHealth * healthMult;
        maxHealth = baseHealth * healthMult;
    }

    public void takeDamage(double amount) {
    long now = System.currentTimeMillis();
        if (now - lastHitTime >= damageCooldown) {
            health -= amount;
            hitOverlayAlpha = maxOverlayAlpha;
            if (health < 0) health = 0;
            lastHitTime = now;
            color = Color.RED; // flash red on hit
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public double getHealth() { return health; }

    public void update() {
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

            x += normX * speed;
            y += normY * speed;
        }

        // Apply knockback velocity
        x += knockbackVX;
        y += knockbackVY;

        // Decay knockback velocity
        knockbackVX *= knockbackDecay;
        knockbackVY *= knockbackDecay;

        if (Math.abs(knockbackVX) < 0.1) knockbackVX = 0;
        if (Math.abs(knockbackVY) < 0.1) knockbackVY = 0;

        x = core.WorldManager.wrapX(x);
        y = core.WorldManager.wrapY(y);
    }


    public void draw(Graphics g, core.CameraManager camera) {
        int drawX = Constants.SCREEN_WIDTH / 2;
        int drawY = Constants.SCREEN_HEIGHT / 2;

        if (sprite != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Draw base sprite
            g2d.drawImage(sprite, drawX - size / 2, drawY - size / 2, size, size, null);

            // Apply red tint only to non-transparent pixels
            if (hitOverlayAlpha > 0f) {
                BufferedImage tinted = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D tg = tinted.createGraphics();

                // Draw sprite into buffer
                tg.drawImage(sprite, 0, 0, size, size, null);

                // Set red tint with alpha
                tg.setComposite(AlphaComposite.SrcAtop.derive(hitOverlayAlpha));
                tg.setColor(Color.RED);
                tg.fillRect(0, 0, size, size);
                tg.dispose();

                // Draw tinted sprite
                g2d.drawImage(tinted, drawX - size / 2, drawY - size / 2, null);
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
        return size * 0.5; // or 0.55 if you want a slight buffer
    }
}