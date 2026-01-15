package weapons;
import entities.Projectile;
import utils.Sound;
import entities.Player;

public class SMG extends Weapon {
    public SMG(ProjectileSpawner spawner, Player player) {
        super(new WeaponStats(5, 25, 800, 15, 10, 30, 1500), spawner, player);

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
            Projectile p = new Projectile(spawnX, spawnY, pelletAngle, stats.projectileSpeed, stats.range, stats.damage, player);
            spawner.spawn(p);
        }
    }
}