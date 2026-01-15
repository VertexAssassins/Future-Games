package gameModificationCards;

import java.util.Map;

public class ModifierLibrary {

    public static final Map<ModifierType, double[]> GOOD_VALUES = Map.of(
        ModifierType.DAMAGE_MULT,      new double[]{5, 15, 50},
        ModifierType.MAX_HEALTH,       new double[]{5, 15, 50},
        ModifierType.AMMO_CAPACITY,    new double[]{5, 15, 50},
        ModifierType.FIRE_RATE,        new double[]{5, 15, 50},
        ModifierType.RELOAD_SPEED,     new double[]{5, 15, 50},
        ModifierType.MOVE_SPEED,       new double[]{5, 15, 50},
        ModifierType.POINTS_GAINED,    new double[]{5, 15, 50}
    );

    public static final Map<ModifierType, double[]> BAD_VALUES = Map.of(
        ModifierType.ENEMY_DAMAGE,     new double[]{5, 15, 50},
        ModifierType.ENEMY_HEALTH,     new double[]{5, 15, 50},
        ModifierType.ENEMY_SPEED,      new double[]{5, 15, 50}
    );

    public static Modifier create(ModifierType type, Rarity rarity) {
        double[] values = GOOD_VALUES.containsKey(type)
                ? GOOD_VALUES.get(type)
                : BAD_VALUES.get(type);

        double value = switch (rarity) {
            case STANDARD -> values[0];
            case UNCOMMON -> values[1];
            case RARE -> values[2];
        };

        return new Modifier(type, value);
    }
}
