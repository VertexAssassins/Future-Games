package entities;

import java.awt.*;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Rabid extends Enemy {
    public Rabid(double worldX, double worldY) {
        super(worldX, worldY);

        try {
                setSprite(ImageIO.read(getClass().getResource("/enemies/rabid/rabid.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        baseSpeed = 3;
        baseDamage = 25;
        baseHealth = 50.0;
        baseAttackCooldown = 600;
        burstCount = 3;
        basePoints = 5;
        size = 50;
        color = Color.BLUE;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0, 1); // no scaling, just use new base values
    }
}