package utils;
import javax.sound.sampled.*;
import java.net.URL;

public class Sound {

    private byte[] audioData;
    private AudioFormat format;

    public Sound(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Sound not found: " + path);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            format = ais.getFormat();
            audioData = ais.readAllBytes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        play(1.0f);
    }

    public void play(float pitch) {
        if (audioData == null) return;

        new Thread(() -> {
            try {
                float newSampleRate = format.getSampleRate() * pitch;

                AudioFormat pitchedFormat = new AudioFormat(
                    newSampleRate,
                    format.getSampleSizeInBits(),
                    format.getChannels(),
                    true,
                    false
                );

                SourceDataLine line = AudioSystem.getSourceDataLine(pitchedFormat);
                line.open(pitchedFormat);
                line.start();

                line.write(audioData, 0, audioData.length);
                line.drain();
                line.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public float getDurationSeconds() {
        int bytesPerFrame = format.getFrameSize();
        int totalFrames = audioData.length / bytesPerFrame;
        return totalFrames / format.getFrameRate();
    }
}