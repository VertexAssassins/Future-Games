package input;

import javax.swing.*;
import java.awt.event.ActionEvent;
import entities.Player;

public class KeyBindings {
    public static void setup(JPanel panel, Player player) {
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();

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
    }
}