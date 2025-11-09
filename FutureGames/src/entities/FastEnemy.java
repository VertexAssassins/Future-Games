package entities;

import java.awt.*;

public class FastEnemy extends Enemy {
    public FastEnemy(double worldX, double worldY) {
        super(worldX, worldY);
        baseSpeed = 1.5;
        baseDamage = 5.0;
        baseHealth = 50.0;
        baseAttackCooldown = 800;
        burstCount = 1;
        burstInterval = 150;
        
        size = 30;
        color = Color.CYAN;

        applyGlobalModifiers(1.0, 1.0, 1.0, 1.0); // no scaling, just use new base values
    }
}