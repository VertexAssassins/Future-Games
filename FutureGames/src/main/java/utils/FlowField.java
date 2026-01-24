package utils;

import core.GameWorld;
import entities.Player;

import java.awt.*;
import java.util.ArrayDeque;

public class FlowField {

    private static final int CELL_SIZE = 20;

    private final int gridW;
    private final int gridH;

    private final float[][] cost;
    private final float[][] dirX;
    private final float[][] dirY;

    public FlowField() {
        this.gridW = Constants.MAP_WIDTH / CELL_SIZE;
        this.gridH = Constants.MAP_HEIGHT / CELL_SIZE;

        this.cost = new float[gridW][gridH];
        this.dirX = new float[gridW][gridH];
        this.dirY = new float[gridW][gridH];
    }

    public int getCellSize() { return CELL_SIZE; }

    public float sampleDirX(double worldX, double worldY) {
        int gx = (int)(worldX / CELL_SIZE);
        int gy = (int)(worldY / CELL_SIZE);
        if (gx < 0 || gy < 0 || gx >= gridW || gy >= gridH) return 0;
        return dirX[gx][gy];
    }

    public float sampleDirY(double worldX, double worldY) {
        int gx = (int)(worldX / CELL_SIZE);
        int gy = (int)(worldY / CELL_SIZE);
        if (gx < 0 || gy < 0 || gx >= gridW || gy >= gridH) return 0;
        return dirY[gx][gy];
    }

    public void rebuild(GameWorld world, Player player, double targetX, double targetY) {
        int startX = (int)(targetX / CELL_SIZE);
        int startY = (int)(targetY / CELL_SIZE);

        if (startX < 0 || startY < 0 || startX >= gridW || startY >= gridH) {
            return;
        }

        // 1. reset costs
        for (int x = 0; x < gridW; x++) {
            for (int y = 0; y < gridH; y++) {
                cost[x][y] = Float.POSITIVE_INFINITY;
                dirX[x][y] = 0;
                dirY[x][y] = 0;
            }
        }

        ArrayDeque<Point> queue = new ArrayDeque<>();
        cost[startX][startY] = 0;
        queue.add(new Point(startX, startY));

        int[][] dirs = { {1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1} };

        // 2. BFS distance field
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            float baseCost = cost[p.x][p.y];

            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];

                if (nx < 0 || ny < 0 || nx >= gridW || ny >= gridH) continue;

                if (isBlockedCell(world, nx, ny)) continue;

                if (cost[nx][ny] > baseCost + 1) {
                    cost[nx][ny] = baseCost + 1;
                    queue.add(new Point(nx, ny));
                }
            }
        }

        // 3. build direction vectors
        buildDirections();
    }

    private boolean isBlockedCell(GameWorld world, int gx, int gy) {
        double cx = gx * CELL_SIZE + CELL_SIZE * 0.5;
        double cy = gy * CELL_SIZE + CELL_SIZE * 0.5;
        double r  = CELL_SIZE * 0.75;
        return world.collidesCircle(cx, cy, r);
    }

    private void buildDirections() {
        for (int x = 0; x < gridW; x++) {
            for (int y = 0; y < gridH; y++) {
                float best = cost[x][y];
                float bx = 0, by = 0;

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;

                        int nx = x + dx;
                        int ny = y + dy;

                        if (nx < 0 || ny < 0 || nx >= gridW || ny >= gridH) continue;

                        if (cost[nx][ny] < best) {
                            best = cost[nx][ny];
                            bx = dx;
                            by = dy;
                        }
                    }
                }

                float len = (float)Math.sqrt(bx*bx + by*by);
                if (len > 0) {
                    dirX[x][y] = bx / len;
                    dirY[x][y] = by / len;
                } else {
                    dirX[x][y] = 0;
                    dirY[x][y] = 0;
                }
            }
        }
    }
}
