package weapons;
import entities.Projectile;
import utils.Sound;
import entities.Player;
import core.GameWorld;

public class SMG extends Weapon {
    public SMG(ProjectileSpawner spawner, Player player, GameWorld world) {
        super(new WeaponStats(5, 25, 800, 15, 10, 30, 1500, "smg"), spawner, player, world);

        setFireSound(new Sound("/player/smg/smgShoot.wav"));
        setReloadSound(new Sound("/player/smg/smgReload.wav"));
    }

    @Override
    protected void spawnProjectile(double x, double y, double angle) {
        double offset = 10.0;
        double spawnX = x + Math.cos(angle) * offset;
        double spawnY = y + Math.sin(angle) * offset;

        int pellets = 1;
        for (int i = 0; i < pellets; i++) {
            double pelletAngle = angle + Math.toRadians((Math.random() - 0.5) * stats.spread);
            Projectile p = new Projectile(spawnX, spawnY, pelletAngle, stats.projectileSpeed, stats.range, stats.damage, player, world);
            spawner.spawn(p);
        }
    }

    @Override
    public void applyPermanentUpgrade() {
        // +50% damage
        stats.damage = stats.baseDamage * 1.5;
        //incraese ammo to 45
        stats.maxAmmo = 45;
        //reduce spread to 15
        stats.spread = 15;
        //increase reload speed by 25%
        stats.reloadTime = (long)(stats.baseReloadTime * 0.75);
    }
}