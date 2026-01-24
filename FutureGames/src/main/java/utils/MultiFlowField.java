package utils;

import core.GameWorld;
import entities.Player;

public class MultiFlowField {

    private static final int MAX_LANES = 6;   // upper bound
    private static final int MAX_PER_LANE = 10;

    private final FlowField[] lanes;
    private int activeLanes = 1;

    public MultiFlowField() {
        lanes = new FlowField[MAX_LANES];
        for (int i = 0; i < MAX_LANES; i++) {
            lanes[i] = new FlowField(); // your existing FlowField
        }
    }

    public int getMaxPerLane() {
        return MAX_PER_LANE;
    }

    public int getActiveLanes() {
        return activeLanes;
    }

    public FlowField getLane(int laneIndex) {
        if (laneIndex < 0) laneIndex = 0;
        if (laneIndex >= activeLanes) laneIndex = activeLanes - 1;
        return lanes[laneIndex];
    }

    public void rebuildAll(GameWorld world, Player player, int enemyCount) {
        // how many lanes do we actually need?
        activeLanes = Math.min(MAX_LANES,
                (enemyCount + MAX_PER_LANE - 1) / MAX_PER_LANE);
        if (activeLanes <= 0) activeLanes = 1;

        for (int i = 0; i < activeLanes; i++) {
            double offsetX = 0;
            double offsetY = 0;

            // simple pattern: spread around player
            switch (i % 4) {
                case 0: offsetX =  40; offsetY =  0;  break;
                case 1: offsetX = -40; offsetY =  0;  break;
                case 2: offsetX =  0;  offsetY =  40; break;
                case 3: offsetX =  0;  offsetY = -40; break;
            }

            double tx = player.getX() + offsetX;
            double ty = player.getY() + offsetY;

            lanes[i].rebuild(world, player, tx, ty);
        }
    }
}