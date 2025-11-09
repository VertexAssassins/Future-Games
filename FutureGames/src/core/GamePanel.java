package core;

import javax.swing.*;
import java.awt.*;

import entities.Enemy;
import entities.FastEnemy;
import entities.Player;
import entities.TankEnemy;
import input.KeyBindings;

import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private final Player player = new Player();
    private final CameraManager camera = new CameraManager(player);
    private final List<Enemy> enemies = new ArrayList<>();
    private final WaveManager waveManager = new WaveManager(enemies, player);

    public GamePanel() {
        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.BLACK);
        setFocusable(true);
        KeyBindings.setup(this, player);
        new GameLoop(this, player, enemies, waveManager).start();

        Timer waveStartTimer = new Timer(3000, e -> {
            List<Wave.SpawnRequest> requests = List.of(
                new Wave.SpawnRequest(FastEnemy.class, 20),
                new Wave.SpawnRequest(TankEnemy.class, 10)
            );
        waveManager.startWave(new Wave(requests, 0.5)); // 0.5s per enemy → 30 enemies over 15s
        });
        waveStartTimer.setRepeats(false);
        waveStartTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Enemy enemy : enemies) {
            enemy.update(player);
            enemy.draw(g, camera);
        }
        player.draw(g, camera);
    }
}