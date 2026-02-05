package core;

import entities.Enemy;

// Interface defining enemy spawn rules
public interface EnemySpawnRule {
    int getSpawnCount(int waveNumber);
    Class<? extends Enemy> getEnemyType();
}