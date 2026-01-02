package core;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;

import entities.Player;
import weapons.WeaponUnlockManager;

public class ShopPanel {
    private final List<ShopItem> items;

    public ShopPanel() {
        items = new ArrayList<>();
        items.add(new ShopItem("pistol", "Pistol", 0));       // default unlocked
        items.add(new ShopItem("revolver", "Revolver", 1));
        items.add(new ShopItem("shotgun", "Shotgun", 1));
        items.add(new ShopItem("smg", "SMG", 1));
        items.add(new ShopItem("assaultrifle", "Assault Rifle", 1));
        items.add(new ShopItem("autoshotgun", "Auto Shotgun", 1));
        items.add(new ShopItem("minigun", "Minigun", 1));
    }

    public void draw(Graphics g, Player player, int width, int height) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Weapon Shop", width / 2 - 120, 80);

        int y = 150;

        for (ShopItem item : items) {
            drawItem(g, item, player, 100, y);
            y += 120;
        }
    }

    private void drawItem(Graphics g, ShopItem item, Player player, int x, int y) {
        // Placeholder image box
        g.setColor(Color.GRAY);
        g.fillRect(x, y, 80, 80);

        // Weapon name
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(item.name, x + 100, y + 30);

        // Price or "Unlocked"
        boolean unlocked = WeaponUnlockManager.isUnlocked(item.weaponId);

        if (unlocked) {
            g.setColor(Color.GREEN);
            g.drawString("Unlocked", x + 100, y + 60);
        } else {
            g.setColor(Color.YELLOW);
            g.drawString("Price: " + item.price, x + 100, y + 60);

            // Draw unlock button
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x + 250, y + 20, 120, 40);
            g.setColor(Color.WHITE);
            g.drawString("Unlock", x + 275, y + 48);
        }
    }

    public void handleClick(int mx, int my, Player player) {
    int y = 150;

        for (ShopItem item : items) {
            Rectangle button = new Rectangle(350, y + 20, 120, 40);

            if (button.contains(mx, my)) {
                tryUnlock(item, player);
            }

            y += 120;
        }
    }

    private void tryUnlock(ShopItem item, Player player) {
        if (WeaponUnlockManager.isUnlocked(item.weaponId)) return;

        if (player.getPoints() >= item.price) {
            player.addPoints(-item.price);
            WeaponUnlockManager.unlock(item.weaponId);
        } else {
            System.out.println("Not enough points");
        }
    }
}
