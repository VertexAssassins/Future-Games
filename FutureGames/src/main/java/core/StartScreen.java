package core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import weapons.WeaponUnlockManager;

public class StartScreen extends JPanel {

    // Helper to load + scale an image by multiplier
    private ImageIcon loadScaled(String path, int scale) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image img = icon.getImage().getScaledInstance(
                icon.getIconWidth() * scale,
                icon.getIconHeight() * scale,
                Image.SCALE_SMOOTH
        );
        return new ImageIcon(img);
    }

    // Helper to scale an image to a specific width/height
    private ImageIcon loadToSize(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public StartScreen(JFrame frame) {
        setLayout(null);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1200, 800));

        int screenW = 1200;
        int screenH = 800;

        // Scale factor for 64x64 buttons
        int scale = 2;

        // Load UI images
        ImageIcon borderImg = loadToSize("/ui/Border Template.png", screenW, screenH);
        ImageIcon playImg   = loadScaled("/ui/Play Button.png", scale);
        ImageIcon quitImg   = loadScaled("/ui/Quit Button.png", scale);

        // Full-screen border panel
        JLabel borderPanel = new JLabel(borderImg);
        borderPanel.setLayout(null);
        borderPanel.setBounds(0, 0, screenW, screenH);
        add(borderPanel);

        // Title
        // Load custom font
        Font customFont = null;
        try {
            customFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/fonts/GOOGLE-SPIES.TTF")
            ).deriveFont(96f); // set size here
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
            customFont = new Font("Arial", Font.BOLD, 96); // fallback
        }

        JLabel title = new JLabel("DEATH IS AN OPPORTUNITY");
        title.setForeground(Color.WHITE);
        title.setFont(customFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 120, screenW, 100);
        borderPanel.add(title);

        // Play button
        JButton playButton = new JButton(playImg);
        playButton.setBorderPainted(false);
        playButton.setContentAreaFilled(false);
        playButton.setFocusPainted(false);
        playButton.setBounds(
                (screenW - playImg.getIconWidth()) / 2,
                250,
                playImg.getIconWidth(),
                playImg.getIconHeight()
        );
        borderPanel.add(playButton);

        // Quit button
        JButton quitButton = new JButton(quitImg);
        quitButton.setBorderPainted(false);
        quitButton.setContentAreaFilled(false);
        quitButton.setFocusPainted(false);
        quitButton.setBounds(
                (screenW - quitImg.getIconWidth()) / 2,
                400,
                quitImg.getIconWidth(),
                quitImg.getIconHeight()
        );
        borderPanel.add(quitButton);

        // Reset Progress button
        JButton resetButton = new JButton("Reset Progress");
        resetButton.setFont(new Font("Arial", Font.BOLD, 24));
        resetButton.setBounds(
                (screenW - 250) / 2,
                550,
                250,
                60
        );
        borderPanel.add(resetButton);

        // Actions
        playButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.add(new GamePanel());
            frame.revalidate();
            frame.repaint();
        });

        quitButton.addActionListener(e -> System.exit(0));

        resetButton.addActionListener(e -> {
            PersistenceManager.save("points", 0);
            WeaponUnlockManager.resetAll();
            PersistenceManager.resetAllWeaponUpgrades();
            JOptionPane.showMessageDialog(this, "Progress reset!");
            frame.revalidate();
            frame.repaint();
        });
    }
}