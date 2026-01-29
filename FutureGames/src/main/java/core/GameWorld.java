package core;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import java.util.List;

public class GameWorld {

    private final int tileSize = 32; // pixels

    private final int worldWidth;   // in pixels
    private final int worldHeight;  // in pixels

    private final int tilesX;       // worldWidth  / tileSize
    private final int tilesY;       // worldHeight / tileSize

    public static boolean DEBUG_COLLISION = false;

    private int[][] tileMap;        // tile indices
    private Image[] groundTiles;    // the 5 tile images
    private List<WorldObject> objects = new ArrayList<>();
    private List<Image> staticObjectImages = new ArrayList<>();

    public List<WorldObject> getObjects() { return objects; }
    public int getWidth() { return worldWidth; }
    public int getHeight() { return worldHeight; }

    public GameWorld(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        this.tilesX = worldWidth / tileSize;
        this.tilesY = worldHeight / tileSize;

        loadTiles();
        generateTileMap();

        loadStaticObjects();
        spawnObjects();
    }

    private void loadTiles() {
        groundTiles = new Image[5];

        try {
            for (int i = 0; i < 5; i++) {
                groundTiles[i] = ImageIO.read(
                    getClass().getResource("/world/tiles/Ground_rocks-" + (i+1) + ".png")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateTileMap() {
        tileMap = new int[tilesX][tilesY];
        Random r = new Random();

        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                tileMap[x][y] = r.nextInt(5); // 0–4
            }
        }
    }

    private void loadStaticObjects() {
        try {
            //Every object image to be loaded
            String[] names = {
                "Hand-1.png",
                "Hand-2.png",
                "Rock.png",
                "Ruins.png",
                "Skulls.png",
                "Tombstone-1.png",
                "Tombstone-2.png",
                "Trunk.png"
            };

            for (String name : names) {
                Image img = ImageIO.read(
                    getClass().getResource("/world/objects/" + name)
                );
                staticObjectImages.add(img);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean collidesCircle(double px, double py, double pr) {
        for (WorldObject obj : objects) {

            double ox = obj.x;
            double oy = obj.y - (obj.height * 0.5); // adjust for centerY

            double dx = px - ox;
            double dy = py - oy;

            // WRAP horizontally
            if (dx >  worldWidth / 2) dx -= worldWidth;
            if (dx < -worldWidth / 2) dx += worldWidth;

            // WRAP vertically
            if (dy >  worldHeight / 2) dy -= worldHeight;
            if (dy < -worldHeight / 2) dy += worldHeight;

            double distSq = dx*dx + dy*dy;
            double combined = pr + obj.collisionRadius;

            if (distSq < combined * combined) {
                return true;
            }
        }
        return false;
    }

    private void spawnObjects() {
        Random r = new Random();
        
        Rectangle playerStart = new Rectangle(300, 200, 80, 80);

        for (Image img : staticObjectImages) {

            int w = img.getWidth(null);
            int h = img.getHeight(null);
            int maxDim = Math.max(w, h);

            int spawnCount;

            if (maxDim > 96) spawnCount = 1;
            else if (maxDim > 64) spawnCount = 4;
            else spawnCount = 15;

            for (int i = 0; i < spawnCount; i++) {

                double x, y;
                Rectangle objRect;

                do {
                    x = r.nextInt(worldWidth - w);
                    y = r.nextInt(worldHeight - h);

                    objRect = new Rectangle((int)x, (int)y, w, h);

                } while (objRect.intersects(playerStart));

                double centerX = x + w / 2.0;
                double centerY = y + h / 2.0;

                objects.add(new StaticObject(centerX, centerY, img));
            }
        }
    }

    public void draw(Graphics2D g2, double camX, double camY, int screenW, int screenH) {

        int startX = (int)(camX / tileSize) - 2;
        int startY = (int)(camY / tileSize) - 2;

        int endX = startX + (screenW / tileSize) + 4;
        int endY = startY + (screenH / tileSize) + 4;

        for (int tx = startX; tx < endX; tx++) {
            for (int ty = startY; ty < endY; ty++) {

                // Wrap tile coordinates
                int wx = ((tx % tilesX) + tilesX) % tilesX;
                int wy = ((ty % tilesY) + tilesY) % tilesY;

                int tileIndex = tileMap[wx][wy];

                int screenX = (int)(tx * tileSize - camX);
                int screenY = (int)(ty * tileSize - camY);

                g2.drawImage(groundTiles[tileIndex], screenX, screenY, tileSize, tileSize, null);
            }
        }
    }

}