package weapons;
import entities.Projectile;
import utils.Sound;
import entities.Player;

public class PumpShotgun extends Weapon {

    public PumpShotgun(ProjectileSpawner spawner, Player player) {
        super(new WeaponStats(8, 30, 400, 10, 1, 2, 2500, "pumpshotgun"), spawner, player);

        setFireSound(new Sound("/player/shotgun/shotgunShoot.wav"));
        setReloadSound(new Sound("/player/shotgun/shotgunReload.wav"));
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        double offset = 10.0;
        double spawnX = x + Math.cos(angle) * offset;
        double spawnY = y + Math.sin(angle) * offset;

        int pellets = 6;
        for (int i = 0; i < pellets; i++) {
            double pelletAngle = angle + Math.toRadians((Math.random() - 0.5) * stats.spread);
            Projectile p = new Projectile(spawnX, spawnY, pelletAngle, stats.projectileSpeed, stats.range, stats.damage, player);
            spawner.spawn(p);
        }
    }

    @Override
    public void applyPermanentUpgrade() {
        // +25% damage
        stats.damage = stats.baseDamage * 1.25;
        //incraese ammo to 4
        stats.maxAmmo = 4;
        //reduce spread to 20
        stats.spread = 20;
        //increase reload speed by 25%
        stats.reloadTime = (long)(stats.baseReloadTime * 0.75);
    }
}

