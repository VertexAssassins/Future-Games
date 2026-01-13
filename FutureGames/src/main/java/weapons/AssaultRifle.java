package weapons;
import entities.Projectile;
import utils.Sound;

public class AssaultRifle extends Weapon {
    public AssaultRifle(ProjectileSpawner spawner) {
        super(new WeaponStats(10, 10, 800, 20, 7.5, 45, 2000), spawner);

        setFireSound(new Sound("/player/assaultrifle/assaultrifleShoot.wav"));
        setReloadSound(new Sound("/player/assaultrifle/assaultrifleReload.wav"));
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        double offset = 10.0;
        double spawnX = x + Math.cos(angle) * offset;
        double spawnY = y + Math.sin(angle) * offset;

        int pellets = 1;
        for (int i = 0; i < pellets; i++) {
            double pelletAngle = angle + Math.toRadians((Math.random() - 0.5) * stats.spread);
            Projectile p = new Projectile(spawnX, spawnY, pelletAngle, stats.projectileSpeed, stats.range, stats.damage);
            spawner.spawn(p);
        }
    }
}