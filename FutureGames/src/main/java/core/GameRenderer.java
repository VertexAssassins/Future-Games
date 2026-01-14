package core;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import entities.Enemy;
import entities.HealthPickup;
import entities.Player;
import entities.Projectile;
import waves.WaveManager;

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
            case PLAYING -> drawGameplay(g);
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
    // GAME OVER SCREEN
    // -------------------------
    private void drawGameOver(Graphics g) {
        Player player = panel.getPlayer();

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
}