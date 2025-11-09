package weapons;

import java.awt.Point;
import entities.Projectile;

public class SMG extends Weapon {

    public SMG() {
        super(6, 15, 500, 14, 10, 30, 1500); 
        // smaller damage, higher rate of fire, wider spread, bigger clip
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        Projectile p = new Projectile(x, y, angle, projectileSpeed, range, damage);
        System.out.println("SMG shot at angle: " + Math.toDegrees(angle));
    }
}
