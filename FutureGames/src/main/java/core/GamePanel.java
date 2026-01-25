package core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import entities.Enemy;
import entities.HealthPickup;
import entities.Player;
import entities.Projectile;
import gameModificationCards.Modifier;
import gameModificationCards.ModifierCard;
import input.KeyBindings;
import utils.BloodSplatter;
import utils.MusicManager;
import utils.Sound;
import utils.UISlider;
import waves.WaveManager;
import weapons.AssaultRifle;
import weapons.AutoShotgun;
import weapons.LMG;
import weapons.Pistol;
import weapons.PumpShotgun;
import weapons.Revolver;
import weapons.SMG;
import weapons.Weapon;
import weapons.WeaponManager;
import weapons.WeaponType;
import weapons.WeaponUnlockManager;

public class GamePanel extends JPanel {
    private final Player player = new Player();
    private final CameraManager camera = new CameraManager(player);
    private final List<Enemy> enemies = new ArrayList<>();
    private final GameWorld world = new GameWorld(2400, 2400);
    private final WaveManager waveManager = new WaveManager(enemies, player, world);
    private final List<Projectile> projectiles = new ArrayList<>();
    private final GameRenderer renderer = new GameRenderer(this);
    private final MusicManager musicManager = new MusicManager();
    private final List<HealthPickup> healthPickups = new ArrayList<>();
    private final List<BloodSplatter> bloodEffects = new ArrayList<>();
    private final WeaponType[] weaponOrder = {
        WeaponType.PISTOL,
        WeaponType.REVOLVER,
        WeaponType.SHOTGUN,
        WeaponType.SMG,
        WeaponType.ASSAULTRIFLE,
        WeaponType.AUTOSHOTGUN,
        WeaponType.LMG
    };

    private int currentWeaponIndex = 0;
    
    private GameState gameState = GameState.PLAYING;
    public Rectangle retryButton = new Rectangle( getWidth() / 2 - 200, getHeight() / 2, 400, 100 );
    public Rectangle shopButton;
    public Rectangle quitButton;
    public Rectangle continueButton;
    private Rectangle[] cardSelectionRects;
    private List<ModifierCard> currentCards;
    private Image playButtonImg;
    private long bigStickStartTime = 0;

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

    private boolean debugEnabled = false;
    private Sound bigStickSound = new Sound("/videos/TheBiggerStick.wav");

    public GameWorld getWorld() { return world; }
    public boolean isDebugEnabled() { return debugEnabled; }
    public boolean isMouseDown() { return mouseDown; }
    public Point getMousePos() { return mousePos; }
    public WeaponManager getWeaponManager() { return weaponManager; }
    public MusicManager getMusicManager() { return musicManager; }
    public Player getPlayer() { return player; }
    public CameraManager getCamera() { return camera; }
    public List<Enemy> getEnemies() { return enemies; }
    public WaveManager getWaveManager() { return waveManager; }
    public List<HealthPickup> getHealthPickups() { return healthPickups; }
    public List<BloodSplatter> getBloodEffects() { return bloodEffects; }


    public Rectangle getResumeButton() { return resumeButton; }
    public Rectangle getPauseQuitButton() { return pauseQuitButton; }

    public UISlider getMusicSlider() { return musicSlider; }
    public UISlider getSfxSlider() { return sfxSlider; }

    public Rectangle getRetryButton() { return retryButton; }
    public Rectangle getShopButton() { return shopButton; }
    public Rectangle getQuitButton() { return quitButton; }
    public Rectangle getContinueButton() { return continueButton; }
    public Image getPlayButtonImg() { return playButtonImg; }

    public void setCardSelectionRects(Rectangle[] rects) { this.cardSelectionRects = rects; }
    public Rectangle[] getCardSelectionRects() { return cardSelectionRects; }
    public List<ModifierCard> getCurrentCards() { return currentCards; }
    public void showCardSelection(List<ModifierCard> cards) { this.currentCards = cards; }

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
            WeaponType.PISTOL, new Pistol(this::spawnProjectile, player, world),
            WeaponType.REVOLVER, new Revolver(this::spawnProjectile, player, world),
            WeaponType.SHOTGUN, new PumpShotgun(this::spawnProjectile, player, world),
            WeaponType.SMG, new SMG(this::spawnProjectile, player, world),
            WeaponType.ASSAULTRIFLE, new AssaultRifle(this::spawnProjectile, player, world),
            WeaponType.AUTOSHOTGUN, new AutoShotgun(this::spawnProjectile, player, world),
            WeaponType.LMG, new LMG(this::spawnProjectile, player, world)
        ),
        WeaponType.PISTOL
    );

    public GamePanel() {
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.BLACK);

        try {
            playButtonImg = ImageIO.read(getClass().getResource("/ui/Play Button.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize game world
        player.setWorld(world);

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

        // Key binding for F3 (debug toggle)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
            "toggleDebug"
        );

        getActionMap().put("toggleDebug", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                debugEnabled = !debugEnabled;
            }
        });

        WeaponUnlockManager.unlock("pistol");
            KeyBindings.setup(this, player, index -> {

            if (index == -1) {
                cycleWeapon(-1); // previous
                return;
            }

            if (index == -2) {
                cycleWeapon(+1); // next
                return;
            }

            // Otherwise it's a number key
            switch (index) {
                case 0 -> trySwitch(WeaponType.PISTOL);
                case 1 -> trySwitch(WeaponType.REVOLVER);
                case 2 -> trySwitch(WeaponType.SHOTGUN);
                case 3 -> trySwitch(WeaponType.SMG);
                case 4 -> trySwitch(WeaponType.ASSAULTRIFLE);
                case 5 -> trySwitch(WeaponType.AUTOSHOTGUN);
                case 6 -> trySwitch(WeaponType.LMG);
            }
        });

        new GameLoop(this, player, enemies, waveManager, musicManager).start();

            // Handle shooting
            addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                if (gameState == GameState.BIG_STICK_ENDING) {
                    return; // ignore all input during the ending
                }

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

                // --- CARD SELECTION SCREEN ---
                if (gameState == GameState.CARD_SELECTION) {
                    Rectangle[] rects = getCardSelectionRects();

                    for (int i = 0; i < 3; i++) {
                        if (rects[i].contains(mx, my)) {
                            applyCard(currentCards.get(i));
                            setGameState(GameState.PLAYING);
                            waveManager.advanceWave();
                            return;
                        }
                    }
                }

                // --- SHOP SCREEN INTERACTION ---
                if (gameState == GameState.SHOP) {

                    // First let the shop panel handle unlock/upgrade clicks
                    getShopPanel().handleClick(mx, my, player);

                    // Now handle the Play Button
                    if (continueButton != null && continueButton.contains(mx, my)) {
                        gameState = GameState.PLAYING;
                        return;
                    }
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
        shopPanel.setOnBigStickActivated(() -> {
            playCutscene();
        });
    }

    private JCodecVideoPanel videoPanel;

    private void playCutscene() {
        gameState = GameState.BIG_STICK_ENDING;

        removeAll();
        revalidate();
        repaint();

        musicManager.pauseMusic();
        bigStickSound.play();

        videoPanel = new JCodecVideoPanel("/videos/TheBiggerStick.mp4", () -> {
            SwingUtilities.invokeLater(() -> {

                remove(videoPanel);
                videoPanel = null;

                gameState = GameState.GAME_OVER; // or ENDING
                musicManager.resumeMusic();
                repaint();
            });
        });

        setLayout(new BorderLayout());
        add(videoPanel, BorderLayout.CENTER);
        revalidate();
        repaint();

        new Thread(videoPanel).start();
    }

    private void cycleWeapon(int direction) {
        int total = weaponOrder.length;

        // Move index
        currentWeaponIndex = (currentWeaponIndex + direction + total) % total;

        WeaponType next = weaponOrder[currentWeaponIndex];

        // Only switch if unlocked
        if (WeaponUnlockManager.isUnlocked(next.name().toLowerCase())) {
            weaponManager.switchTo(next);
            player.setWeaponAnimation(next);
        } else {
            // If locked, keep cycling until you find an unlocked one
            cycleWeapon(direction);
        }
    }

    private void trySwitch(WeaponType type) {
        if (WeaponUnlockManager.isUnlocked(type.name().toLowerCase())) {
            weaponManager.switchTo(type);
            player.setWeaponAnimation(type);

            // Sync circular index
            for (int i = 0; i < weaponOrder.length; i++) {
                if (weaponOrder[i] == type) {
                    currentWeaponIndex = i;
                    break;
                }
            }
        }
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

    public void applyCard(ModifierCard card) {
        applyModifier(card.good);
        applyModifier(card.bad);
    }

    private void applyModifier(Modifier m) {
        switch (m.type) {
            case DAMAGE_MULT -> player.damageMultiplier += m.value / 100.0;
            case MAX_HEALTH -> player.increaseMaxHealth(m.value);
            case AMMO_CAPACITY -> weaponManager.increaseAmmoCapacity(m.value);
            case FIRE_RATE -> weaponManager.increaseFireRate(m.value);
            case RELOAD_SPEED -> weaponManager.increaseReloadSpeed(m.value);
            case MOVE_SPEED -> player.increaseSpeed(m.value);
            case POINTS_GAINED -> player.pointsMultiplier += m.value / 100.0;

            case ENEMY_DAMAGE -> Enemy.GLOBAL_DAMAGE_MULT += m.value / 100.0;
            case ENEMY_HEALTH -> Enemy.GLOBAL_HEALTH_MULT += m.value / 100.0;
            case ENEMY_SPEED -> Enemy.GLOBAL_SPEED_MULT += m.value / 100.0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (gameState == GameState.BIG_STICK_ENDING) {
            super.paintComponent(g);
            return; // do NOT draw game or death UI
        }
        
        super.paintComponent(g);

        updateUIRectangles();

        renderer.render(g);
    }

    public void updateUIRectangles() {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        if (gameState == GameState.PAUSED) {
            resumeButton = new Rectangle(cx - 100, cy - 40, 200, 50);
            pauseQuitButton = new Rectangle(cx - 100, cy + 30, 200, 50);
        }

        if (gameState == GameState.GAME_OVER) {
            retryButton = new Rectangle(cx - 100, cy, 200, 50);
            shopButton  = new Rectangle(cx - 100, cy + 70, 200, 50);
            quitButton  = new Rectangle(cx - 100, cy + 140, 200, 50);
        }

        if (gameState == GameState.SHOP) {
            int w = 200;
            int h = 80;

            continueButton = new Rectangle(
                getWidth() - w - 40,
                getHeight() - h - 40,
                w,
                h
            );
        }
    }

    public List<Projectile> getProjectiles() {
    return projectiles;
    }

    public void resetGame() {
        // Reset game state
        gameState = GameState.PLAYING;

        // -------------------------
        // RESET PLAYER MODIFIERS
        // -------------------------
        player.damageMultiplier = 1.0;
        player.speedMultiplier = 1.0;
        player.pointsMultiplier = 1.0;
        player.maxHealthBonus = 0.0;

        player.recalcStats();
        player.health = player.maxHealth;
        player.setPosition(300, 200);

        // -------------------------
        // RESET ENEMY GLOBAL MODIFIERS
        // -------------------------
        Enemy.GLOBAL_SPEED_MULT = 1.0;
        Enemy.GLOBAL_DAMAGE_MULT = 1.0;
        Enemy.GLOBAL_HEALTH_MULT = 1.0;
        Enemy.GLOBAL_COOLDOWN_MULT = 1.0;
        Enemy.GLOBAL_POINTS_MULT = 1.0;

        // -------------------------
        // CLEAR ENTITIES
        // -------------------------
        synchronized (enemies) { enemies.clear(); }
        synchronized (projectiles) { projectiles.clear(); }
        healthPickups.clear();

        // -------------------------
        // RESET WEAPONS
        // -------------------------
        for (Weapon w : weaponManager.getAllWeapons()) {
            w.resetStatsToBase();   // you'll add this method
        }

        weaponManager.applyPermanentUpgrades();

        weaponManager.switchTo(WeaponType.PISTOL);
        player.setWeaponAnimation(WeaponType.PISTOL);

        // -------------------------
        // RESET WAVES
        // -------------------------
        waveManager.reset();
        waveManager.advanceWave(); // start Wave 1
    }
}