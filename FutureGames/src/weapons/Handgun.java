package weapons;
import entities.Projectile;

public class Handgun extends Weapon {
    public Handgun(ProjectileSpawner spawner) {
        super(new WeaponStats(10, 10, 600, 25, 1, 6, 3000), spawner);
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        spawner.spawn(new Projectile(x, y, angle, stats.projectileSpeed, stats.range, stats.damage));
    }
}