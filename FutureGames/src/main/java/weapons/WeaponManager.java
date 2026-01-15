package weapons;
import java.util.Map;

public class WeaponManager {
    private final Map<WeaponType, Weapon> weapons;
    private WeaponType current;

    public WeaponManager(Map<WeaponType, Weapon> weapons, WeaponType defaultWeapon) {
        this.weapons = weapons;
        this.current = defaultWeapon;
    }

    public void increaseAmmoCapacity(double percent) {
        for (Weapon w : weapons.values()) {
            w.getStats().maxAmmo += (int)(w.getStats().maxAmmo * (percent / 100.0));
        }
    }

    public void increaseFireRate(double percent) {
        for (Weapon w : weapons.values()) {
            w.getStats().rateOfFire *= (1 + percent / 100.0);
        }
    }

    public void switchTo(WeaponType type) {
        String id = type.name().toLowerCase();

        if (!WeaponUnlockManager.isUnlocked(id)) {
            System.out.println("Weapon locked: " + id);
            return; // do NOT switch
        }

        current = type;
    }

    public Weapon getCurrent() {
        return weapons.get(current);
    }

    public void update() {
        getCurrent().update();
    }

    public boolean tryShoot(double x, double y, double angle) {
        return getCurrent().tryFire(x, y, angle);
    }
}
