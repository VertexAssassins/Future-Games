package utils;

import javax.sound.sampled.*;

import java.net.URL;

public class MusicTrack {
    private Clip clip;
    private String name;

    public MusicTrack(String path) {
        this.name = path;

        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Music not found: " + path);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVolume(float v) {
        if (clip == null) return;

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float volume = Math.max(0.0001f, v);
            float dB = (float)(Math.log10(volume) * 20);

            gain.setValue(dB);
        }
    }

    public void play() {
        if (clip == null) return;
        clip.setFramePosition(0);
        clip.start();
    }

    public void stop() {
        if (clip != null) clip.stop();
    }

    public boolean isFinished() {
        return !clip.isRunning();
    }

    public String getName() {
        return name;
    }
}