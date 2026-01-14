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
    private final GameRenderer renderer = new GameRenderer(this);
    private final MusicManager musicManager = new MusicManager();
    
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
    public MusicManager getMusicManager() { return musicManager; }
    public Player getPlayer() { return player; }
    public CameraManager getCamera() { return camera; }
    public List<Enemy> getEnemies() { return enemies; }
    public WaveManager getWaveManager() { return waveManager; }

    public Rectangle getResumeButton() { return resumeButton; }
    public Rectangle getPauseQuitButton() { return pauseQuitButton; }

    public UISlider getMusicSlider() { return musicSlider; }
    public UISlider getSfxSlider() { return sfxSlider; }

    public Rectangle getRetryButton() { return retryButton; }
    public Rectangle getShopButton() { return shopButton; }
    public Rectangle getQuitButton() { return quitButton; }
    public Rectangle getContinueButton() { return continueButton; }

    public ShopPanel getShopPanel() { return shopPanel; }

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

        updateUIRectangles();   // <-- ADD THIS

        renderer.render(g);
    }

    public void updateUIRectangles() {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        if (gameState == GameState.PAUSED) {
            resumeButton = new Rectangle(cx - 100, cy - 40, 200, 50);
            pauseQuitButton = new Rectangle(cx - 100, cy + 30, 200, 50);

            musicSlider.x = cx - 120;
            musicSlider.y = cy + 110;

            sfxSlider.x = cx - 120;
            sfxSlider.y = cy + 160;
        }

        if (gameState == GameState.GAME_OVER) {
            retryButton = new Rectangle(cx - 100, cy, 200, 50);
            shopButton  = new Rectangle(cx - 100, cy + 70, 200, 50);
            quitButton  = new Rectangle(cx - 100, cy + 140, 200, 50);
        }

        if (gameState == GameState.SHOP) {
            continueButton = new Rectangle(getWidth() - 220, getHeight() - 80, 200, 50);
        }
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