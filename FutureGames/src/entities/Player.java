package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import utils.Constants;

public class Player {
    private double x = 300, y = 200;
    private final int size = 80;

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

    public void applyModifiers(double speedMult, double healthMult) {
        speed = baseSpeed * speedMult;
        health = baseHealth * healthMult;
    }

    public void takeDamage(double amount) {
    long now = System.currentTimeMillis();
        if (now - lastHitTime >= damageCooldown) {
            health -= amount;
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

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            double normX = dx / length;
            double normY = dy / length;

            x += normX * speed;
            y += normY * speed;
        }

        x = core.WorldManager.wrapX(x);
        y = core.WorldManager.wrapY(y);
    }


    public void draw(Graphics g, core.CameraManager camera) {
        int drawX = Constants.SCREEN_WIDTH / 2;
        int drawY = Constants.SCREEN_HEIGHT / 2;

        if (sprite != null) {
            g.drawImage(sprite, drawX - size / 2, drawY - size / 2, size, size, null);
        } else {
            g.setColor(color);
            g.fillRect(drawX - size / 2, drawY - size / 2, size, size);
        }
    }

    public void setDirection(String key, boolean pressed) {
        switch (key) {
            case "W" -> up = pressed;
            case "S" -> down = pressed;
            case "A" -> left = pressed;
            case "D" -> right = pressed;
        }
    }
}