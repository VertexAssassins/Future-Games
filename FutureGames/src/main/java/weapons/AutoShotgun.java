package weapons;
import entities.Projectile;
import utils.Sound;

public class AutoShotgun extends Weapon {

    public AutoShotgun(ProjectileSpawner spawner) {
        super(new WeaponStats(15, 45, 400, 15, 5, 8, 4000), spawner);

        setFireSound(new Sound("/player/autoshotgun/shotgunShoot.wav"));
        setReloadSound(new Sound("/player/autoshotgun/autoshotgunReload.wav"));
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        double offset = 10.0;
        double spawnX = x + Math.cos(angle) * offset;
        double spawnY = y + Math.sin(angle) * offset;

        int pellets = 10;
        for (int i = 0; i < pellets; i++) {
            double pelletAngle = angle + Math.toRadians((Math.random() - 0.5) * stats.spread);
            Projectile p = new Projectile(spawnX, spawnY, pelletAngle, stats.projectileSpeed, stats.range, stats.damage);
            spawner.spawn(p);
        }
    }
}

