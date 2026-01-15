package gameModificationCards;

public class RarityValues {
    public static double scale(Rarity rarity, double standard, double uncommon, double rare) {
        return switch (rarity) {
            case STANDARD -> standard;
            case UNCOMMON -> uncommon;
            case RARE -> rare;
        };
    }
}
