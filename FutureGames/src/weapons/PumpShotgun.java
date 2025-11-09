package weapons;

import java.awt.Point;
import entities.Projectile;

public class PumpShotgun extends Weapon {

    public PumpShotgun() {
        super(8, 40, 400, 10, 1, 5, 2000); 
        // high spread, low ROF, few bullets per reload
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        int pellets = 6;
        for (int i = 0; i < pellets; i++) {
            double pelletAngle = angle + Math.toRadians((Math.random() - 0.5) * spread);
            Projectile p = new Projectile(x, y, pelletAngle, projectileSpeed, range, damage);
            System.out.println("Shotgun pellet at angle: " + Math.toDegrees(pelletAngle));
        }
    }
}
