package waves;

import entities.Enemy;
import entities.Player;
import utils.Constants;
import java.util.List;

public class Wave {
    public static class SpawnRequest {
    private final Class<? extends Enemy> type;
    private int count;

    public SpawnRequest(Class<? extends Enemy> type, int count) {
        this.type = type;
        this.count = count;
    }

    public Class<? extends Enemy> getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public void decrementCount() {
        count--;
    }
}

    private final List<SpawnRequest> spawnRequests;
    private final double spawnRate; // seconds per enemy
    private final long startTime;
    private int totalToSpawn;
    private int totalSpawned = 0;

    public Wave(List<SpawnRequest> spawnRequests, double spawnRate) {
        this.spawnRequests = spawnRequests;
        this.spawnRate = spawnRate;
        this.startTime = System.currentTimeMillis();
        this.totalToSpawn = spawnRequests.stream().mapToInt(SpawnRequest::getCount).sum();
    }

    public boolean shouldSpawn() {
        long elapsed = System.currentTimeMillis() - startTime;
        double expectedSpawnCount = elapsed / (spawnRate * 1000.0);
        return totalSpawned < expectedSpawnCount && totalSpawned < totalToSpawn;
    }

    public Enemy spawnNext(Player player) {
        for (SpawnRequest request : spawnRequests) {
            if (request.getCount() > 0) {
                try {
                    double[] coords = getRandomSpawnCoordinates(player);
                    Enemy enemy = request.getType()
                        .getConstructor(double.class, double.class)
                        .newInstance(coords[0], coords[1]);
                    request.decrementCount();
                    totalSpawned++;
                    return enemy;
                } catch (Exception e) {
                    e.printStackTrace();
                    }
                }
            }
        return null;
    }

    private double[] getRandomSpawnCoordinates(Player player) {
        double safeDistance = 800; // optional: avoid spawning too close
        double px = player.getX();
        double py = player.getY();

        double x, y;
        double distance = 0;
        int attempts = 0;

        do {
            x = Math.random() * Constants.MAP_WIDTH;
            y = Math.random() * Constants.MAP_HEIGHT;

            double dx = x - px;
            double dy = y - py;
            distance = Math.sqrt(dx * dx + dy * dy);

            attempts++;
            if (attempts > 10) break;
        } while (distance < safeDistance);

        return new double[] { x, y };
    }

    public boolean isFinished() {
        return totalSpawned >= totalToSpawn;
    }
}