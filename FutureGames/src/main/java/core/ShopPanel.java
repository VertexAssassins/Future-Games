package core;

import java.util.List;

import javax.imageio.ImageIO;
import java.net.URL;

import java.util.ArrayList;
import java.awt.*;

import entities.Player;
import weapons.WeaponUnlockManager;

public class ShopPanel {
    private final List<ShopItem> items;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int panelX;
    private int panelY;

    private Image borderPanel;
    private Image unlockButtonImg;
    private Image upgradeButtonImg;
    private Font easyText;

    public ShopPanel() {
        items = new ArrayList<>();
        items.add(new ShopItem("pistol", "Pistol", 0, 25));       // default unlocked
        items.add(new ShopItem("revolver", "Revolver", 100, 200));
        items.add(new ShopItem("shotgun", "Shotgun", 400, 600));
        items.add(new ShopItem("smg", "SMG", 750, 1000));
        items.add(new ShopItem("assaultrifle", "Assault Rifle", 1500, 3000));
        items.add(new ShopItem("autoshotgun", "Auto Shotgun", 5000, 7500));
        items.add(new ShopItem("lmg", "LMG", 10000, 15000));

        for (ShopItem item : items) {
            try {
                URL url = getClass().getResource("/shopWeapons/" + item.weaponId + ".png");
                System.out.println("Loading " + item.weaponId + ": " + url); // debug

                item.icon = ImageIO.read(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            borderPanel = ImageIO.read(getClass().getResource("/ui/Border Template.png"));
            unlockButtonImg = ImageIO.read(getClass().getResource("/ui/Unlock Button.png"));
            upgradeButtonImg = ImageIO.read(getClass().getResource("/ui/Upgrade Button.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            easyText = Font.createFont(Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/fonts/EASYTEXT.TTF"));
        } catch (Exception e) {
            e.printStackTrace();
            easyText = new Font("Arial", Font.BOLD, 20); // fallback
        }
    }

   public void draw(Graphics g, Player player, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;

        int panelW = 900;
        int panelH = 900;

        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        panelX = x;
        panelY = y;

        // Draw panel background
        g2.drawImage(borderPanel, x, y, panelW, panelH, null);

        // Title
        String title = "Weapon Shop";
        g2.setFont(easyText.deriveFont(Font.BOLD, 36f));

        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);

        int titleX = x + (panelW - titleWidth) / 2;
        int titleY = y + 100;

        g2.setColor(Color.WHITE);
        g2.drawString(title, titleX, titleY);

        //Ponits display
        String pointsText = "Points: " + player.getPoints();
        g2.setFont(easyText.deriveFont(Font.BOLD, 28f));

        FontMetrics fmPoints = g2.getFontMetrics();
        int pointsWidth = fmPoints.stringWidth(pointsText);

        int pointsX = x + panelW - pointsWidth - 30;
        int pointsY = y + 100;

        g2.setColor(Color.YELLOW);
        g2.drawString(pointsText, pointsX, pointsY);

        // Scrollable content area
        int contentStartY = y + 180;
        int itemY = contentStartY - scrollOffset;

        for (ShopItem item : items) {
            drawItem(g2, item, player, x + 120, itemY);
            itemY += 140;
        }

        // Update scroll limits
        int contentHeight = items.size() * 140;
        int visibleHeight = panelH - 250;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
    }

    private void drawItem(Graphics2D g, ShopItem item, Player player, int x, int y) {

        // Weapon icon
        if (item.icon != null) {
            g.drawImage(item.icon, x, y, 100, 100, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(x, y, 100, 100);
        }

        // Weapon name
        g.setColor(Color.WHITE);
        g.setFont(easyText.deriveFont(Font.BOLD, 20f));
        g.drawString(item.name, x + 130, y + 40);

        boolean unlocked = WeaponUnlockManager.isUnlocked(item.weaponId);
        boolean upgraded = PersistenceManager.loadWeaponUpgraded(item.weaponId);

        // Status text
        g.setFont(easyText.deriveFont(Font.BOLD, 22f));

        if (!unlocked) {
            g.setColor(Color.YELLOW);
            g.drawString("Price: " + item.price, x + 130, y + 80);

            // Unlock button
            g.drawImage(unlockButtonImg, x + 350, y + 20, 180, 60, null);

        } else {
            if (!upgraded) {
                g.setColor(Color.CYAN);
            g.drawString("Upgrade: " + item.upgradeCost, x + 130, y + 80);

                // Upgrade button
                g.drawImage(upgradeButtonImg, x + 350, y + 20, 180, 60, null);

            } else {
                g.setColor(Color.GREEN);
                g.drawString("Upgraded!", x + 130, y + 80);
            }
        }

        // Draw temporary error message
        if (System.currentTimeMillis() < item.errorMessageUntil) {
            g.setColor(Color.RED);
            g.setFont(easyText.deriveFont(Font.BOLD, 22f));

            String msg = "Not Enough Points";

            // Center above the button
            int msgX = x + 350 + (180 / 2) - (g.getFontMetrics().stringWidth(msg) / 2);
            int msgY = y + 15; // slightly above the button

            g.drawString(msg, msgX, msgY);
        }
    }

    public void handleScroll(int rotation) {
        scrollOffset += rotation * 30; // scroll speed

        // Clamp
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    public void handleClick(int mx, int my, Player player) {
        int y = panelY + 180 - scrollOffset;

        for (ShopItem item : items) {

            int itemX = panelX + 120;
            int buttonX = itemX + 350;

            int unlockY = y + 20;
            int upgradeY = y + 20;

            Rectangle unlockButton = new Rectangle(buttonX, unlockY, 180, 60);
            Rectangle upgradeButton = new Rectangle(buttonX, upgradeY, 180, 60);

            boolean unlocked = WeaponUnlockManager.isUnlocked(item.weaponId);
            boolean upgraded = PersistenceManager.loadWeaponUpgraded(item.weaponId);

            if (!unlocked) {
                if (unlockButton.contains(mx, my)) {
                    tryUnlock(item, player);
                }
            } else if (!upgraded) {
                if (upgradeButton.contains(mx, my)) {
                    tryUpgrade(item, player);
                }
            }

            y += 140;
        }
    }

    private void tryUnlock(ShopItem item, Player player) {
        if (WeaponUnlockManager.isUnlocked(item.weaponId)) return;

        if (player.getPoints() >= item.price) {
            player.addPoints(-item.price);
            WeaponUnlockManager.unlock(item.weaponId);
        } else {
            item.errorMessageUntil = System.currentTimeMillis() + 5000;
        }
    }

    private void tryUpgrade(ShopItem item, Player player) {
        if (!WeaponUnlockManager.isUnlocked(item.weaponId)) return;
        if (PersistenceManager.loadWeaponUpgraded(item.weaponId)) return;

        int cost = item.upgradeCost;

        if (player.getPoints() >= cost) {
            player.addPoints(-cost);
            PersistenceManager.saveWeaponUpgraded(item.weaponId, true);
        } else {
            item.errorMessageUntil = System.currentTimeMillis() + 5000; 
        }
    }
}
