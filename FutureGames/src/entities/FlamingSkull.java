package entities;

import java.awt.*;

public class FlamingSkull extends Enemy {
    public FlamingSkull(double worldX, double worldY) {
        super(worldX, worldY);
        baseSpeed = 3;
        baseDamage = 5;
        baseHealth = 10.0;
        baseAttackCooldown = 1500;
        burstCount = 1;
        basePoints = 3;
        size = 25;
        color = Color.RED;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0, 1); // no scaling, just use new base values
    }
}