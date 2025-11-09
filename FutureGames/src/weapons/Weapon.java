package weapons;

import java.awt.Point;

public abstract class Weapon {
    protected double baseDamage;
    protected double damage;
    protected double spread;       // in degrees
    protected double range;
    protected double projectileSpeed;
    protected double rateOfFire;   // bullets per second
    protected int maxAmmo;
    protected int ammo;
    protected long reloadTime;     // milliseconds
    protected long lastShotTime;
    protected boolean reloading = false;
    protected long reloadStartTime;

    // Getters for UI, shooting logic, debugging
    public double getDamage() { return damage; }
    public double getSpread() { return spread; }            // in degrees
    public double getRange() { return range; }
    public double getProjectileSpeed() { return projectileSpeed; }
    public double getRateOfFire() { return rateOfFire; }
    public int getMaxAmmo() { return maxAmmo; }
    public long getReloadTime() { return reloadTime; }


    public Weapon(double baseDamage, double spread, double range, double projectileSpeed,
                  double rateOfFire, int maxAmmo, long reloadTime) {
        this.baseDamage = baseDamage;
        this.damage = baseDamage;
        this.spread = spread;
        this.range = range;
        this.projectileSpeed = projectileSpeed;
        this.rateOfFire = rateOfFire;
        this.maxAmmo = maxAmmo;
        this.ammo = maxAmmo;
        this.reloadTime = reloadTime;
        this.lastShotTime = 0;
    }

    /** Call this every game tick to handle reloads */
    public void update() {
        if (reloading && System.currentTimeMillis() - reloadStartTime >= reloadTime) {
            ammo = maxAmmo;
            reloading = false;
        }
    }

    /** Attempt to fire a projectile toward target (mouse) */
    public boolean tryFire(double playerX, double playerY, Point mouse) {
        long now = System.currentTimeMillis();

        // Check fire rate
        if (now - lastShotTime < 1000 / rateOfFire) return false;

        if (ammo <= 0) {
            reload();
            return false;
        }

        // Fire projectile
        double angleToMouse = Math.atan2(mouse.y - playerY, mouse.x - playerX);

        // Apply random spread
        double halfSpreadRad = Math.toRadians(spread / 2.0);
        double randomOffset = (Math.random() * halfSpreadRad * 2) - halfSpreadRad;
        double finalAngle = angleToMouse + randomOffset;

        spawnProjectile(playerX, playerY, finalAngle);

        ammo--;
        lastShotTime = now;

        if (ammo <= 0) reload();
        return true;
    }

    /** Simulate spawning a projectile — override to integrate with your game world */
    protected abstract void spawnProjectile(double x, double y, double angle);

    /** Start reload */
    public void reload() {
        if (!reloading) {
            reloading = true;
            reloadStartTime = System.currentTimeMillis();
        }
    }

    /** Apply global modifier to all variables (e.g., +20% damage) */
    public void applyModifier(double damageMult, double spreadMult, double rangeMult,
                              double speedMult, double rateMult, double ammoMult, double reloadMult) {
        damage = baseDamage * damageMult;
        spread *= spreadMult;
        range *= rangeMult;
        projectileSpeed *= speedMult;
        rateOfFire *= rateMult;
        maxAmmo = (int)(maxAmmo * ammoMult);
        reloadTime = (long)(reloadTime * reloadMult);
    }

    // Getters for UI, debugging
    public int getAmmo() { return ammo; }
    public boolean isReloading() { return reloading; }
}
