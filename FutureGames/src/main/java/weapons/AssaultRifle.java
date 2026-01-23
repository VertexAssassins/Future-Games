package weapons;
import entities.Projectile;
import utils.Sound;
import entities.Player;
import core.GameWorld;

public class AssaultRifle extends Weapon {
    public AssaultRifle(ProjectileSpawner spawner, Player player, GameWorld world) {
        super(new WeaponStats(10, 15, 800, 20, 7.5, 45, 3000, "assaultrifle"), spawner, player, world);

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
            Projectile p = new Projectile(spawnX, spawnY, pelletAngle, stats.projectileSpeed, stats.range, stats.damage, player, world);
            spawner.spawn(p);
        }
    }

    @Override
    public void applyPermanentUpgrade() {
        // +50% damage
        stats.damage = stats.baseDamage * 1.5;
        //incraese ammo to 60
        stats.maxAmmo = 60;
        //reduce spread to 7.5
        stats.spread = 7.5;
        //increase reload speed by 50%
        stats.reloadTime = (long)(stats.baseReloadTime * 0.5);
    }
}