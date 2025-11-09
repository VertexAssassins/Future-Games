package core;

import entities.Player;
import utils.Constants;

public class CameraManager {
    private final Player player;

    public CameraManager(Player player) {
        this.player = player;
    }

    public int getOffsetX() {
        return (int) player.getX() - Constants.SCREEN_WIDTH / 2;
    }

    public int getOffsetY() {
        return (int) (player.getY() - Constants.SCREEN_HEIGHT / 2);
    }
}