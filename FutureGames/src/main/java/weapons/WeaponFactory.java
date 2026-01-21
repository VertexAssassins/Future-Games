package weapons;

import entities.Player;

public class WeaponFactory {
    public static Weapon create(WeaponType type, ProjectileSpawner spawner, Player player) {
        return switch (type) {
            case PISTOL -> new Pistol(spawner, player);
            case REVOLVER -> new Revolver(spawner, player);
            case SHOTGUN -> new PumpShotgun(spawner, player);
            case SMG -> new SMG(spawner, player);
            case ASSAULTRIFLE -> new AssaultRifle(spawner, player);
            case AUTOSHOTGUN -> new AutoShotgun(spawner, player);
            case LMG-> new LMG(spawner, player);
        };
    }
}