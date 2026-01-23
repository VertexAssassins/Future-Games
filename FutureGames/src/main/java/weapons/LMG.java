package weapons;
import entities.Projectile;
import utils.Sound;
import entities.Player;
import core.GameWorld;

public class LMG extends Weapon {
    public LMG(ProjectileSpawner spawner, Player player, GameWorld world) {
        super(new WeaponStats(20, 25, 800, 40, 25, 100, 5000, "lmg"), spawner, player, world);

        setFireSound(new Sound("/player/lmg/lmgShoot.wav"));
        setReloadSound(new Sound("/player/lmg/lmgReload.wav"));
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
        // +25% damage
        stats.damage = stats.baseDamage * 1.5;
        //incraese ammo to 150
        stats.maxAmmo = 150;
        //reduce spread to 15
        stats.spread = 15;
        //increase reload speed by 25%
        stats.reloadTime = (long)(stats.baseReloadTime * 0.5);
    }
}