package entities;

import java.awt.*;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Rabid extends Enemy {
    public Rabid(double worldX, double worldY) {
        super(worldX, worldY);

        try {
                setSprite(ImageIO.read(getClass().getResource("/enemies/rabid/rabid.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        baseSpeed = 3;
        baseDamage = 25;
        baseHealth = 50.0;
        baseAttackCooldown = 600;
        burstCount = 3;
        basePoints = 5;
        size = 50;
        color = Color.BLUE;

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