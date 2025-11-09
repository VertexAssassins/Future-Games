package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import entities.Player;
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

            while(delta >= 1) {
            player.update();
            waveManager.update();

            for (Enemy enemy : enemies) {
                enemy.update(player);
                enemy.attemptAttack(player);
            }

            // --- Grid-based collision ---
            int cellSize = 100; // adjust based on enemy size
            Map<Long, List<Enemy>> grid = new HashMap<>();

            // Place enemies into grid cells
            for (Enemy e : enemies) {
                int cx = (int)(e.getX() / cellSize);
                int cy = (int)(e.getY() / cellSize);
                long key = (((long) cx) << 32) | (cy & 0xffffffffL);
                grid.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }

            // Check collisions only within same + neighboring cells
            for (var entry : grid.entrySet()) {

                long key = entry.getKey();
                int cx = (int)(key >> 32);
                int cy = (int) key;

                for (int nx = -1; nx <= 1; nx++) {
                    for (int ny = -1; ny <= 1; ny++) {
                        long neighborKey = (((long) (cx + nx)) << 32) | ((cy + ny) & 0xffffffffL);
                        List<Enemy> cellEnemies = grid.get(neighborKey);
                        if (cellEnemies == null) continue;

                        for (Enemy a : entry.getValue()) {
                            for (Enemy b : cellEnemies) {
                                if (a == b) continue;

                                double dx = b.getX() - a.getX();
                                double dy = b.getY() - a.getY();
                                double distSq = dx * dx + dy * dy;
                                double minDist = a.getColliderRadius() + b.getColliderRadius();

                                if (distSq < minDist * minDist && distSq > 0) {
                                    double distance = Math.sqrt(distSq);
                                    double overlap = minDist - distance;
                                    double pushX = (dx / distance) * (overlap / 2);
                                    double pushY = (dy / distance) * (overlap / 2);

                                    a.setPosition(a.getX() - pushX, a.getY() - pushY);
                                    b.setPosition(b.getX() + pushX, b.getY() + pushY);
                                }
                            }
                        }
                    }
                }
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
            if(sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}