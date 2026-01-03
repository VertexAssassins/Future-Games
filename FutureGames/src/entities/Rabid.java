package entities;

import java.awt.*;

public class Rabid extends Enemy {
    public Rabid(double worldX, double worldY) {
        super(worldX, worldY);
        baseSpeed = 3;
        baseDamage = 25;
        baseHealth = 50.0;
        baseAttackCooldown = 600;
        burstCount = 3;
        basePoints = 5;
        size = 30;
        color = Color.BLUE;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0, 1); // no scaling, just use new base values
    }
}