package utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MusicManager {

    private final List<MusicTrack> tracks = new ArrayList<>();
    private final Deque<MusicTrack> lastPlayed = new ArrayDeque<>(5);

    private float musicVolume = 1.0f;
    private boolean paused = false;

    private MusicTrack current;

    public MusicManager() {
        loadTracks();
        startMusicThread();
    }

    private void loadTracks() {
        try {
            var codeSource = getClass().getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                System.err.println("No code source found");
                return;
            }

            var jarURL = codeSource.getLocation();
            try (var zip = new java.util.zip.ZipInputStream(jarURL.openStream())) {

                java.util.zip.ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName();

                    if (!entry.isDirectory() && isAudioFile(name) && name.contains("music/")) {
                        String clean = name.substring(name.indexOf("music/"));
                        System.out.println("Adding track: /" + clean);
                        tracks.add(new MusicTrack("/" + clean));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Loaded tracks: " + tracks.size());
    }

    private boolean isAudioFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".wav") || lower.endsWith(".mp3") || lower.endsWith(".ogg");
    }

    private void startMusicThread() {
        new Thread(() -> {
            while (true) {
                if (!paused) {
                    if (current == null || current.isFinished()) {
                        current = pickNextTrack();
                        current.play();
                    }
                }

                try {
                    Thread.sleep(200); // check 5 times per second
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private MusicTrack pickNextTrack() {
        List<MusicTrack> candidates = new ArrayList<>(tracks);

        // Remove last 5 played
        candidates.removeAll(lastPlayed);

        // If everything is excluded, reset history
        if (candidates.isEmpty()) {
            lastPlayed.clear();
            candidates.addAll(tracks);
        }

        // Pick random
        MusicTrack next = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));

        // Add to history
        lastPlayed.addLast(next);
        if (lastPlayed.size() > 5)
            lastPlayed.removeFirst();

        return next;
    }

    public void setVolume(float v) {
        musicVolume = v;

        if (current != null) {
            current.setVolume(v);
        }
    }

    public void pauseMusic() {
        paused = true;
        if (current != null) current.stop();
    }

    public void resumeMusic() {
        paused = false;
    }

    public void stop() {
        if (current != null) {
            current.stop();
        }
    }
}