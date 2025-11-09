package entities;

import java.awt.*;

public class TankEnemy extends Enemy {
    public TankEnemy(double worldX, double worldY) {
        super(worldX, worldY);
        baseSpeed = .8;
        baseDamage = 2.5;
        baseHealth = 2.0;
        baseAttackCooldown = 1200;
        burstCount = 2;
        size = 60;
        color = Color.ORANGE;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0);
    }
}