package weapons;

public class WeaponStats {
    public double damage;
    public double spread;
    public double range;
    public double projectileSpeed;
    public double rateOfFire;
    public int maxAmmo;
    public long reloadTime;

    public WeaponStats(double damage, double spread, double range, double projectileSpeed,
                       double rateOfFire, int maxAmmo, long reloadTime) {
        this.damage = damage;
        this.spread = spread;
        this.range = range;
        this.projectileSpeed = projectileSpeed;
        this.rateOfFire = rateOfFire;
        this.maxAmmo = maxAmmo;
        this.reloadTime = reloadTime;
    }
}