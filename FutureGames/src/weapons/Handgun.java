package weapons;

import java.awt.Point;
import entities.Projectile;

public class Handgun extends Weapon {

    public Handgun() {
        super(10, 10, 600, 12, 3, 12, 1000); 
        // damage=10, spread=10°, range=600, speed=12 px/tick, 3 shots/sec, 12 bullets, 1s reload
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        Projectile p = new Projectile(x, y, angle, projectileSpeed, range, damage);
        System.out.println("Handgun shot at angle: " + Math.toDegrees(angle));
    }
}
