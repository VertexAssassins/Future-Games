package waves;

import entities.Enemy;
import entities.Player;
import utils.Constants;
import java.util.List;

import core.GameWorld;

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

    public Enemy spawnNext(Player player, GameWorld world) {
        for (SpawnRequest request : spawnRequests) {
            if (request.getCount() > 0) {
                try {
                    // 1. Create a temp enemy to get its collider radius
                    Enemy temp = request.getType()
                        .getConstructor(double.class, double.class, GameWorld.class)
                        .newInstance(0, 0, world);

                    double radius = temp.getColliderRadius();

                    // 2. Get a valid spawn location
                    double[] coords = getRandomSpawnCoordinates(player, world, radius);

                    // 3. Spawn the real enemy
                    Enemy enemy = request.getType()
                        .getConstructor(double.class, double.class, GameWorld.class)
                        .newInstance(coords[0], coords[1], world);

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

    private double[] getRandomSpawnCoordinates(Player player, GameWorld world, double enemyRadius) {
        double safeDistance = 800;
        double px = player.getX();
        double py = player.getY();

        double x, y;
        int attempts = 0;

        while (attempts < 40) {
            x = Math.random() * Constants.MAP_WIDTH;
            y = Math.random() * Constants.MAP_HEIGHT;

            double dx = x - px;
            double dy = y - py;
            double distance = Math.sqrt(dx * dx + dy * dy);

            boolean farEnough = distance >= safeDistance;
            boolean notInsideObject = !world.collidesCircle(x, y, enemyRadius);

            if (farEnough && notInsideObject) {
                return new double[]{x, y};
            }

            attempts++;
        }
        // fallback
        return new double[]{Math.random() * Constants.MAP_WIDTH, Math.random() * Constants.MAP_HEIGHT};
    }

    public boolean isFinished() {
        return totalSpawned >= totalToSpawn;
    }
}