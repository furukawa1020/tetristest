package tetris;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {

    private static final float SAMPLE_RATE = 44100;

    public void playRotate() {
        playTone(600, 50, 0.3); // High beep
    }

    public void playMove() {
        playTone(200, 20, 0.1); // Short click
    }

    public void playDrop() {
        playTone(150, 100, 0.4); // Low thud
    }

    public void playClear() {
        new Thread(() -> {
            playTone(800, 100, 0.3);
            playTone(1000, 100, 0.3);
        }).start();
    }

    public void playGameOver() {
        new Thread(() -> {
            playTone(500, 300, 0.4);
            playTone(400, 300, 0.4);
            playTone(300, 600, 0.4);
        }).start();
    }

    private void playTone(double freq, int durationMs, double vol) {
        new Thread(() -> {
            try {
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                int steps = (int) (SAMPLE_RATE * durationMs / 1000);
                for (int i = 0; i < steps; i++) {
                    double angle = i / (SAMPLE_RATE / freq) * 2.0 * Math.PI;
                    // Square wave for 8-bit feel
                    buf[0] = (byte) ((Math.sin(angle) > 0 ? 1 : -1) * 127 * vol);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Simple background music loop
    public void startMusic() {
        new Thread(() -> {
            try {
                // Korobeiniki-ish theme (simplified)
                int[] notes = {
                        659, 493, 523, 587, 523, 493, 440, 440, 523, 659, 587, 523, 493, 523, 587, 659, 523, 440, 440,
                        0,
                        587, 698, 880, 783, 698, 587, 523, 523, 659, 880, 783, 698, 659, 523, 440, 440
                };
                int[] durations = {
                        400, 200, 200, 400, 200, 200, 400, 200, 200, 400, 200, 200, 400, 200, 400, 400, 400, 400, 400,
                        400,
                        400, 200, 400, 200, 200, 200, 200, 200, 400, 400, 200, 200, 400, 400, 400, 400
                };

                while (true) {
                    for (int i = 0; i < notes.length; i++) {
                        if (notes[i] == 0) {
                            Thread.sleep(durations[i]);
                        } else {
                            playToneSync(notes[i], durations[i], 0.15);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void playToneSync(double freq, int durationMs, double vol) {
        try {
            byte[] buf = new byte[1];
            AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();

            int steps = (int) (SAMPLE_RATE * durationMs / 1000);
            for (int i = 0; i < steps; i++) {
                double angle = i / (SAMPLE_RATE / freq) * 2.0 * Math.PI;
                // Square wave
                buf[0] = (byte) ((Math.sin(angle) > 0 ? 1 : -1) * 127 * vol);
                sdl.write(buf, 0, 1);
            }
            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
