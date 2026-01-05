package core;

import javax.swing.*;

import weapons.WeaponUnlockManager;

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

        // Reset Button
        JButton resetButton = new JButton("Reset Progress");
        resetButton.setBounds(500, 500, 200, 60);
        resetButton.setFont(new Font("Arial", Font.BOLD, 24));
        add(resetButton);

        // Button actions
        startButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.add(new GamePanel());
            frame.revalidate();
            frame.repaint();
        });

        resetButton.addActionListener(e -> {
            // Reset points
            PersistenceManager.save("points", 0);

            // Reset weapon unlocks
            WeaponUnlockManager.resetAll();

            // Optional: show confirmation
            JOptionPane.showMessageDialog(this, "Progress reset!");

            // Refresh the screen
            frame.revalidate();
            frame.repaint();
        });

        quitButton.addActionListener(e -> System.exit(0));
    }
}
