package core;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

import entities.Enemy;
import entities.HealthPickup;
import entities.Player;
import entities.Projectile;
import gameModificationCards.ModifierCard;
import gameModificationCards.ModifierType;
import utils.BloodSplatter;
import waves.WaveManager;
import weapons.WeaponManager;

public class GameRenderer {

    private final GamePanel panel;

    private Image cardWhite;
    private Image cardGreen;
    private Image cardBlue;
    private Image pauseBorder;
    private Image pauseResume;
    private Image pauseQuit;
    private Image retryButtonImg;
    private Image shopButtonImg;
    private Image quitButtonImg;
    private Image borderPanelImg;

    private Font easyText;

    public GameRenderer(GamePanel panel) {
        this.panel = panel;

        // Load images
        cardWhite = new ImageIcon(getClass().getResource("/ui/Border Template White.png")).getImage();
        cardGreen = new ImageIcon(getClass().getResource("/ui/Border Template Green.png")).getImage();
        cardBlue  = new ImageIcon(getClass().getResource("/ui/Border Template Blue.png")).getImage();
        pauseBorder = new ImageIcon(getClass().getResource("/ui/Border Template.png")).getImage();
        pauseResume = new ImageIcon(getClass().getResource("/ui/Play Button.png")).getImage();
        pauseQuit   = new ImageIcon(getClass().getResource("/ui/Quit Button.png")).getImage();
        retryButtonImg = new ImageIcon(getClass().getResource("/ui/Retry Button.png")).getImage();
        shopButtonImg  = new ImageIcon(getClass().getResource("/ui/Shop Button.png")).getImage();
        quitButtonImg  = new ImageIcon(getClass().getResource("/ui/Quit Button.png")).getImage();
        borderPanelImg = new ImageIcon(getClass().getResource("/ui/Border Template.png")).getImage();

        pauseResume = scale(pauseResume, 128, 128);
        pauseQuit   = scale(pauseQuit,   128, 128);

        // Load custom font
        try {
            easyText = Font.createFont(
                    Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/fonts/EASYTEXT.TTF")
            );
            // Register the font with the graphics environment
            // REF: https://docs.oracle.com/javase/8/docs/api/java/awt/GraphicsEnvironment.html
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(easyText);
        } catch (Exception e) {
            e.printStackTrace();
            easyText = new Font("Monospaced", Font.BOLD, 20); // fallback
        }
    }

    // MAIN ENTRY POINT
    public void render(Graphics g) {
        GameState state = panel.getGameState();

        switch (state) {
            case PAUSED -> drawPauseMenu(g);
            case PLAYING -> { drawGameplay(g); drawDebug((Graphics2D) g); }
            case CARD_SELECTION -> drawCardSelection(g);
            case GAME_OVER -> drawGameOver(g);
            case SHOP -> drawShop(g);
            case BIG_STICK_ENDING -> drawBigStickEnding(g);
        }
    }

    // GAMEPLAY RENDERING
    private void drawGameplay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        //Draw tile background first
        panel.getWorld().draw(
            g2,
            panel.getCamera().getOffsetX(),
            panel.getCamera().getOffsetY(),
            panel.getWidth(),
            panel.getHeight()
        );

        // Draw static and other world objects
        double camX = panel.getCamera().getOffsetX();
        double camY = panel.getCamera().getOffsetY();
        for (WorldObject obj : panel.getWorld().getObjects()) {
            obj.draw(g2, camX, camY, panel.getWidth(), panel.getHeight(),
                     panel.getWorld().getWidth(), panel.getWorld().getHeight());
        }

        // Draw gameplay elements on top
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
        Graphics2D g2 = (Graphics2D) g;

        // 1. Draw blood splatters first
        for (BloodSplatter b : panel.getBloodEffects()) {
            b.render(g2, panel.getCamera());
        }

        // 2. Snapshot enemies once (this is done to avoid concurrent modification issues)
        List<Enemy> snapshot = new ArrayList<>(panel.getEnemies());

        // 3. Draw enemies
        for (Enemy e : snapshot) {
            e.draw(g2, panel.getCamera());
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

    // HUD (wave, points, health)
    private void drawHUD(Graphics g) {
        g.setFont(easyText.deriveFont(Font.BOLD, 15f));
        g.setColor(Color.WHITE);

        WaveManager waves = panel.getWaveManager();
        Player player = panel.getPlayer();

        // Wave
        g.drawString("Wave: " + waves.getWaveNumber(), 20, 30);

        // Points
        g.drawString("Points: " + player.getPoints(), panel.getWidth() - 150, 30);

        drawHealthBar(g, player);
        drawDashCharge(g, player);
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

        g.setFont(easyText.deriveFont(Font.BOLD, 15f));
        g.drawString((int)health + " / " + (int)max, x + 60, y + 16);
    }

    private void drawDashCharge(Graphics g, Player player) {
        // Position under the health bar
        int x = 30;
        int y = 120; // 30px below health bar

        BufferedImage frame = player.getDashChargeFrame();
        int size = frame.getWidth(); // assuming square frames

        // Draw the animation frame
        g.drawImage(frame, x, y, size, size, null);

        // Draw charge count next to it
        g.setColor(Color.WHITE);
        g.setFont(easyText.deriveFont(Font.BOLD, 15f));
        g.drawString(player.getDashCharges() + " / " + player.getMaxDashCharges(),
                    x + size + 10,
                    y + size - 10);
    }

    // PAUSE MENU
    private void drawPauseMenu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int screenW = panel.getWidth();
        int screenH = panel.getHeight();

        // Dim background
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenW, screenH);

        // Panel size
        int panelW = 720;
        int panelH = 720;

        int x = (screenW - panelW) / 2;
        int y = (screenH - panelH) / 2;

        // Background
        g2.drawImage(pauseBorder, x, y, panelW, panelH, null);

        // TITLE
        g2.setFont(easyText.deriveFont(Font.BOLD, 64f));
        g2.setColor(Color.WHITE);

        String title = "PAUSED";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = x + (panelW - fm.stringWidth(title)) / 2;
        int titleY = y + 120;

        g2.drawString(title, titleX, titleY);

        // BUTTONS (scaled)
        int buttonSpacing = 110;   // more spacing for bigger buttons
        int buttonStartY = y + 120;

        int btnW = pauseResume.getWidth(null);
        int btnH = pauseResume.getHeight(null);

        // Resume
        Rectangle resumeRect = panel.getResumeButton();
        resumeRect.setBounds(
                x + (panelW - btnW) / 2,
                buttonStartY,
                btnW,
                btnH
        );
        g2.drawImage(pauseResume, resumeRect.x, resumeRect.y, btnW, btnH, null);

        // Quit
        Rectangle quitRect = panel.getPauseQuitButton();
        quitRect.setBounds(
                x + (panelW - btnW) / 2,
                buttonStartY + buttonSpacing,
                btnW,
                btnH
        );
        g2.drawImage(pauseQuit, quitRect.x, quitRect.y, btnW, btnH, null);

        // SLIDERS (centered)
        g2.setFont(easyText.deriveFont(Font.BOLD, 32f));
        g2.setColor(Color.WHITE);

        FontMetrics fm32 = g2.getFontMetrics();   // IMPORTANT FIX

        int sliderWidth = 300;
        int sliderHeight = 8;

        int centerX = x + panelW / 2;
        int sliderX = centerX - (sliderWidth / 2);

        int musicLabelY = y + 420;
        int musicSliderY = musicLabelY + 40;

        int sfxLabelY = musicSliderY + 80;
        int sfxSliderY = sfxLabelY + 40;

        // MUSIC
        String musicText = "Music Volume";
        int musicTextX = centerX - (fm32.stringWidth(musicText) / 2);
        g2.drawString(musicText, musicTextX, musicLabelY);

        panel.getMusicSlider().setFillColor(Color.RED);
        panel.getMusicSlider().setBounds(sliderX, musicSliderY, sliderWidth, sliderHeight);
        panel.getMusicSlider().draw(g2);

        // SFX
        String sfxText = "SFX Volume";
        int sfxTextX = centerX - (fm32.stringWidth(sfxText) / 2);

        panel.getSfxSlider().setFillColor(Color.RED);
        panel.getSfxSlider().setBounds(sliderX, sfxSliderY, sliderWidth, sliderHeight);
        panel.getSfxSlider().draw(g2);

        g2.setColor(Color.WHITE);
        g2.drawString(sfxText, sfxTextX, sfxLabelY);
    }

    private Image scale(Image img, int w, int h) {
        return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    // CARD SELECTION SCREEN
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

        // Pick template based on rarity
        Image template = switch (card.rarity) {
            case STANDARD -> cardWhite;
            case UNCOMMON -> cardGreen;
            case RARE -> cardBlue;
        };

        // Draw the template scaled to card size
        g2.drawImage(template, x, y, w, h, null);

        // Title (rarity text)
        g2.setFont(easyText.deriveFont(Font.BOLD, 20f));
        g2.setColor(Color.WHITE);

        String rarityText = card.rarity.toString();
        FontMetrics fm = g2.getFontMetrics();

        // Center horizontally inside the card
        int textWidth = fm.stringWidth(rarityText);
        int centerX = x + (w - textWidth) / 2;

        g2.drawString(rarityText, centerX, y + 40);


        // Good modifier
        g2.setFont(easyText.deriveFont(Font.BOLD, 20f));
        g2.setColor(new Color(120, 255, 120));
        g2.drawString(
                "+" + card.good.value + "% " + formatModifier(card.good.type),
                x + 20,
                y + 80
        );

        // Bad modifier
        g2.setColor(new Color(255, 120, 120));
        g2.drawString(
                "+" + card.bad.value + "% " + formatModifier(card.bad.type),
                x + 20,
                y + 120
        );
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

    // GAME OVER SCREEN
    private void drawGameOver(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Player player = panel.getPlayer();

        // Panel size (adjust if needed)
        int panelW = 900;
        int panelH = 700;

        // Center panel on screen
        int x = (panel.getWidth() - panelW) / 2;
        int y = (panel.getHeight() - panelH) / 2;

        // Draw panel background
        g2.drawImage(borderPanelImg, x, y, panelW, panelH, null);

        // GAME OVER TITLE (centered)
        String title = "Game Over";
        g2.setFont(easyText.deriveFont(Font.BOLD, 64f));
        g2.setColor(Color.WHITE);

        FontMetrics fmTitle = g2.getFontMetrics();
        int titleX = x + (panelW - fmTitle.stringWidth(title)) / 2;
        int titleY = y + 120;

        g2.drawString(title, titleX, titleY);

        // FINAL SCORE (centered)
        String scoreText = "Final Score: " + player.getPoints();
        g2.setFont(easyText.deriveFont(Font.BOLD, 32f));
        g2.setColor(Color.YELLOW);

        FontMetrics fmScore = g2.getFontMetrics();
        int scoreX = x + (panelW - fmScore.stringWidth(scoreText)) / 2;
        int scoreY = y + 200;

        g2.drawString(scoreText, scoreX, scoreY);

        // BUTTONS (centered inside panel)
        int btnW = 350;
        int btnH = 150;
        int btnX = x + (panelW - btnW) / 2;

        // Update rectangles for click detection
        panel.retryButton = new Rectangle(btnX, y + 280, btnW, btnH);
        panel.shopButton  = new Rectangle(btnX, y + 400, btnW, btnH);
        panel.quitButton  = new Rectangle(btnX, y + 520, btnW, btnH);

        // Draw PNG buttons
        g2.drawImage(retryButtonImg, panel.retryButton.x, panel.retryButton.y, btnW, btnH, null);
        g2.drawImage(shopButtonImg,  panel.shopButton.x,  panel.shopButton.y,  btnW, btnH, null);
        g2.drawImage(quitButtonImg,  panel.quitButton.x,  panel.quitButton.y,  btnW, btnH, null);
    }

    // SHOP SCREEN
    private void drawShop(Graphics g) {
        panel.getShopPanel().draw(g, panel.getPlayer(), panel.getWidth(), panel.getHeight());

        drawPlayButton((Graphics2D) g);
    }

    private void drawPlayButton(Graphics2D g) {
        Rectangle btn = panel.getContinueButton();
        Image img = panel.getPlayButtonImg();

        if (btn == null || img == null) return;

        g.drawImage(
            img,
            btn.x,
            btn.y,
            btn.width,
            btn.height,
            null
        );
    }
    
    // DEBUG RENDERING
    private void drawDebug(Graphics2D g2) {
        if (!panel.isDebugEnabled()) return;

        int x = 20;
        int y = 40;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(10, 10, 350, 300, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(easyText.deriveFont(Font.BOLD, 20f));

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

    // BIG STICK ENDING
    private void drawBigStickEnding(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int w = panel.getWidth();
        int h = panel.getHeight();

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);
    }
}