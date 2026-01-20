package entities;

import java.awt.*;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FlamingSkull extends Enemy {
    public FlamingSkull(double worldX, double worldY) {
        super(
            worldX, worldY,
            5,      // desiredSpacing (none)
            0.0,    // separationStrength (no avoidance)
            0.0,    // noiseStrength (perfect straight line)
            10,     // orbitRadius (must be touching to orbit)
            0.0,    // orbitStrength (never orbit)
            4.0     // chaseStrength (hyper-aggressive)
        );

        try {
                setSprite(ImageIO.read(getClass().getResource("/enemies/flamingSkull/flamingSkull.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        baseSpeed = 2.5;
        baseDamage = 5;
        baseHealth = 10.0;
        baseAttackCooldown = 1500;
        burstCount = 1;
        basePoints = 3;
        size = 40;
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