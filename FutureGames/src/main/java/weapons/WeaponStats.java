package weapons;

public class WeaponStats {
    // current (mutable) stats
    public double damage;
    public double spread;
    public double range;
    public double projectileSpeed;
    public double rateOfFire;
    public int maxAmmo;
    public long reloadTime;

    // base (immutable) stats for reset
    public final double baseDamage;
    public final double baseSpread;
    public final double baseRange;
    public final double baseProjectileSpeed;
    public final double baseRateOfFire;
    public final int    baseMaxAmmo;
    public final long   baseReloadTime;

    public WeaponStats(double damage, double spread, double range, double projectileSpeed,
                       double rateOfFire, int maxAmmo, long reloadTime) {

        // store base values
        this.baseDamage = damage;
        this.baseSpread = spread;
        this.baseRange = range;
        this.baseProjectileSpeed = projectileSpeed;
        this.baseRateOfFire = rateOfFire;
        this.baseMaxAmmo = maxAmmo;
        this.baseReloadTime = reloadTime;

        // initialize current values from base
        this.damage = damage;
        this.spread = spread;
        this.range = range;
        this.projectileSpeed = projectileSpeed;
        this.rateOfFire = rateOfFire;
        this.maxAmmo = maxAmmo;
        this.reloadTime = reloadTime;
    }

    public void resetToBase() {
        this.damage = baseDamage;
        this.spread = baseSpread;
        this.range = baseRange;
        this.projectileSpeed = baseProjectileSpeed;
        this.rateOfFire = baseRateOfFire;
        this.maxAmmo = baseMaxAmmo;
        this.reloadTime = baseReloadTime;
    }
}