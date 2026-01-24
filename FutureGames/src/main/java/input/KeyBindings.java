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
                player.startDash();
            }
        });

        // Weapon switching keys — **moved inside the setup() method**
        String[] weaponKeys = {"1", "2", "3", "4", "5", "6", "7"};
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

        // Q = previous
        im.put(KeyStroke.getKeyStroke("pressed Q"), "weapon_prev");
        am.put("weapon_prev", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                weaponSwitcher.switchWeapon(-1); // previous
            }
        });

        // E = next
        im.put(KeyStroke.getKeyStroke("pressed E"), "weapon_next");
        am.put("weapon_next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                weaponSwitcher.switchWeapon(-2); // next (special code)
            }
        });
    }

    /** Interface to switch weapons */
    public interface WeaponSwitcher {
        void switchWeapon(int index);
    }
}
