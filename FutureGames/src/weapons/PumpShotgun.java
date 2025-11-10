package weapons;
import entities.Projectile;

public class PumpShotgun extends Weapon {

    public PumpShotgun(ProjectileSpawner spawner) {
        super(new WeaponStats(8, 30, 400, 50, 1, 2, 2500), spawner);
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        int pellets = 6;
        for (int i = 0; i < pellets; i++) {
            double pelletAngle = angle + Math.toRadians((Math.random() - 0.5) * stats.spread);
            Projectile p = new Projectile(x, y, pelletAngle, stats.projectileSpeed, stats.range, stats.damage);
            spawner.spawn(p);
        }
    }
}

