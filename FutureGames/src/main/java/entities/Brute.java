package entities;

import java.awt.*;

public class Brute extends Enemy {
    public Brute(double worldX, double worldY) {
        super(worldX, worldY);
        baseSpeed = 1;
        baseDamage = 20;
        baseHealth = 100.0;
        baseAttackCooldown = 1200;
        burstCount = 2;
        basePoints = 8;
        size = 60;
        color = Color.ORANGE;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0, 1); // no scaling, just use new base values
    }
}