package core;

import javax.swing.*;

// Main game window frame
public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Future Games");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Add start screen panel to access the game
        StartScreen startScreen = new StartScreen(this);
        add(startScreen);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}