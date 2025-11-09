package core;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JPanel {

    public StartScreen(JFrame frame) {
        setLayout(null); // We'll position elements manually
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1200, 800));

        // Game title
        JLabel title = new JLabel("DEATH IS AN OPPERTUNITY");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 60));
        title.setBounds(0, 100, 1200, 100); // center horizontally
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title);

        // Start button
        JButton startButton = new JButton("Start");
        startButton.setBounds(500, 300, 200, 60);
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        add(startButton);

        // Quit button
        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(500, 400, 200, 60);
        quitButton.setFont(new Font("Arial", Font.BOLD, 24));
        add(quitButton);

        // Button actions
        startButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.add(new GamePanel());
            frame.revalidate();
            frame.repaint();
        });

        quitButton.addActionListener(e -> System.exit(0));
    }
}
