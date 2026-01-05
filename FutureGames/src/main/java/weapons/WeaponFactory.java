package weapons;

public class WeaponFactory {
    public static Weapon create(WeaponType type, ProjectileSpawner spawner) {
        return switch (type) {
            case HANDGUN -> new Handgun(spawner);
            case REVOLVER -> new Revolver(spawner);
            case SHOTGUN -> new PumpShotgun(spawner);
            case SMG -> new SMG(spawner);
            case ASSAULTRIFLE -> new AssaultRifle(spawner);
            case AUTOSHOTGUN -> new AutoShotgun(spawner);
            case MINIGUN -> new Minigun(spawner);
        };
    }
}