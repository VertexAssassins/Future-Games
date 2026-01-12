package entities;

import java.awt.*;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Brute extends Enemy {
    public Brute(double worldX, double worldY) {
        super(worldX, worldY);

        try {
                setSprite(ImageIO.read(getClass().getResource("/enemies/brute/brute.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        baseSpeed = 1;
        baseDamage = 20;
        baseHealth = 100.0;
        baseAttackCooldown = 1200;
        burstCount = 2;
        basePoints = 8;
        size = 125;
        color = Color.ORANGE;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0, 1); // no scaling, just use new base values
    }
}