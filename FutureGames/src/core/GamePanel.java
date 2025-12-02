package core;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import entities.Enemy;
import entities.Player;
import entities.Projectile;
import input.KeyBindings;
import waves.WaveManager;
import weapons.*;

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
    private boolean mouseDown = false;
    private Point mousePos = new Point(0, 0);
    public boolean isMouseDown() { return mouseDown; }
    public Point getMousePos() { return mousePos; }
    public WeaponManager getWeaponManager() { return weaponManager; }

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
            WeaponType.SMG, new SMG(this::spawnProjectile),
            WeaponType.SHOTGUN, new PumpShotgun(this::spawnProjectile)
        ),
        WeaponType.HANDGUN
    );

    public GamePanel() {
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.BLACK);
        setFocusable(true);
        KeyBindings.setup(this, player, index -> {
            switch (index) {
                case 0 -> weaponManager.switchTo(WeaponType.HANDGUN);
                case 1 -> weaponManager.switchTo(WeaponType.SMG);
                case 2 -> weaponManager.switchTo(WeaponType.SHOTGUN);
            }
        });
        new GameLoop(this, player, enemies, waveManager).start();

        // Handle shooting
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mouseDown = true;

                    if (gameState == GameState.GAME_OVER && retryButton.contains(e.getPoint())) {
                        resetGame();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mouseDown = false;
                }
            }
        });

        // Track mouse for aiming
        addMouseMotionListener(new MouseMotionAdapter() {
        @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mousePos = e.getPoint();
            }
        });
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
        }

        // Update retry button position
        retryButton = new Rectangle(getWidth() / 2 - 100, getHeight() / 2, 200, 50);
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