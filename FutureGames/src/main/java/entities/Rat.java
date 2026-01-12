package entities;

import java.awt.*;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Rat extends Enemy {
    public Rat(double worldX, double worldY) {
        super(worldX, worldY);

        try {
                setSprite(ImageIO.read(getClass().getResource("/enemies/rat/rat.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        baseSpeed = 2.5;
        baseDamage = 1;
        baseHealth = 1.0;
        baseAttackCooldown = 500;
        burstCount = 1;
        basePoints = 1;
        size = 25;
        color = Color.GREEN;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0, 1); // no scaling, just use new base values
    }
}