package core;

import entities.Enemy;
import entities.Player;

public interface EnemySpawnRule {
    int getSpawnCount(int waveNumber);
    Class<? extends Enemy> getEnemyType();
}