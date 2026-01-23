package entities;

import java.awt.*;
import core.GameWorld;

public class Brute extends Enemy {
    public Brute(double worldX, double worldY, GameWorld world) {
        super(
            worldX, worldY,
            10,     // desiredSpacing (almost none)
            0.02,   // separationStrength (barely avoids others)
            0.0,    // noiseStrength (no randomness)
            20,     // orbitRadius (must be VERY close to orbit)
            0.0,    // orbitStrength (never orbit)
            3.0,     // chaseStrength (extremely aggressive)
            world
        );

        loadRandomSprite("/enemies/brute/", "025");

        baseSpeed = 2.1;
        baseDamage = 20;
        baseHealth = 100.0;
        baseAttackCooldown = 1200;
        burstCount = 2;
        basePoints = 8;
        size = 125;
        color = Color.ORANGE;

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