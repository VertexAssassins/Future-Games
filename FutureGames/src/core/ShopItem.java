package core;

public class ShopItem {
    public final String weaponId;
    public final String name;
    public final int price;

    public ShopItem(String weaponId, String name, int price) {
        this.weaponId = weaponId;
        this.name = name;
        this.price = price;
    }
}
