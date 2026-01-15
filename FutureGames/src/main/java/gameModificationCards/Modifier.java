package gameModificationCards;

public class Modifier {
    public final ModifierType type;
    public final double value; // already scaled by rarity

    public Modifier(ModifierType type, double value) {
        this.type = type;
        this.value = value;
    }
}
