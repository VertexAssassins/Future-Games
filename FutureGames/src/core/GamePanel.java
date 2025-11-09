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
import utils.Constants;
import weapons.*;

import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private final Player player = new Player();
    private final CameraManager camera = new CameraManager(player);
    private final List<Enemy> enemies = new ArrayList<>();
    private final WaveManager waveManager = new WaveManager(enemies, player);
    private final List<Projectile> projectiles = new ArrayList<>();
    private final Handgun handgun = new Handgun();
    private final SMG smg = new SMG();
    private final PumpShotgun shotgun = new PumpShotgun();
    private Weapon currentWeapon = handgun;
    private boolean mouseDown = false;

    private Point mousePos = new Point(0, 0);

    public Weapon getCurrentWeapon() { return currentWeapon; }
    public boolean isMouseDown() { return mouseDown; }
    public Point getMousePos() { return mousePos; }

    public GamePanel() {
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.BLACK);
        setFocusable(true);
        KeyBindings.setup(this, player, index -> {
            switch (index) {
                case 0 -> currentWeapon = handgun;
                case 1 -> currentWeapon = smg;
                case 2 -> currentWeapon = shotgun;
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
        });

        Timer waveStartTimer = new Timer(3000, e -> {
            List<Wave.SpawnRequest> requests = List.of(
                new Wave.SpawnRequest(FastEnemy.class, 0),
                new Wave.SpawnRequest(TankEnemy.class, 0)
            );
        waveManager.startWave(new Wave(requests, 0.0001)); // 0.5s per enemy → 30 enemies over 15s
        });
        waveStartTimer.setRepeats(false);
        waveStartTimer.start();
    }

    public void attemptShoot() {
        if (!mouseDown) return; // only fire if mouse is held

        Weapon weapon = currentWeapon;
        if (weapon.tryFire(0, 0, mousePos)) { // 0,0 because player is center of panel for aiming
            // Angle from panel center (player) to current mouse position
            double angle = Math.atan2(mousePos.y - (getHeight() / 2.0),
                                    mousePos.x - (getWidth() / 2.0));

            if (weapon instanceof PumpShotgun) {
                int pellets = 6;
                for (int i = 0; i < pellets; i++) {
                    double pelletAngle = angle + Math.toRadians((Math.random() - 0.5) * weapon.getSpread());
                    synchronized (projectiles) {
                        projectiles.add(new Projectile(player.getX(), player.getY(), pelletAngle,
                                                    weapon.getProjectileSpeed(),
                                                    weapon.getRange(),
                                                    weapon.getDamage()));
                    }
                }
            } else {
                synchronized (projectiles) {
                    projectiles.add(new Projectile(player.getX(), player.getY(), angle,
                                                weapon.getProjectileSpeed(),
                                                weapon.getRange(),
                                                weapon.getDamage()));
                }
            }
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