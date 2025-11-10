package weapons;

public class WeaponFactory {
    public static Weapon create(WeaponType type, ProjectileSpawner spawner) {
        return switch (type) {
            case HANDGUN -> new Handgun(spawner);
            case SMG -> new SMG(spawner);
            case SHOTGUN -> new PumpShotgun(spawner);
        };
    }
}