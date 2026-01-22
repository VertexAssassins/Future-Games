package entities;

import java.awt.*;

public class Rat extends Enemy {
    public Rat(double worldX, double worldY) {
        super(
            worldX, worldY,
            5,       // desiredSpacing (extremely tight)
            -1.2,    // separationStrength (strong cohesion)
            0.02,    // noiseStrength (smooth movement)
            20,      // orbitRadius (must be touching to orbit)
            0.0,     // orbitStrength (no orbiting)
            0.6      // chaseStrength (pack movement)
        );

        loadRandomSprite("/enemies/rat/", "001");

        baseSpeed = 2.25;
        baseDamage = 1;
        baseHealth = 1.0;
        baseAttackCooldown = 500;
        burstCount = 5;
        basePoints = 1;
        size = 45;
        color = Color.GREEN;

        burstRemaining = burstCount;

        applyGlobalModifiers(
            GLOBAL_SPEED_MULT,
            GLOBAL_DAMAGE_MULT,
            GLOBAL_HEALTH_MULT,
            GLOBAL_COOLDOWN_MULT,
            GLOBAL_POINTS_MULT
        );
    }
}