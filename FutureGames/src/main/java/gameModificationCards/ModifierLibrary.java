package gameModificationCards;

import java.util.Map;

public class ModifierLibrary {

    public static final Map<ModifierType, double[]> GOOD_VALUES = Map.of(
        ModifierType.DAMAGE_MULT,      new double[]{2, 5, 25},
        ModifierType.MAX_HEALTH,       new double[]{2, 5, 15},
        ModifierType.AMMO_CAPACITY,    new double[]{2, 5, 25},
        ModifierType.FIRE_RATE,        new double[]{2, 5, 15},
        ModifierType.MOVE_SPEED,       new double[]{2, 5, 15},
        ModifierType.POINTS_GAINED,    new double[]{2, 5, 10}
    );

    public static final Map<ModifierType, double[]> BAD_VALUES = Map.of(
        ModifierType.ENEMY_DAMAGE,     new double[]{2, 5, 10},
        ModifierType.ENEMY_HEALTH,     new double[]{2, 5, 10},
        ModifierType.ENEMY_SPEED,      new double[]{2, 5, 10}
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
