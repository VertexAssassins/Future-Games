package weapons;

import java.awt.Point;

public abstract class Weapon {
    protected final ProjectileSpawner spawner;
    protected final WeaponStats stats;
    protected int ammo;
    protected long lastShotTime;
    protected boolean reloading = false;
    protected long reloadStartTime;

    public Weapon(WeaponStats stats, ProjectileSpawner spawner) {
        this.stats = stats;
        this.spawner = spawner;
        this.ammo = stats.maxAmmo;
    }

    public void update() {
        if (reloading && System.currentTimeMillis() - reloadStartTime >= stats.reloadTime) {
            ammo = stats.maxAmmo;
            reloading = false;
        }
    }

    public boolean tryFire(double x, double y, double angle) {
            long now = System.currentTimeMillis();
            if (now - lastShotTime < 1000 / stats.rateOfFire) return false;
            if (ammo <= 0) {
                reload();
                return false;
            }

            spawnProjectile(x, y, angle);
            ammo--;
            lastShotTime = now;
            if (ammo <= 0) reload();
            return true;
        }

    protected abstract void spawnProjectile(double x, double y, double angle);

    public void reload() {
        if (!reloading) {
            reloading = true;
            reloadStartTime = System.currentTimeMillis();
        }
    }

    public boolean isReloading() { return reloading; }
    public int getAmmo() { return ammo; }
    public WeaponStats getStats() { return stats; }
}