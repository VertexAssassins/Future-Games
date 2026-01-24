package core;

import java.util.List;
import java.awt.Rectangle;
import entities.Player;
import entities.Projectile;
import gameModificationCards.ModifierCard;
import utils.BloodSplatter;
import utils.Constants;
import utils.MusicManager;
import utils.Quadtree;
import waves.WaveManager;
import entities.Enemy;
import entities.HealthPickup;

public class GameLoop extends Thread {
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000 / TARGET_FPS;

    private final GamePanel panel;
    private final Player player;
    private final List<Enemy> enemies;
    private final WaveManager waveManager;
    private boolean running = true;
    private final MusicManager musicManager;

    public GameLoop(GamePanel panel, Player player, List<Enemy> enemies, WaveManager waveManager, MusicManager musicManager) {
        this.panel = panel;
        this.player = player;
        this.enemies = enemies;
        this.waveManager = waveManager;
        this.musicManager = musicManager;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        double delta = 0.0;
        int frames = 0;
        double nsPerFrame = 1_000_000_000.0 / TARGET_FPS;

        while (running) {
            long now = System.nanoTime();
            double frameDelta = (now - lastTime) / nsPerFrame;
            lastTime = now;

            // Only accumulate delta while actively playing
            if (panel.getGameState() == GameState.PLAYING) {
                delta += frameDelta;
            }

            // Handle death → GAME_OVER
            if (!player.isAlive() && panel.getGameState() == GameState.PLAYING) {
                panel.setGameState(GameState.GAME_OVER);
            }

            // Advance game logic only in PLAYING
            if (panel.getGameState() == GameState.PLAYING) {
                while (delta >= 1.0) {
                    updateGameLogic();
                    delta -= 1.0;
                }
            }

            // Always repaint current state (PLAYING, PAUSED, SHOP, GAME_OVER)
            panel.repaint();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("FPS: " + frames + " | state=" + panel.getGameState());
                frames = 0;
                timer += 1000;
            }

            long elapsed = System.nanoTime() - now;
            long sleepTime = (long)(nsPerFrame - elapsed) / 1_000_000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void updateGameLogic() {

        player.update();
        waveManager.update();

        for (BloodSplatter b : panel.getBloodEffects()) {
            b.update();
        }
        panel.getBloodEffects().removeIf(BloodSplatter::isFinished);

        for (Enemy enemy : enemies) {
            enemy.update();
        }

        Quadtree quadtree = buildQuadtree();

        updateEnemyMovement(panel.getWorld(), quadtree);
        applyKnockbackMovement();

        updateWeaponsAndShooting();

        resolveEnemyBounce(quadtree);
        quadtree = rebuildQuadtree(); // final rebuild before projectile update

        updateProjectiles(quadtree);
        
        applyEnemyAttacks();
        checkPickupCollisions();
        handleEnemyDeaths();
        maybeAdvanceWave();
    }

    private void updateEnemyMovement(GameWorld world, Quadtree quadtree) {
        for (Enemy enemy : enemies) {
            enemy.updateMovement(player, world, quadtree.query(enemy.getBounds()));
        }
    }

    private Quadtree buildQuadtree() {
        Quadtree quadtree = new Quadtree(0, new Rectangle(0, 0, Constants.MAP_WIDTH, Constants.MAP_HEIGHT));
        for (Enemy e : enemies) quadtree.insert(e);
        return quadtree;
    }

    private Quadtree rebuildQuadtree() {
        return buildQuadtree(); // reuse logic
    }

    private void resolveEnemyBounce(Quadtree quadtree) {
        for (Enemy e : enemies) {
            List<Enemy> nearby = quadtree.query(e.getBounds());
            for (Enemy other : nearby) {
                if (e != other && isColliding(e, other)) {
                    resolveBounce(e, other);
                }
            }
        }
    }

    private void applyKnockbackMovement() {
        for (Enemy enemy : enemies) {
            enemy.applyKnockbackMovement();
        }
    }

    private void updateProjectiles(Quadtree quadtree) {
        synchronized (panel.getProjectiles()) {
            List<Projectile> projectiles = panel.getProjectiles();

            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile p = projectiles.get(i);

                // Step 1: move projectile
                boolean stillAlive = p.move();
                if (!stillAlive) {
                    projectiles.remove(i);
                    continue;
                }

                // Step 2: check collision with fresh quadtree
                List<Enemy> candidates = quadtree.query(p.getSweptAABB());
                if (p.checkCollisions(candidates)) {
                    projectiles.remove(i);
                }

            System.out.println("Projectile at (" + p.getX() + ", " + p.getY() + ") queried " + candidates.size() + " enemies.");
            
            }
        }
    }

    private void applyEnemyAttacks() {
        for (Enemy enemy : enemies) {
            enemy.attemptAttack(player);
        }
    }

    private void updateWeaponsAndShooting() {
        panel.getWeaponManager().update();
        if (panel.isMouseDown()) {
            double angle = panel.getAimAngle();
            panel.getWeaponManager().tryShoot(player.getX(), player.getY(), angle);
        }
    }

    private void handleEnemyDeaths() {
        for (Enemy e : enemies) {
            if (!e.isAlive() && !e.isDeathHandled()) {

                // 5% drop chance
                if (Math.random() < 0.05) {
                    int amount = rollHealthAmount();
                    panel.getHealthPickups().add(
                        new HealthPickup(e.getX(), e.getY(), amount)
                    );
                }

                e.onDeath(player, panel);
                e.markDeathHandled();
            }
        }

        // remove AFTER effects are spawned
        enemies.removeIf(e -> !e.isAlive());
    }

    private void checkPickupCollisions() {
        List<HealthPickup> pickups = panel.getHealthPickups();

        for (int i = pickups.size() - 1; i >= 0; i--) {
            HealthPickup hp = pickups.get(i);

            Rectangle playerBounds = new Rectangle(
                (int)(player.getX() - player.getSize() / 2),
                (int)(player.getY() - player.getSize() / 2),
                player.getSize(),
                player.getSize()
            );

            if (playerBounds.intersects(hp.getBounds())) {
                player.heal(hp.getAmount());
                pickups.remove(i);
            }
        }
    }

    private int rollHealthAmount() {
        double r = Math.random();

        if (r < 0.35) return 5;   // 35%
        if (r < 0.65) return 10;  // next 30%
        if (r < 0.85) return 15;  // next 20%
        return 20;                // last 15%
    }

    private void maybeAdvanceWave() {

        // If wave is finished and enemies are gone
        if (panel.getGameState() == GameState.PLAYING &&
            enemies.isEmpty() &&
            !waveManager.isWaveActive()) {

            // If at least Wave 1 has been completed → show cards
            if (waveManager.getWaveNumber() >= 1) {
                List<ModifierCard> cards = waveManager.generateCards();
                panel.showCardSelection(cards);
                panel.setGameState(GameState.CARD_SELECTION);
                return;
            }

            // Otherwise (waveNumber == 0) → start Wave 1
            waveManager.advanceWave();
        }
    }

    private boolean isColliding(Enemy a, Enemy b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double distSq = dx * dx + dy * dy;
        double minDist = a.getColliderRadius() + b.getColliderRadius();
        return distSq < minDist * minDist;
    }

    private void resolveBounce(Enemy a, Enemy b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double distSq = dx * dx + dy * dy;
        double minDist = a.getColliderRadius() + b.getColliderRadius();

        if (distSq == 0 || distSq >= minDist * minDist) return;

        double distance = Math.sqrt(distSq);
        double overlap = minDist - distance;
        double pushX = (dx / distance) * (overlap / 2);
        double pushY = (dy / distance) * (overlap / 2);

        a.setPosition(a.getX() - pushX, a.getY() - pushY);
        b.setPosition(b.getX() + pushX, b.getY() + pushY);
    }
}