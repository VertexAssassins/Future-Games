package waves;

import entities.Enemy;

import java.util.ArrayList;
import java.util.List;

import core.EnemySpawnRule;

public class WaveBlueprint {
    private final List<EnemySpawnRule> rules = new ArrayList<>();

    public void addRule(EnemySpawnRule rule) {
        rules.add(rule);
    }

    public List<Wave.SpawnRequest> generateRequests(int waveNumber) {
        List<Wave.SpawnRequest> requests = new ArrayList<>();
        for (EnemySpawnRule rule : rules) {
            int count = rule.getSpawnCount(waveNumber);
            if (count > 0) {
                requests.add(new Wave.SpawnRequest(rule.getEnemyType(), count));
            }
        }
        return requests;
    }

    public class ConditionalSpawnRule implements EnemySpawnRule {
    private final Class<? extends Enemy> type;
    private final java.util.function.IntFunction<Integer> logic;

        public ConditionalSpawnRule(Class<? extends Enemy> type, java.util.function.IntFunction<Integer> logic) {
            this.type = type;
            this.logic = logic;
        }

        public int getSpawnCount(int waveNumber) {
            return logic.apply(waveNumber);
        }

        public Class<? extends Enemy> getEnemyType() {
            return type;
        }
    } 
}