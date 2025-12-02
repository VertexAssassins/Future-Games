package core;

import java.util.prefs.Preferences;

public class PersistenceManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(PersistenceManager.class);

    public static void save(String key, int value) {
        prefs.putInt(key, value);
    }

    public static int load(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public static void save(String key, boolean value) {
        prefs.putBoolean(key, value);
    }

    public static boolean load(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
}