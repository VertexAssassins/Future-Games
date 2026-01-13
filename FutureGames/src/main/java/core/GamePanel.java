package core;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import entities.Enemy;
import entities.Player;
import entities.Projectile;
import input.KeyBindings;
import utils.MusicManager;
import utils.Sound;
import waves.WaveManager;
import weapons.*;
import utils.UISlider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GamePanel extends JPanel {
    private final Player player = new Player();
    private final CameraManager camera = new CameraManager(player);
    private final List<Enemy> enemies = new ArrayList<>();
    private final WaveManager waveManager = new WaveManager(enemies, player);
    private final List<Projectile> projectiles = new ArrayList<>();
    private GameState gameState = GameState.PLAYING;
    private Rectangle retryButton = new Rectangle( getWidth() / 2 - 100, getHeight() / 2, 200, 50 );
    private Rectangle shopButton;
    private Rectangle quitButton;
    private Rectangle continueButton;
    // Pause menu UI
    private Rectangle resumeButton;
    private Rectangle pauseQuitButton;
    private UISlider musicSlider;
    private UISlider sfxSlider;
    private final ShopPanel shopPanel = new ShopPanel();
    private boolean mouseDown = false;
    private Point mousePos = new Point(0, 0);
    private boolean draggingMusic = false;
    private boolean draggingSfx = false;
    public boolean isMouseDown() { return mouseDown; }
    public Point getMousePos() { return mousePos; }
    public WeaponManager getWeaponManager() { return weaponManager; }
    private final MusicManager musicManager = new MusicManager();
    public MusicManager getMusicManager() { return musicManager; }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState state) {
        this.gameState = state;
    }

    public double getAimAngle() {
    double worldMouseX = mousePos.x + camera.getOffsetX();
    double worldMouseY = mousePos.y + camera.getOffsetY();
    return Math.atan2(worldMouseY - player.getY(), worldMouseX - player.getX());
    }

    private final WeaponManager weaponManager = new WeaponManager(
        Map.of(
            WeaponType.HANDGUN, new Handgun(this::spawnProjectile),
            WeaponType.REVOLVER, new Revolver(this::spawnProjectile),
            WeaponType.SHOTGUN, new PumpShotgun(this::spawnProjectile),
            WeaponType.SMG, new SMG(this::spawnProjectile),
            WeaponType.ASSAULTRIFLE, new AssaultRifle(this::spawnProjectile),
            WeaponType.AUTOSHOTGUN, new AutoShotgun(this::spawnProjectile),
            WeaponType.LMG, new LMG(this::spawnProjectile)
        ),
        WeaponType.HANDGUN
    );

    public GamePanel() {
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.BLACK);

        // Initialize pause menu UI
        resumeButton = new Rectangle(0, 0, 0, 0);       // will be positioned in paintComponent
        pauseQuitButton = new Rectangle(0, 0, 0, 0);

        musicSlider = new UISlider(0, 0, 200, 1.0f);    // default full volume
        sfxSlider   = new UISlider(0, 0, 200, 1.0f);

        setFocusable(true);

        // Key binding for ESC (pause toggle)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "togglePause"
        );

        getActionMap().put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });

        WeaponUnlockManager.unlock("pistol");
        KeyBindings.setup(this, player, index -> {
            switch (index) {
                case 0 -> {
                    weaponManager.switchTo(WeaponType.HANDGUN);
                    player.setWeaponAnimation(WeaponType.HANDGUN);
                }
                case 1 -> {
                    weaponManager.switchTo(WeaponType.REVOLVER);
                    player.setWeaponAnimation(WeaponType.REVOLVER);
                }
                case 2 -> {
                    weaponManager.switchTo(WeaponType.SHOTGUN);
                    player.setWeaponAnimation(WeaponType.SHOTGUN);
                }
                case 3 -> {
                    weaponManager.switchTo(WeaponType.SMG);
                    player.setWeaponAnimation(WeaponType.SMG);
                }
                case 4 -> {
                    weaponManager.switchTo(WeaponType.ASSAULTRIFLE);
                    player.setWeaponAnimation(WeaponType.ASSAULTRIFLE);
                }
                case 5 -> {
                    weaponManager.switchTo(WeaponType.AUTOSHOTGUN);
                    player.setWeaponAnimation(WeaponType.AUTOSHOTGUN);
                }
                case 6 -> {
                    weaponManager.switchTo(WeaponType.LMG);
                    player.setWeaponAnimation(WeaponType.LMG);
                }
            }
        });
        new GameLoop(this, player, enemies, waveManager, musicManager).start();

            // Handle shooting
            addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                // --- GAME OVER SCREEN BUTTONS ---
                if (gameState == GameState.GAME_OVER) {

                    if (retryButton.contains(mx, my)) {
                        resetGame();
                        return;
                    }

                    if (shopButton.contains(mx, my)) {
                        gameState = GameState.SHOP;
                        return;
                    }

                    if (quitButton.contains(mx, my)) {
                        System.exit(0);
                    }
                }

                // --- SHOP SCREEN INTERACTION ---
                if (gameState == GameState.SHOP) {
                    shopPanel.handleClick(mx, my, player);

                    // Handle continue button
                    if (continueButton.contains(mx, my)) {
                        gameState = GameState.PLAYING;
                        return;
                    }

                    return;
                }

                // --- PAUSE MENU INTERACTION ---
                if (gameState == GameState.PAUSED) {

                    if (resumeButton.contains(mx, my)) {
                        gameState = GameState.PLAYING;
                        return;
                    }

                    if (pauseQuitButton.contains(mx, my)) {
                        System.exit(0);
                    }

                    if (musicSlider.contains(mx, my)) {
                        draggingMusic = true;
                        musicSlider.setFromMouse(mx);
                        musicManager.setVolume(musicSlider.value);
                        repaint();
                        return;
                    }

                    if (sfxSlider.contains(mx, my)) {
                        draggingSfx = true;
                        sfxSlider.setFromMouse(mx);
                        Sound.globalSfxVolume = sfxSlider.value;
                        repaint();
                        return;
                    }

                    return;
                }

                // --- NORMAL GAME SHOOTING ---
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mouseDown = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mouseDown = false;
                    draggingMusic = false;
                    draggingSfx = false;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                player.updateFacingDirection(mousePos.x);
            }

            @Override
                public void mouseDragged(MouseEvent e) {
                    mousePos = e.getPoint();
                    player.updateFacingDirection(mousePos.x);

                    if (gameState == GameState.PAUSED) {
                        int mx = e.getX();

                        if (draggingMusic) {
                            musicSlider.setFromMouse(mx);
                            musicManager.setVolume(musicSlider.value);
                            repaint();
                        }

                        if (draggingSfx) {
                            sfxSlider.setFromMouse(mx);
                            Sound.globalSfxVolume = sfxSlider.value;
                            repaint();
                        }
                    }
                }
        });

        addMouseWheelListener(e -> {
            if (gameState == GameState.SHOP) {
                shopPanel.handleScroll(e.getWheelRotation());
                repaint();
            }
        });
    }

    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }

    public void attemptShoot() {
    if (!mouseDown) return;

    double angle = getAimAngle();
    weaponManager.tryShoot(player.getX(), player.getY(), angle);
    }

    public void spawnProjectile(Projectile p) {
        synchronized (projectiles) {
            projectiles.add(p);
        }
    }

   @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState == GameState.PAUSED) {
            // Update pause UI positions dynamically
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            resumeButton = new Rectangle(cx - 100, cy - 40, 200, 50);
            pauseQuitButton = new Rectangle(cx - 100, cy + 30, 200, 50);

            musicSlider.x = cx - 120;
            musicSlider.y = cy + 110;

            sfxSlider.x = cx - 120;
            sfxSlider.y = cy + 160;

            drawPauseMenu(g);
            return;
        }

        // Draw player
        player.draw(g, camera);

        // Draw projectiles
        synchronized (projectiles) {
            for (Projectile p : projectiles) {
                p.draw(g, camera.getOffsetX(), camera.getOffsetY());
            }
        }

        // Draw enemies
        synchronized (enemies) {
            for (Enemy enemy : enemies) {
                enemy.draw(g, camera);
            }
        }

        // HUD
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);

        if (gameState == GameState.PLAYING) {
            // Wave (top-left)
            g.drawString("Wave: " + waveManager.getWaveNumber(), 20, 30);

            // Points (top-right)
            g.drawString("Points: " + player.getPoints(), getWidth() - 150, 30);
        }

        if (gameState == GameState.GAME_OVER) {
            // Final score (center)
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Final Score: " + player.getPoints(),
                        getWidth() / 2 - 100, getHeight() / 2);

            // Game Over text
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over", getWidth() / 2 - 150, getHeight() / 2 - 80);

            // Retry button
            g.setColor(Color.DARK_GRAY);
            g.fillRect(retryButton.x, retryButton.y, retryButton.width, retryButton.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Retry", retryButton.x + 65, retryButton.y + 32);

            // Shop button
            g.setColor(Color.DARK_GRAY);
            g.fillRect(shopButton.x, shopButton.y, shopButton.width, shopButton.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Shop", shopButton.x + 70, shopButton.y + 32);

            // Quit button
            g.setColor(Color.DARK_GRAY);
            g.fillRect(quitButton.x, quitButton.y, quitButton.width, quitButton.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Quit", quitButton.x + 75, quitButton.y + 32);

        }

        if (gameState == GameState.SHOP) {
            shopPanel.draw(g, player, getWidth(), getHeight());

            // Continue button (bottom-right)
            continueButton = new Rectangle(getWidth() - 220, getHeight() - 80, 200, 50);

            g.setColor(Color.DARK_GRAY);
            g.fillRect(continueButton.x, continueButton.y, continueButton.width, continueButton.height);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Continue", continueButton.x + 50, continueButton.y + 32);

            return; // skip drawing game world
        }

        // Update retry button position
        retryButton = new Rectangle(getWidth() / 2 - 100, getHeight() / 2, 200, 50);
        shopButton  = new Rectangle(getWidth() / 2 - 100, getHeight() / 2 + 70, 200, 50);
        quitButton  = new Rectangle(getWidth() / 2 - 100, getHeight() / 2 + 140, 200, 50);
    }

    private void drawPauseMenu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Dim background
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Menu box
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(getWidth()/2 - 180, getHeight()/2 - 180, 360, 360, 20, 20);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        g2.drawString("PAUSED", getWidth()/2 - 60, getHeight()/2 - 120);

        // Resume button
        g2.setColor(Color.LIGHT_GRAY);
        g2.fill(resumeButton);
        g2.setColor(Color.BLACK);
        g2.draw(resumeButton);
        g2.drawString("Resume", resumeButton.x + 55, resumeButton.y + 32);

        // Quit button
        g2.setColor(Color.LIGHT_GRAY);
        g2.fill(pauseQuitButton);
        g2.setColor(Color.BLACK);
        g2.draw(pauseQuitButton);
        g2.drawString("Quit", pauseQuitButton.x + 75, pauseQuitButton.y + 32);

        // Sliders
        g2.drawString("Music Volume", getWidth()/2 - 60, getHeight()/2 + 100);
        musicSlider.draw(g2);

        g2.drawString("SFX Volume", getWidth()/2 - 50, getHeight()/2 + 150);
        sfxSlider.draw(g2);
    }

    public List<Projectile> getProjectiles() {
    return projectiles;
    }

    public void resetGame() {
        // Reset game state
        gameState = GameState.PLAYING;

        // Reinitialize player
        player.applyModifiers(1.0, 1.0); // reset stats
        player.setPosition(300, 200);   // or your spawn point
        // Optionally reset movement flags if needed

        // Clear enemies and projectiles
        synchronized (enemies) {
            enemies.clear();
        }
        synchronized (projectiles) {
            projectiles.clear();
        }

        // Reset weapon manager
        weaponManager.switchTo(WeaponType.HANDGUN);
        weaponManager.getCurrent().reload(); // optional

        // Reset wave manager
        waveManager.reset();
    }
}