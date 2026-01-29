package core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.common.io.NIOUtils;

public class JCodecVideoPanel extends JPanel implements Runnable {

    private final String videoPath;
    private final Runnable onFinished;
    private final Runnable onReady;

    private volatile boolean playing = true;
    private volatile boolean loading = true;

    private List<BufferedImage> frames = new ArrayList<>();
    private BufferedImage currentFrame;

    public JCodecVideoPanel(String videoPath, Runnable onReady, Runnable onFinished) {
        this.videoPath = videoPath;
        this.onReady = onReady;
        this.onFinished = onFinished;
        setBackground(Color.BLACK);
    }

    @Override
    public void run() {
        try {
            // Wait until panel has a real size
            while (getWidth() == 0 || getHeight() == 0) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            // Load MP4 from resources
            InputStream is = getClass().getResourceAsStream(videoPath);
            if (is == null) {
                System.err.println("VIDEO NOT FOUND: " + videoPath);
                if (onFinished != null) onFinished.run();
                return;
            }

            // Write to temp file
            File tempFile = File.createTempFile("cutscene", ".mp4");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                is.transferTo(fos);
            }

            // Decode all frames first
            AWTFrameGrab grab = AWTFrameGrab.createAWTFrameGrab(
                    NIOUtils.readableChannel(tempFile)
            );

            BufferedImage frame;
            int targetW = getWidth();
            int targetH = getHeight();

            while ((frame = grab.getFrame()) != null) {
                BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = scaled.createGraphics();
                g2.drawImage(frame, 0, 0, targetW, targetH, null);
                g2.dispose();

                frames.add(scaled);
            }

            loading = false;

            if (onReady != null) onReady.run();

            for (BufferedImage f : frames) {
                if (!playing) break;
                currentFrame = f;
                repaint();
                Thread.sleep(33);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (onFinished != null) onFinished.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (loading) {
            g.setColor(Color.WHITE);
            g.drawString("Loading cutscene...", 20, 20);
            return;
        }

        if (currentFrame != null) {
            g.drawImage(currentFrame, 0, 0, null);
        }
    }

    public void stopVideo() {
        playing = false;
    }
}