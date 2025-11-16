package core;

import java.util.List;
import java.awt.Rectangle;
import entities.Player;
import entities.Projectile;
import utils.Constants;
import utils.Quadtree;
import waves.WaveManager;
import entities.Enemy;

public class GameLoop extends Thread {
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000 / TARGET_FPS;

    private final GamePanel panel;
    private final Player player;
    private final List<Enemy> enemies;
    private final WaveManager waveManager;
    private boolean running = true;

    public GameLoop(GamePanel panel, Player player, List<Enemy> enemies, WaveManager waveManager) {
        this.panel = panel;
        this.player = player;
        this.enemies = enemies;
        this.waveManager = waveManager;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        double delta = 0.0;
        int frames = 0;
        double nsPerFrame = 1_000_000_000.0 / TARGET_FPS;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerFrame;
            lastTime = now;

            if (!player.isAlive() && panel.getGameState() == GameState.PLAYING) {
                panel.setGameState(GameState.GAME_OVER);
            }

            while (delta >= 1) {
                // Phase 1: Update player and wave manager
                player.update();
                waveManager.update();

                // Phase 2: Update enemy movement (no attack yet)
                for (Enemy enemy : enemies) {
                    enemy.updateMovement(player); // movement only
                }

                // Phase 3: Build quadtree from updated enemy positions
                Quadtree quadtree = new Quadtree(0, new Rectangle(0, 0, Constants.MAP_WIDTH, Constants.MAP_HEIGHT));
                for (Enemy e : enemies) quadtree.insert(e);

                // Phase 4: Resolve enemy bounce
                for (Enemy e : enemies) {
                    List<Enemy> nearby = quadtree.query(e.getBounds());
                    for (Enemy other : nearby) {
                        if (e != other && isColliding(e, other)) {
                            resolveBounce(e, other);
                        }
                    }
                }

                // Phase 5: Update projectiles using quadtree
                synchronized (panel.getProjectiles()) {
                    for (int i = panel.getProjectiles().size() - 1; i >= 0; i--) {
                        Projectile p = panel.getProjectiles().get(i);
                        boolean alive = p.update(enemies, quadtree);
                        if (!alive) {
                            panel.getProjectiles().remove(i);
                        }
                    }
                }

                for (Enemy enemy : enemies) {
                    enemy.applyKnockbackMovement(); // after knockback is applied
                }

                // Phase 6: Apply enemy attacks (damage + knockback)
                for (Enemy enemy : enemies) {
                    enemy.attemptAttack(player);
                }

                // Phase 7: Update weapon manager and handle shooting
                panel.getWeaponManager().update();
                if (panel.isMouseDown()) {
                    double angle = panel.getAimAngle();
                    panel.getWeaponManager().tryShoot(player.getX(), player.getY(), angle);
                }

                // Phase 8: Handle enemy deaths
                for (Enemy e : enemies) {
                    if (!e.isAlive()) {
                        e.onDeath();
                    }
                }
                enemies.removeIf(e -> !e.isAlive());

                // Phase 9: Advance wave if needed
                if (panel.getGameState() == GameState.PLAYING && enemies.isEmpty() && !waveManager.isWaveActive()) {
                    waveManager.advanceWave();
                }

                delta--;
            }

            panel.repaint();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("FPS: " + frames);
                frames = 0;
                timer += 1000;
            }

            long elapsed = System.nanoTime() - now;
            long sleepTime = (long)(nsPerFrame - elapsed) / 1_000_000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private boolean isColliding(Enemy a, Enemy b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double distSq = dx * dx + dy * dy;
        double minDist = a.getColliderRadius() + b.getColliderRadius();
        return distSq < minDist * minDist;
    }

    private void resolveBounce(Enemy a, Enemy b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double distSq = dx * dx + dy * dy;
        double minDist = a.getColliderRadius() + b.getColliderRadius();

        if (distSq == 0 || distSq >= minDist * minDist) return;

        double distance = Math.sqrt(distSq);
        double overlap = minDist - distance;
        double pushX = (dx / distance) * (overlap / 2);
        double pushY = (dy / distance) * (overlap / 2);

        a.setPosition(a.getX() - pushX, a.getY() - pushY);
        b.setPosition(b.getX() + pushX, b.getY() + pushY);
    }
}