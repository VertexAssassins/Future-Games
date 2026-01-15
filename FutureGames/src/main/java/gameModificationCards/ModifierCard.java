package gameModificationCards;

public class ModifierCard {
    public final Rarity rarity;
    public final Modifier good;
    public final Modifier bad;

    public ModifierCard(Rarity rarity, Modifier good, Modifier bad) {
        this.rarity = rarity;
        this.good = good;
        this.bad = bad;
    }
}