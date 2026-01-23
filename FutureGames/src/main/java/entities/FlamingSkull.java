package entities;

import java.awt.*;
import core.GameWorld;

public class FlamingSkull extends Enemy {
    public FlamingSkull(double worldX, double worldY, GameWorld world) {
        super(
            worldX, worldY,
            5,      // desiredSpacing (none)
            0.0,    // separationStrength (no avoidance)
            0.0,    // noiseStrength (perfect straight line)
            10,     // orbitRadius (must be touching to orbit)
            0.0,    // orbitStrength (never orbit)
            4.0,    // chaseStrength (hyper-aggressive)
            world
        );

        loadRandomSprite("/enemies/flamingSkull/", "008");

        baseSpeed = 2.5;
        baseDamage = 5;
        baseHealth = 10.0;
        baseAttackCooldown = 1500;
        burstCount = 1;
        basePoints = 3;
        size = 50;
        color = Color.RED;

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