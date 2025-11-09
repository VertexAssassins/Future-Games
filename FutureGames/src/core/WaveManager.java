package core;

import entities.Enemy;
import java.util.List;
import entities.Player;

public class WaveManager {
    private final List<Enemy> enemies;
    private final Player player;
    private Wave currentWave;

    public WaveManager(List<Enemy> enemies, Player player) {
        this.enemies = enemies;
        this.player = player;
    }

    public void startWave(Wave wave) {
        this.currentWave = wave;
    }

    public void update() {
        if (currentWave != null && currentWave.shouldSpawn()) {
            Enemy enemy = currentWave.spawnNext(player);
            if (enemy != null) {
                enemies.add(enemy);
            }
        }

        if (currentWave != null && currentWave.isFinished()) {
            currentWave = null; // ready for next wave
        }
    }
}