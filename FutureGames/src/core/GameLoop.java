package core;

import java.util.List;

import javax.swing.*;
import entities.Player;
import entities.Enemy;

public class GameLoop {
    private final JPanel panel;
    private final Player player;
    private final List<Enemy> enemies;
    private final WaveManager waveManager;

    public GameLoop(JPanel panel, Player player, List<Enemy> enemies, WaveManager waveManager) {
        this.panel = panel;
        this.player = player;
        this.enemies = enemies;
        this.waveManager = waveManager;
    }

    public void start() {
        Timer timer = new Timer(16, e -> {
            player.update();

            waveManager.update();

            for (Enemy enemy : enemies) {
                enemy.update(player);
                enemy.attemptAttack(player);
            }

            // Prevent enemy overlap
            for (int i = 0; i < enemies.size(); i++) {
                Enemy a = enemies.get(i);
                for (int j = i + 1; j < enemies.size(); j++) {
                    Enemy b = enemies.get(j);

                    double dx = b.getX() - a.getX();
                    double dy = b.getY() - a.getY();
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    double minDist = a.getColliderRadius() + b.getColliderRadius();

                    if (distance < minDist && distance > 0) {
                        double overlap = minDist - distance;
                        double pushX = (dx / distance) * (overlap / 2);
                        double pushY = (dy / distance) * (overlap / 2);

                        a.setPosition(a.getX() - pushX, a.getY() - pushY);
                        b.setPosition(b.getX() + pushX, b.getY() + pushY);
                    }
                }
            }

            panel.repaint();
        });
        timer.start();
    }
}