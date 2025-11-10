package core;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import entities.Enemy;
import entities.FastEnemy;
import entities.Player;
import entities.TankEnemy;
import entities.Projectile;
import input.KeyBindings;
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
    private boolean mouseDown = false;
    private Point mousePos = new Point(0, 0);
    public boolean isMouseDown() { return mouseDown; }
    public Point getMousePos() { return mousePos; }
    public WeaponManager getWeaponManager() { return weaponManager; }

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

        Timer waveStartTimer = new Timer(3000, e -> {
            List<Wave.SpawnRequest> requests = List.of(
                new Wave.SpawnRequest(FastEnemy.class, 1000),
                new Wave.SpawnRequest(TankEnemy.class, 0)
            );
        waveManager.startWave(new Wave(requests, 0.001)); // 0.5s per enemy → 30 enemies over 15s
        });
        waveStartTimer.setRepeats(false);
        waveStartTimer.start();
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

        // Draw enemies (you might want to draw them last if needed)
        synchronized (enemies) {
            for (Enemy enemy : enemies) {
                enemy.draw(g, camera);
            }
        }
    }

    public List<Projectile> getProjectiles() {
    return projectiles;
    }

}