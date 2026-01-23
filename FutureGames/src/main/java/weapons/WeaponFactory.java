package weapons;

import entities.Player;
import core.GameWorld;

public class WeaponFactory {
    public static Weapon create(WeaponType type, ProjectileSpawner spawner, Player player, GameWorld world) {
        return switch (type) {
            case PISTOL -> new Pistol(spawner, player, world);
            case REVOLVER -> new Revolver(spawner, player, world);
            case SHOTGUN -> new PumpShotgun(spawner, player, world);
            case SMG -> new SMG(spawner, player, world);
            case ASSAULTRIFLE -> new AssaultRifle(spawner, player, world);
            case AUTOSHOTGUN -> new AutoShotgun(spawner, player, world);
            case LMG-> new LMG(spawner, player, world);
        };
    }
}