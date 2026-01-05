package core;

import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Future Games");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        StartScreen startScreen = new StartScreen(this);
        add(startScreen);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}