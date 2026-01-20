package waves;

import entities.Enemy;

import java.util.ArrayList;
import java.util.List;

import gameModificationCards.*;
import entities.Player;
import entities.Brute;
import entities.FlamingSkull;
import entities.Zombie;
import entities.Rat;
import entities.Rabid;

public class WaveManager {
    private final List<Enemy> enemies;
    private final Player player;
    private final WaveBlueprint blueprint = new WaveBlueprint();
    private Wave currentWave;
    private int currentWaveNumber = 0;
    private boolean justReset = false;

    public int getWaveNumber() {
        return currentWaveNumber;
    }

    public WaveManager(List<Enemy> enemies, Player player) {
        this.enemies = enemies;
        this.player = player;

        // Zombie enemy: increases by 4 * (1.25 ^ waveNum)
        blueprint.addRule(blueprint.new ConditionalSpawnRule(Zombie.class, waveNum -> (int)(4 * Math.pow(1.25, waveNum))));
        // Brute enemy: 25% chance to spawn. sqrt(waveNum) + 1
        blueprint.addRule(
            blueprint.new ConditionalSpawnRule(
                Brute.class,
                waveNum -> {
                    if (waveNum <= 5)
                        return 0; // too early, don't spawn

                    // 1 in 4 chance
                    if (Math.random() > 0.25)
                        return 0;

                    // Spawn amount
                    return (int) Math.ceil(Math.sqrt(waveNum) + 1);
                }
            )
        );
        // Flaming Skull enemy: 20% chance to spawn. (waveNum^2 / 4) + 5
        blueprint.addRule(blueprint.new ConditionalSpawnRule(
                FlamingSkull.class,
                waveNum -> {
                    if (waveNum <= 3)
                        return 0; // too early, don't spawn

                    // 1 in 5 chance
                    if (Math.random() > (1.0 / 5.0))
                        return 0;

                    // Spawn amount
                    return (int) Math.ceil(40 * (1 - Math.exp(-0.12 * waveNum)));
                }
            )
        );
        // Rat enemy: one in 6 chance to spawn. (waveNum^2 / 3) + 20
        blueprint.addRule(blueprint.new ConditionalSpawnRule(
                Rat.class,
                waveNum -> {
                    if (waveNum <= 5)
                        return 0; // too early, don't spawn

                    // 1 in 6 chance
                    if (Math.random() > (1.0 / 6.0))
                        return 0;

                    // Spawn amount
                    return (int) Math.ceil(200 * (1 - Math.exp(-0.021 * waveNum)));
                }
            )
        );
        // Rabid enemy: one in 6 chance to spawn. (waveNum^2 / 5) + 10
        blueprint.addRule(blueprint.new ConditionalSpawnRule(
                Rabid.class,
                waveNum -> {
                    if (waveNum <= 5)
                        return 0; // too early, don't spawn

                    // 1 in 6 chance
                    if (Math.random() > (1.0 / 6.0))
                        return 0;

                    // Spawn amount
                    return (int) Math.ceil(30 * (1 - Math.exp(-0.11 * waveNum)));
                }
            )
        );
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
        if (justReset) {
            justReset = false;
            return;
        }

        currentWaveNumber++;
        List<Wave.SpawnRequest> requests = blueprint.generateRequests(currentWaveNumber);
        double spawnRate = 0.25; // or scale with waveNumber
        startWave(new Wave(requests, spawnRate));
    }

    public List<ModifierCard> generateCards() {
        List<ModifierType> goodList = ModifierLibrary.GOOD_VALUES.keySet().stream().toList();
        List<ModifierType> badList  = ModifierLibrary.BAD_VALUES.keySet().stream().toList();

        List<ModifierCard> cards = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Rarity rarity = rollRarity();
            Modifier good = ModifierLibrary.create(randomFrom(goodList), rarity);
            Modifier bad  = ModifierLibrary.create(randomFrom(badList), rarity);

            cards.add(new ModifierCard(rarity, good, bad));
        }

        return cards;
    }

    private Rarity rollRarity() {
        double r = Math.random();
        if (r < 0.60) return Rarity.STANDARD;
        if (r < 0.90) return Rarity.UNCOMMON;
        return Rarity.RARE;
    }

    private <T> T randomFrom(List<T> list) {
        return list.get((int)(Math.random() * list.size()));
    }


    public void reset() {
        currentWaveNumber = 0;
        currentWave = null;
        justReset = true;
    }
}