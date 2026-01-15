package weapons;

import java.awt.Point;

import entities.Player;
import utils.Sound;

public abstract class Weapon {
    protected final ProjectileSpawner spawner;
    protected final WeaponStats stats;
    protected final Player player;
    protected int ammo;
    protected long lastShotTime;
    protected boolean reloading = false;
    protected long reloadStartTime;
    protected Sound fireSound;
    protected Sound reloadSound;

    public Weapon(WeaponStats stats, ProjectileSpawner spawner, Player player) {
        this.stats = stats;
        this.spawner = spawner;
        this.player = player;
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

        if (fireSound != null) {
            float pitch = 1.0f + (float)(Math.random() * 0.1 - 0.05);
            fireSound.play(pitch);
        }

        if (ammo <= 0) reload();
        return true;
    }

    protected abstract void spawnProjectile(double x, double y, double angle);

    public void reload() {
        if (!reloading) {
            reloading = true;
            reloadStartTime = System.currentTimeMillis();

            if (reloadSound != null) {
                float reloadTimeSeconds = stats.reloadTime / 1000f;
                float soundDuration = reloadSound.getDurationSeconds();

                if (reloadTimeSeconds > 0 && soundDuration > 0) {
                    float pitch = soundDuration / reloadTimeSeconds;
                    reloadSound.play(pitch);
                } else {
                    reloadSound.play(); // fallback
                }
            }
        }
    }

    public boolean isReloading() { return reloading; }
    public int getAmmo() { return ammo; }
    public WeaponStats getStats() { return stats; }
    public void setFireSound(Sound sound) { this.fireSound = sound; }
    public void setReloadSound(Sound sound) { this.reloadSound = sound; }
}