package core;

import entities.Enemy;
import entities.FastEnemy;

import java.util.List;

import entities.Player;
import entities.TankEnemy;

public class WaveManager {
    private final List<Enemy> enemies;
    private final Player player;
    private final WaveBlueprint blueprint = new WaveBlueprint();
    private Wave currentWave;
    private int currentWaveNumber = 0;

    public int getWaveNumber() {
        return currentWaveNumber;
    }

    public WaveManager(List<Enemy> enemies, Player player) {
        this.enemies = enemies;
        this.player = player;

        blueprint.addRule(blueprint.new ConditionalSpawnRule(FastEnemy.class, waveNum -> waveNum * 10));
        blueprint.addRule(blueprint.new ConditionalSpawnRule(TankEnemy.class, waveNum -> (waveNum % 5 == 0 ? 3 : 0) + (waveNum % 10 == 0 ? 6 : 0)));
    }

    public void startWave(Wave wave) {
        this.currentWave = wave;
    }

    public boolean isWaveActive() {
        return currentWave != null;
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

    public void advanceWave() {
        currentWaveNumber++;
        List<Wave.SpawnRequest> requests = blueprint.generateRequests(currentWaveNumber);
        double spawnRate = 0.25; // or scale with waveNumber
        startWave(new Wave(requests, spawnRate));
    }


    public void reset() {
        currentWave = null;
        currentWaveNumber = 1;
    }
}