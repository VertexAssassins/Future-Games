package core;

import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Future Games");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}