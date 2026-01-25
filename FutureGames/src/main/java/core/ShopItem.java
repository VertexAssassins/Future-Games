package core;

import java.awt.Image;

public class ShopItem {

    public enum Type {
        WEAPON,
        SPECIAL
    }

    public final String weaponId;
    public final String name;
    public final int price;
    public final int upgradeCost;
    public Image icon;
    public long errorMessageUntil = 0;
    public Type type;

    public ShopItem(String weaponId, String name, int price, int upgradeCost) {
        this.type = Type.WEAPON;
        this.weaponId = weaponId;
        this.name = name;
        this.price = price;
        this.upgradeCost = upgradeCost;
    }

    public ShopItem(Type type, String id, String name, int price) {
        this.type = type;
        this.weaponId = id;
        this.name = name;
        this.price = price;
        this.upgradeCost = 0;
    }

}
