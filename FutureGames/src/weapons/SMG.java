package weapons;
import entities.Projectile;

public class SMG extends Weapon {
    public SMG(ProjectileSpawner spawner) {
        super(new WeaponStats(5, 5, 500, 100, 10, 30, 1500), spawner);
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        spawner.spawn(new Projectile(x, y, angle, stats.projectileSpeed, stats.range, stats.damage));
    }
}