package input;

import javax.swing.*;

import java.awt.event.ActionEvent;
import entities.Player;

public class KeyBindings {
    public static void setup(JPanel panel, Player player, WeaponSwitcher weaponSwitcher) {
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();

        // Movement keys
        String[] keys = {"W", "A", "S", "D"};
        for (String key : keys) {
            im.put(KeyStroke.getKeyStroke("pressed " + key), "press_" + key);
            im.put(KeyStroke.getKeyStroke("released " + key), "release_" + key);

            am.put("press_" + key, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    player.setDirection(key, true);
                }
            });
            am.put("release_" + key, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    player.setDirection(key, false);
                }
            });
        }

        // Dash key (Space)
        im.put(KeyStroke.getKeyStroke("pressed SPACE"), "dash");
        am.put("dash", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                player.startDash(); // You’ll define this in Player
            }
        });

        // Weapon switching keys — **moved inside the setup() method**
        String[] weaponKeys = {"1", "2", "3"};
        for (int i = 0; i < weaponKeys.length; i++) {
            String key = weaponKeys[i];
            int weaponIndex = i; // capture index for lambda

            im.put(KeyStroke.getKeyStroke("pressed " + key), "weapon_" + key);
            am.put("weapon_" + key, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    weaponSwitcher.switchWeapon(weaponIndex);
                }
            });
        }
    }

    /** Interface to switch weapons */
    public interface WeaponSwitcher {
        void switchWeapon(int index);
    }
}
