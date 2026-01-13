package weapons;
import entities.Projectile;
import utils.Sound;

public class Handgun extends Weapon {
    public Handgun(ProjectileSpawner spawner) {
        super(new WeaponStats(10, 10, 400, 10, 1, 1, 2000), spawner);

        setFireSound(new Sound("/player/pistol/pistolShoot.wav"));
        setReloadSound(new Sound("/player/pistol/pistolReload.wav"));
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