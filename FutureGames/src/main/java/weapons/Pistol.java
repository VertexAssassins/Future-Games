package weapons;
import entities.Projectile;
import utils.Sound;
import entities.Player;
import core.GameWorld;

public class Pistol extends Weapon {
    public Pistol(ProjectileSpawner spawner, Player player, GameWorld world) {
        super(new WeaponStats(10, 10, 400, 10, 1, 1, 2000, "pistol"), spawner, player, world);

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
            Projectile p = new Projectile(spawnX, spawnY, pelletAngle, stats.projectileSpeed, stats.range, stats.damage, player, world);
            spawner.spawn(p);
        }
    }

    @Override
    public void applyPermanentUpgrade() {
        // +50% damage
        stats.damage = stats.baseDamage * 1.5;
        //incraese ammo to 3
        stats.maxAmmo = 3;
        //increase reload speed by 25%
        stats.reloadTime = (long)(stats.baseReloadTime * 0.75);
        //increase rate of fire by 25%
        stats.rateOfFire = (long)(stats.baseRateOfFire * 1.25);
    }
}