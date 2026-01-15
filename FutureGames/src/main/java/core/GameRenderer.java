package core;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import entities.Enemy;
import entities.HealthPickup;
import entities.Player;
import entities.Projectile;
import gameModificationCards.ModifierCard;
import gameModificationCards.ModifierType;
import gameModificationCards.Rarity;
import waves.WaveManager;
import weapons.WeaponManager;

public class GameRenderer {

    private final GamePanel panel;

    public GameRenderer(GamePanel panel) {
        this.panel = panel;
    }

    // -------------------------
    // MAIN ENTRY POINT
    // -------------------------
    public void render(Graphics g) {
        GameState state = panel.getGameState();

        switch (state) {
            case PAUSED -> drawPauseMenu(g);
            case PLAYING -> { drawGameplay(g); drawDebug((Graphics2D) g); }
            case CARD_SELECTION -> drawCardSelection(g);
            case GAME_OVER -> drawGameOver(g);
            case SHOP -> drawShop(g);
        }
    }

    // -------------------------
    // GAMEPLAY RENDERING
    // -------------------------
    private void drawGameplay(Graphics g) {
        drawPlayer(g);
        drawProjectiles(g);
        drawEnemies(g);
        drawPickups(g);
        drawHUD(g);
    }

    private void drawPlayer(Graphics g) {
        panel.getPlayer().draw(g, panel.getCamera());
    }

    private void drawProjectiles(Graphics g) {
        List<Projectile> snapshot;

        synchronized (panel.getProjectiles()) {
            snapshot = new ArrayList<>(panel.getProjectiles());
        }

        for (Projectile p : snapshot) {
            p.draw(g, panel.getCamera().getOffsetX(), panel.getCamera().getOffsetY());
        }
    }

    private void drawEnemies(Graphics g) {
        List<Enemy> snapshot;

        synchronized (panel.getEnemies()) {
            snapshot = new ArrayList<>(panel.getEnemies());
        }

        for (Enemy e : snapshot) {
            e.draw(g, panel.getCamera());
        }
    }

    private void drawPickups(Graphics g) {
        List<HealthPickup> snapshot;

        synchronized (panel.getHealthPickups()) {
            snapshot = new ArrayList<>(panel.getHealthPickups());
        }

        for (HealthPickup hp : snapshot) {
            hp.draw(g, panel.getCamera());
        }
    }

    // -------------------------
    // HUD (wave, points, health)
    // -------------------------
    private void drawHUD(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);

        WaveManager waves = panel.getWaveManager();
        Player player = panel.getPlayer();

        // Wave
        g.drawString("Wave: " + waves.getWaveNumber(), 20, 30);

        // Points
        g.drawString("Points: " + player.getPoints(), panel.getWidth() - 150, 30);

        drawHealthBar(g, player);
    }

    private void drawHealthBar(Graphics g, Player player) {
        double health = player.getHealth();
        double max = player.getMaxHealth();
        double pct = health / max;

        int x = 20, y = 60, w = 200, h = 20;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, w, h);

        g.setColor(Color.RED);
        g.fillRect(x, y, (int)(w * pct), h);

        g.setColor(Color.WHITE);
        g.drawRect(x, y, w, h);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString((int)health + " / " + (int)max, x + 60, y + 16);
    }

    // -------------------------
    // PAUSE MENU
    // -------------------------
    private void drawPauseMenu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int cx = panel.getWidth() / 2;
        int cy = panel.getHeight() / 2;

        // Dim background
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        // Menu box
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(cx - 180, cy - 180, 360, 360, 20, 20);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        g2.drawString("PAUSED", cx - 60, cy - 120);

        // Buttons
        drawButton(g2, panel.getResumeButton(), "Resume");
        drawButton(g2, panel.getPauseQuitButton(), "Quit");

        // Sliders
        g2.drawString("Music Volume", cx - 60, cy + 100);
        panel.getMusicSlider().draw(g2);

        g2.drawString("SFX Volume", cx - 50, cy + 150);
        panel.getSfxSlider().draw(g2);
    }

    private void drawButton(Graphics2D g2, Rectangle r, String text) {
        g2.setColor(Color.LIGHT_GRAY);
        g2.fill(r);
        g2.setColor(Color.BLACK);
        g2.draw(r);
        g2.drawString(text, r.x + 55, r.y + 32);
    }

    // -------------------------
    // CARD SELECTION SCREEN
    // -------------------------
    private void drawCardSelection(Graphics g) {
        List<ModifierCard> cards = panel.getCurrentCards();
        if (cards == null || cards.size() != 3) return;

        Graphics2D g2 = (Graphics2D) g;

        int screenW = panel.getWidth();
        int screenH = panel.getHeight();

        // Card layout
        int cardWidth = 260;
        int cardHeight = 180;
        int spacing = 40;

        int totalWidth = cardWidth * 3 + spacing * 2;
        int startX = (screenW - totalWidth) / 2;
        int y = screenH / 2 - cardHeight / 2;

        // Prepare rectangles for GamePanel to use for click detection
        Rectangle[] cardRects = new Rectangle[3];

        for (int i = 0; i < 3; i++) {
            int x = startX + i * (cardWidth + spacing);
            cardRects[i] = new Rectangle(x, y, cardWidth, cardHeight);

            drawSingleCard(g2, cards.get(i), x, y, cardWidth, cardHeight);
        }

        // Store rectangles in GamePanel so mousePressed can detect clicks
        panel.setCardSelectionRects(cardRects);
    }

    private void drawSingleCard(Graphics2D g2, ModifierCard card, int x, int y, int w, int h) {

        // Background
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(x, y, w, h, 20, 20);

        // Rarity border
        g2.setStroke(new BasicStroke(4));
        g2.setColor(getRarityColor(card.rarity));
        g2.drawRoundRect(x, y, w, h, 20, 20);

        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        g2.drawString(card.rarity.toString(), x + 15, y + 35);

        // Good modifier
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(new Color(120, 255, 120));
        g2.drawString("+" + card.good.value + "% " + formatModifier(card.good.type),
                    x + 15, y + 75);

        // Bad modifier
        g2.setColor(new Color(255, 120, 120));
        g2.drawString("+" + card.bad.value + "% " + formatModifier(card.bad.type),
                    x + 15, y + 115);
    }

    private Color getRarityColor(Rarity rarity) {
        return switch (rarity) {
            case STANDARD -> new Color(180, 180, 180);
            case UNCOMMON -> new Color(80, 200, 120);
            case RARE -> new Color(120, 160, 255);
        };
    }

    private String formatModifier(ModifierType type) {
        return switch (type) {
            case DAMAGE_MULT -> "Damage";
            case MAX_HEALTH -> "Max Health";
            case AMMO_CAPACITY -> "Ammo Capacity";
            case FIRE_RATE -> "Fire Rate";
            case RELOAD_SPEED -> "Reload Speed";
            case MOVE_SPEED -> "Move Speed";
            case POINTS_GAINED -> "Points Gained";

            case ENEMY_DAMAGE -> "Enemy Damage";
            case ENEMY_HEALTH -> "Enemy Health";
            case ENEMY_SPEED -> "Enemy Speed";
        };
    }

    // -------------------------
    // GAME OVER SCREEN
    // -------------------------
    private void drawGameOver(Graphics g) {
        Player player = panel.getPlayer();

        int cx = panel.getWidth() / 2;
        int cy = panel.getHeight() / 2;

        panel.retryButton = new Rectangle(cx - 100, cy + 20, 200, 50);
        panel.shopButton  = new Rectangle(cx - 100, cy + 90, 200, 50);
        panel.quitButton  = new Rectangle(cx - 100, cy + 160, 200, 50);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Final Score: " + player.getPoints(),
                panel.getWidth() / 2 - 100, panel.getHeight() / 2);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Game Over",
                panel.getWidth() / 2 - 150, panel.getHeight() / 2 - 80);

        drawButton((Graphics2D) g, panel.getRetryButton(), "Retry");
        drawButton((Graphics2D) g, panel.getShopButton(), "Shop");
        drawButton((Graphics2D) g, panel.getQuitButton(), "Quit");
    }

    // -------------------------
    // SHOP SCREEN
    // -------------------------
    private void drawShop(Graphics g) {
        panel.getShopPanel().draw(g, panel.getPlayer(), panel.getWidth(), panel.getHeight());

        drawButton((Graphics2D) g, panel.getContinueButton(), "Continue");
    }

    // -------------------------
    // DEBUG RENDERING
    // -------------------------
    private void drawDebug(Graphics2D g2) {
        if (!panel.isDebugEnabled()) return;

        int x = 20;
        int y = 40;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(10, 10, 350, 300, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 16));

        g2.drawString("=== DEBUG MODIFIERS ===", x, y); y += 25;

        // Player modifiers
        Player p = panel.getPlayer();
        g2.drawString("Player Damage x" + format(p.damageMultiplier), x, y); y += 20;
        g2.drawString("Player Speed x" + format(p.speedMultiplier), x, y); y += 20;
        g2.drawString("Player Points x" + format(p.pointsMultiplier), x, y); y += 20;
        g2.drawString("Player Max HP +" + format(p.maxHealthBonus), x, y); y += 30;

        // Enemy modifiers
        g2.drawString("Enemy Damage x" + format(Enemy.GLOBAL_DAMAGE_MULT), x, y); y += 20;
        g2.drawString("Enemy Speed x" + format(Enemy.GLOBAL_SPEED_MULT), x, y); y += 20;
        g2.drawString("Enemy Health x" + format(Enemy.GLOBAL_HEALTH_MULT), x, y); y += 20;

        // Weapon modifiers
        WeaponManager wm = panel.getWeaponManager();
        g2.drawString("Fire Rate: " + wm.getCurrent().getStats().rateOfFire, x, y); y += 20;
        g2.drawString("Max Ammo: " + wm.getCurrent().getStats().maxAmmo, x, y); y += 20;
        g2.drawString("Reload Time: " + wm.getCurrent().getStats().reloadTime + " ms", x, y); y += 20;
    }

    private String format(double val) {
        return String.format("%.2f", val);
    }
}