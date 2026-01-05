package core;

import utils.Constants;

public class WorldManager {
    public static double wrapX(double x) {
        return (x + Constants.MAP_WIDTH) % Constants.MAP_WIDTH;
    }

    public static double wrapY(double y) {
        return (y + Constants.MAP_HEIGHT) % Constants.MAP_HEIGHT;
    }
}