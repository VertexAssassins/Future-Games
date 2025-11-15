package core;

import entities.Enemy;

public interface EnemySpawnRule {
    int getSpawnCount(int waveNumber);
    Class<? extends Enemy> getEnemyType();
}