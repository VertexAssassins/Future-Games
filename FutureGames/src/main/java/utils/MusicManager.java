package utils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MusicManager {

    private final List<MusicTrack> tracks = new ArrayList<>();
    private final Deque<MusicTrack> lastPlayed = new ArrayDeque<>(5);

    private float musicVolume = 1.0f;

    private MusicTrack current;

    public MusicManager() {
        loadTracks();
        startMusicThread();
    }

    private void loadTracks() {
        try {
            // Get the directory inside the JAR/resources
            var dirURL = getClass().getResource("/music/");
            if (dirURL == null) {
                System.err.println("Music folder not found");
                return;
            }

            // If running from IDE (not JAR), dirURL is a file: URL
            if (dirURL.getProtocol().equals("file")) {
                var folder = new java.io.File(dirURL.toURI());
                for (var file : Objects.requireNonNull(folder.listFiles())) {
                    if (isAudioFile(file.getName())) {
                        tracks.add(new MusicTrack("/music/" + file.getName()));
                    }
                }
                return;
            }

            // If running from JAR, we need to scan entries inside the JAR
            if (dirURL.getProtocol().equals("jar")) {
                String path = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
                try (var jar = new java.util.jar.JarFile(path)) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        String name = entry.getName();

                        if (name.startsWith("music/") && isAudioFile(name)) {
                            tracks.add(new MusicTrack("/" + name));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAudioFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".wav") || lower.endsWith(".mp3") || lower.endsWith(".ogg");
    }

    private void startMusicThread() {
        new Thread(() -> {
            while (true) {
                if (current == null || current.isFinished()) {
                    current = pickNextTrack();
                    current.play();
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
}