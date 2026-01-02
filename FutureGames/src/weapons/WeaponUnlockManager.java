package weapons;

import java.util.prefs.Preferences;

public class WeaponUnlockManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(WeaponUnlockManager.class);

    public static boolean isUnlocked(String weaponId) {
        return prefs.getBoolean("weaponUnlocked_" + weaponId, false);
    }

    public static void unlock(String weaponId) {
        prefs.putBoolean("weaponUnlocked_" + weaponId, true);
    }

    public static void resetAll() {
        try {
            for (String key : prefs.keys()) {
                if (key.startsWith("weaponUnlocked_")) {
                    prefs.putBoolean(key, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}