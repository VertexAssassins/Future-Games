package entities;

import java.awt.*;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Zombie extends Enemy {
    public Zombie(double worldX, double worldY) {
        super(
            worldX, worldY,
            60,     // desiredSpacing (wide spread)
            1.2,    // separationStrength (strong anti-clumping)
            0.05,   // noiseStrength (slow, steady shambling)
            200,    // orbitRadius (begin circling early)
            0.6,    // orbitStrength (approach from all angles)
            0.5     // chaseStrength (slow, inevitable)
        );

            try {
                setSprite(ImageIO.read(getClass().getResource("/enemies/zombie/zombie.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        baseSpeed = 1.5;
        baseDamage = 10.0;
        baseHealth = 20.0;
        baseAttackCooldown = 800;
        burstCount = 1;
        burstInterval = 150;
        basePoints = 2;
        size = 50;
        color = Color.CYAN;

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