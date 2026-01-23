package core;

import java.awt.Image;

public class ShopItem {
    public final String weaponId;
    public final String name;
    public final int price;
    public final int upgradeCost;
    public Image icon;

    public long errorMessageUntil = 0;

    public ShopItem(String weaponId, String name, int price, int upgradeCost) {
        this.weaponId = weaponId;
        this.name = name;
        this.price = price;
        this.upgradeCost = upgradeCost;
    }
}
