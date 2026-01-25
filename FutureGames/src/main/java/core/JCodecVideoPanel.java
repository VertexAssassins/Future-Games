package core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JPanel;

import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.common.io.NIOUtils;

public class JCodecVideoPanel extends JPanel implements Runnable {

    private final String videoPath;
    private final Runnable onFinished;
    private volatile boolean playing = true;

    private BufferedImage currentFrame;

    public JCodecVideoPanel(String videoPath, Runnable onFinished) {
        this.videoPath = videoPath;
        this.onFinished = onFinished;
        setBackground(Color.BLACK);
    }

    @Override
    public void run() {
        File tempFile = null;

        try {
            // Load MP4 from resources
            InputStream is = getClass().getResourceAsStream(videoPath);
            if (is == null) {
                System.err.println("VIDEO NOT FOUND: " + videoPath);
                if (onFinished != null) onFinished.run();
                return;
            }

            // Write to a temporary file
            tempFile = File.createTempFile("cutscene", ".mp4");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                is.transferTo(fos);
            }

            // JCodec can now read it
            AWTFrameGrab grab = AWTFrameGrab.createAWTFrameGrab(
                NIOUtils.readableChannel(tempFile)
            );

            while (playing) {
                BufferedImage frame = grab.getFrame();
                if (frame == null) break;

                currentFrame = frame;
                repaint();

                Thread.sleep(33); // ~30 FPS
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (onFinished != null) onFinished.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentFrame != null) {
            g.drawImage(currentFrame, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public void stopVideo() {
        playing = false;
    }
}