src/
    Main.java
    assets/
        player.png
            -   Placeholder for Player Character Sprite
    core/
        CameraManager.java
            - Keeps the player visually centered on the screen by calculating camera offsets.
            - Provides getOffsetX() and getOffsetY() to shift the rendering origin based on the player's position.
            - Uses screen dimensions from Constants to determine how far to offset the view.
            - Called by rendering systems (like GamePanel) to draw entities relative to the camera.
            - Designed to be lightweight and stateless — it simply reflects the player's current position.
        EnemySpawnRule.java
            - Defines a contract for specifying how many enemies of a given type should spawn in a wave.
            - Used by WaveBlueprint to modularly generate wave compositions.
            - Enables scalable and customizable spawn logic per enemy type.
            - Method getSpawnCount(int waveNumber) returns how many enemies to spawn based on the wave number.
            - Method getEnemyType() specifies the class of enemy this rule applies to.
            - Implemented by classes like ConditionalSpawnRule to encapsulate dynamic spawn behavior.
        GameFrame.java
            - Serves as the main application window for the game.
            - Extends JFrame to create a fixed-size, non-resizable window titled "Future Games".
            - Initializes and displays the StartScreen component as the first view.
            - Uses pack() to size the window based on its contents.
            - Centers the window on the screen with setLocationRelativeTo(null).
            - Sets default close behavior to exit the application when the window is closed.
        GameLoop.java
            - Core update thread that runs the game at a fixed target frame rate (60 FPS).
            - Continuously updates game entities: player, enemies, weapons, and projectiles.
            - Manages automatic shooting when the mouse is held down.
            - Handles enemy AI updates and attack logic.
            - Implements grid-based spatial partitioning to optimize enemy collision detection.
            - Detects and resolves collisions between overlapping enemies using physics-based separation.
            - Updates and removes projectiles based on collisions and range limits.
            - Tracks player death and transitions to GAME_OVER state.
            - Automatically advances to the next wave when all enemies are defeated and no wave is active.
            - Repaints the game panel each frame to reflect updated visuals.
            - Logs real-time FPS to the console for performance monitoring.
            - Sleeps the thread to maintain consistent frame timing and avoid CPU overuse.
        GamePanel.java
            - Acts as the main gameplay surface, responsible for rendering all game entities and handling input.
            - Manages core game components: Player, CameraManager, WaveManager, WeaponManager, Enemy list, and Projectile list.
            - Handles mouse input for aiming and shooting, including tracking mouse position and button state.
            - Initializes key bindings for weapon switching via KeyBindings.setup().
            - Starts the GameLoop thread to continuously update and repaint the game.
            - Calculates aiming angle based on mouse position and camera offset.
            - Draws the player, enemies, projectiles, wave number, and game-over UI elements.
            - Implements a retry button that resets the game state when clicked after death.
            - Provides attemptShoot() and spawnProjectile() methods to coordinate shooting logic.
            - Resets all game components in resetGame() to restart the game cleanly.
            - Uses paintComponent() to render the current frame, including dynamic UI and entity visuals.
        GameState.java
            - Defines the current status of the game using an enum.
            - PLAYING: Indicates the game is actively running and the player is alive.
            - GAME_OVER: Indicates the player has died and the game has ended.
            - Used by GamePanel and GameLoop to control rendering, input handling, and wave progression.
            - Enables clean state transitions and UI logic (e.g., showing the retry button on death).
        StartScreen.java
            - Displays the initial UI screen when the game launches.
            - Extends JPanel and sets up a custom layout with manual positioning (null layout).
            - Renders the game title: "DEATH IS AN OPPERTUNITY" in large, centered white text.
            - Provides two interactive buttons:
            - Start: Begins the game by replacing the current panel with a new GamePanel.
            - Quit: Exits the application immediately.
            - Uses fixed dimensions (1200 × 800) and a black background for a dramatic intro screen.
            - Acts as the entry point for transitioning into gameplay.
        Wave.java
            - Represents a single wave of enemies to be spawned during gameplay.
            - Contains a list of SpawnRequest objects, each defining an enemy type and how many of that type to spawn.
            - Controls the spawn rate (in seconds per enemy) and tracks how many enemies have been spawned so far.
            - Uses shouldSpawn() to determine if it's time to spawn the next enemy based on elapsed time and spawn rate.
            - spawnNext(Player player) instantiates the next enemy using reflection and places it at a safe distance from the player.
            - Ensures enemies spawn outside the visible map edges and not too close to the player using getSafeSpawnCoordinates().
            - isFinished() returns true when all enemies in the wave have been spawned.
            - Inner class SpawnRequest encapsulates:
            - The enemy type (Class<? extends Enemy>)
            - The number of enemies to spawn
            - A method to decrement the remaining count
        WaveBlueprint.java
            - Defines a blueprint for generating enemy spawn requests for each wave.
            - Holds a list of EnemySpawnRule objects, each describing how a specific enemy type should scale with wave number.
            - Method addRule() allows modular addition of new enemy spawn behaviors.
            - Method generateRequests(int waveNumber) returns a list of Wave.SpawnRequest objects based on the current wave and active rules.
            - Inner class ConditionalSpawnRule implements EnemySpawnRule:
            - Accepts an enemy type and a lambda-based spawn logic.
            - Calculates spawn count dynamically using IntFunction<Integer> based on wave number.
            - Enables flexible, scalable wave composition without hardcoding enemy counts.
            - Designed for easy expansion — new enemy types and spawn conditions can be added with a single line.
        WaveManager.java
            - Controls the lifecycle and progression of enemy waves during gameplay.
            - Maintains the current wave number and tracks whether a wave is active.
            - Uses a WaveBlueprint to define scalable spawn rules for each enemy type.
            - Initializes spawn rules for:
                - FastEnemy: spawns 10 per wave.
                - TankEnemy: spawns conditionally on milestone waves (e.g., every 5th and 10th wave).
            - Method advanceWave() increments the wave number and starts a new wave using generated spawn requests.
            - Method update() checks if it's time to spawn an enemy and adds it to the game if so.
            - Automatically ends the wave when all enemies have been spawned.
            - Method reset() clears the current wave and resets the wave number to 1.
            - Designed for extensibility — new enemy types and spawn logic can be added easily via WaveBlueprint.
        WorldManager.java
            - Provides utility methods for wrapping coordinates around the game world boundaries.
            - wrapX(double x): Ensures horizontal positions stay within the map width by applying modular arithmetic.
            - wrapY(double y): Ensures vertical positions stay within the map height using the same wrapping logic.
            - Prevents entities from drifting outside the playable area by looping them back around the edges.
            - Uses constants from Constants to define map dimensions.
            - Useful for implementing toroidal (looping) worlds or edge-wrapping mechanics.
    entities/
        Enemy.java
            - Base class for all enemy types in the game.
            - Stores world position (worldX, worldY) and handles movement, combat, and rendering.
            - Separates baseline stats (speed, damage, health, cooldown) from effective stats (after modifiers).
            - Supports burst-style attacks with configurable hit count, interval, and cooldown logic.
            - update(Player) moves the enemy toward the player using wrapped map coordinates and normalized vectors.
            - attemptAttack(Player) checks for collision and applies damage based on burst timing.
            - draw(Graphics, CameraManager) renders the enemy and its collider across map edges for seamless wrapping.
            - Uses WorldManager to wrap positions around map boundaries (toroidal world).
            - Provides utility methods for damage handling (takeDamage, isAlive, onDeath) and stat access.
            - Designed for subclassing — specific enemy types like FastEnemy or TankEnemy extend this class and override behavior or stats.
        FastEnemy.java
            - A lightweight, fast-moving enemy type that extends the base Enemy class.
            - Configured with:
                - Speed: 1.5 units per update (faster than average enemies).
                - Damage: 5.0 per hit.
                - Health: 20.0 (low durability).
                - Attack Cooldown: 800 ms between burst cycles.
                - Burst Logic: 1 hit per burst, with 150 ms between hits.
            - Visual size set to 30 pixels with a cyan color for easy identification.
            - Uses applyGlobalModifiers() to finalize effective stats without scaling.
            - Ideal for early waves or swarm-style enemy behavior.
        Landmark.java
            - Represents a static, non-interactive object placed in the game world.
            - Stores fixed world coordinates (worldX, worldY) and a visual size of 50 × 50 pixels.
            - Draws itself in red using fillRect() to stand out from dynamic entities.
            - Uses toroidal wrapping logic to render across map edges, ensuring visibility even near boundaries.
            - Relies on CameraManager for screen offset calculations and Constants for map dimensions.
            - Useful for environmental decoration, spawn markers, or visual reference points.
        Player.java
            - Represents the player-controlled character in the game world.
            - Stores position (x, y), size, movement flags, health, and speed.
            - Initializes with default stats and loads a sprite image from /assets/player/handgun/player.png.
            - Supports stat scaling via applyModifiers(speedMult, healthMult) for difficulty or upgrades.
            - Handles movement using WASD keys, normalized for consistent diagonal speed.
            - Uses WorldManager to wrap position around map edges for seamless navigation.
            - Implements damage logic with cooldown to prevent rapid hits (damageCooldown = 100 ms).
            - Flashes red on hit to provide visual feedback.
            - draw(Graphics, CameraManager) renders the player sprite or fallback rectangle at screen center.
            - Provides accessors for position, size, health, and alive status.
            - setDirection(String key, boolean pressed) updates movement flags based on input
        Projecttile.java
            - Represents a single projectile fired by the player or a weapon.
            - Stores position (x, y), direction (angle in radians), speed, range, and damage.
            - Moves forward each frame based on its angle and speed.
            - Tracks total distance traveled to determine when it should expire.
            - Checks for collisions with enemies:
            - Uses radius-based proximity detection with a small buffer.
            - Applies damage and disappears on impact.
            - update(List<Enemy>) handles movement and collision logic, returning false if the projectile should be removed.
            - draw(Graphics, cameraX, cameraY) renders the projectile as a yellow circle relative to the camera offset.
            - Designed to be lightweight and easily extensible for future projectile types or effects.
        TankEnemy.java
            - A slow but durable enemy type that extends the base Enemy class.
            - Configured with:
                - Speed: 0.8 units per update (sluggish movement).
                - Damage: 2.5 per hit (low but consistent).
                - Health: 100.0 (high durability).
                - Attack Cooldown: 1200 ms between burst cycles.
                - Burst Logic: 2 hits per burst.
            - Visual size set to 60 pixels with an orange color for easy identification.
            - Uses applyGlobalModifiers() to finalize effective stats without scaling.
            - Ideal for milestone waves or tank-style pressure against the player.
    input/
        KeyBindings.java
            - Centralizes keyboard input configuration for player movement and weapon switching.
            - setup(JPanel, Player, WeaponSwitcher) binds keys to in-game actions:
            - Movement: W, A, S, D keys toggle directional flags on the Player object.
            - Weapon Switching: 1, 2, 3 keys trigger the WeaponSwitcher interface to change weapons.
            - Uses InputMap and ActionMap to register key press and release events while the game panel is focused.
            - Encapsulates weapon switching logic via the WeaponSwitcher interface, allowing flexible integration with WeaponManager.
            - Promotes clean separation of input handling from game logic and rendering.
    utils/
        Constants.java
            - Centralizes key configuration values used throughout the game.
            - Defines screen dimensions:
                - SCREEN_WIDTH = 1200
                - SCREEN_HEIGHT = 800
            - Sets player defaults:
                - PLAYER_SIZE = 40
                - PLAYER_SPEED = 2
            - Specifies map dimensions for world wrapping and camera logic:
                - MAP_WIDTH = 2400
                - MAP_HEIGHT = 1600
            - Promotes consistency and easy tuning of game parameters from a single location.
    weapons/
        Handgun.java
            - Represents a basic semi-automatic weapon in the game.
            - Extends the abstract Weapon class and defines its own projectile behavior.
            - Configured with the following WeaponStats:
                - Damage: 10
                - Projectile Speed: 10
                - Fire Rate: 600 ms between shots
                - Range: 25 units
                - Pellets per Shot: 1
                - Magazine Size: 6
                - Reload Time: 3000 ms
            - spawnProjectile() creates a single Projectile with the configured stats and fires it in the given direction.
            - Ideal for early-game combat and precision shooting.
        ProjectileSpawner.java
            - Functional interface used by weapons to spawn projectiles.
            - Defines a single method: spawn(Projectile p) — called when a weapon fires.
            - Enables decoupling of weapon logic from game state management.
            - Passed into each weapon (e.g. Handgun, SMG, PumpShotgun) to delegate projectile creation to the game engine.
            - Promotes modularity and testability by abstracting projectile instantiation.
        PumpShotgun.java
            - Represents a close-range, high-burst weapon that extends the abstract Weapon class.
            - Configured with the following WeaponStats:
                - Damage: 8 per pellet
                - Projectile Speed: 30
                - Fire Rate: 400 ms between shots
                - Range: 15 units
                - Pellets per Shot: 6 (manually defined in spawnProjectile)
                - Magazine Size: 2
                - Reload Time: 2500 ms
                - Spread: ±15° randomized per pellet
            - spawnProjectile() fires 6 pellets with randomized spread angles around the aim direction.
            - Ideal for crowd control and close-quarters combat, trading precision for area coverage.
        SMG.java
            - Represents a rapid-fire submachine gun weapon that extends the abstract Weapon class.
            - Configured with the following WeaponStats:
                - Damage: 5 per shot
                - Projectile Speed: 5
                - Fire Rate: 500 ms between shots
                - Range: 30 units
                - Pellets per Shot: 10 (though only one is spawned per call — this may be used for burst logic)
                - Magazine Size: 30
                - Reload Time: 1500 ms
            - spawnProjectile() fires a single Projectile with the configured stats and direction.
            - Ideal for sustained fire and mid-range engagements, trading precision for volume
        Weapon.java
            - Serves as the foundation for all weapon types (Handgun, SMG, PumpShotgun, etc.).
            - Encapsulates core weapon behavior and state:
                - Ammo tracking
                - Fire rate control
                - Reloading logic
            - Holds a reference to:
                - ProjectileSpawner — used to delegate projectile creation.
                - WeaponStats — defines weapon-specific parameters like damage, speed, range, etc.
            - tryFire(x, y, angle):
                - Checks fire rate and ammo.
                - Spawns projectile if allowed.
                - Triggers reload if ammo is depleted.
            - update():
                - Handles reload completion based on elapsed time.
            - reload():
                - Initiates reload if not already in progress.
            - Abstract method spawnProjectile(x, y, angle):
                - Implemented by subclasses to define projectile behavior.
            - Provides accessors for ammo, reload status, and weapon stats.
        WeaponFactory.java
            - Provides a centralized factory method for creating weapon instances.
            - create(WeaponType type, ProjectileSpawner spawner) returns a specific Weapon subclass based on the given WeaponType:
                - HANDGUN → Handgun
                - SMG → SMG
                - SHOTGUN → PumpShotgun
            - Simplifies weapon instantiation and promotes clean separation between weapon selection and construction logic.
            - Ensures all weapons are initialized with a shared ProjectileSpawner for consistent projectile handling.
        WeaponManager.java
            - Manages all available weapons and handles switching between them.
            - Stores a map of WeaponType → Weapon instances and tracks the currently equipped weapon.
            - switchTo(WeaponType type): changes the active weapon.
            - getCurrent(): retrieves the currently equipped weapon.
            - update(): delegates update logic to the active weapon (e.g., reload timing).
            - tryShoot(x, y, angle): attempts to fire the current weapon in the given direction.
            - Cleanly separates weapon logic from player input and game state, enabling flexible weapon systems.
        WeaponStats.java
            - Encapsulates all configurable parameters for a weapon’s behavior.
            - Fields include:
                - damage: how much each projectile inflicts.
                - spread: angular deviation for multi-pellet weapons (e.g. shotguns).
                - range: maximum distance a projectile can travel.
                - projectileSpeed: how fast the projectile moves.
                - rateOfFire: shots per second (converted internally to milliseconds).
                - maxAmmo: magazine capacity before reload is needed.
                - reloadTime: time in milliseconds to fully reload.
            - Used by all weapon types to standardize firing logic and stat scaling.
            - Promotes modularity and easy tuning of weapon balance.
        WeaponType.java
            - Defines an enumeration of all supported weapon categories in the game.
            - Available types:
                - HANDGUN
                - SMG
                - SHOTGUN
            - Used by WeaponFactory, WeaponManager, and input systems to identify and switch between weapon classes.
            - Promotes type safety and clean integration across weapon-related logic.

