package com.dozingcatsoftware.linux;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://stackoverflow.com/questions/21211090/java-play-multiple-clips-simultaneously
 *
 */
public class MediaPlayer {
  private static final Logger logger = LoggerFactory.getLogger(MediaPlayer.class);

  public static AudioInputStream mono2stereo(AudioInputStream in) throws IOException {
    byte[] mono = in.readAllBytes();
    byte[] stereo = new byte[mono.length * 2];
    int i = 0;
    AudioFormat f = in.getFormat();
    while (i < mono.length) {
      for (int j = 0; j < f.getFrameSize(); j++) {
        stereo[2*i] = mono[i];
        ++i;
      }
    }
    AudioFormat out = new AudioFormat(f.getEncoding(),
        f.getSampleRate(),
        f.getSampleSizeInBits(),
        f.getChannels() * 2,
        f.getFrameSize() * 2,
        f.getFrameRate(),
        f.isBigEndian());
    return new AudioInputStream(
        new ByteArrayInputStream(stereo), out, stereo.length);
  }

  public static AudioInputStream loadSoundStream(String path)
      throws UnsupportedAudioFileException, IOException {
    InputStream in = ClassLoader.getSystemResourceAsStream(path);
    AudioInputStream audioStream;
    audioStream = AudioSystem.getAudioInputStream(in);
    AudioFormat f = audioStream.getFormat();
    AudioFormat pcmFormat =
        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16,
            f.getChannels(), 2 * f.getChannels(), 44100,
            false);
    AudioInputStream pcmStream =
        AudioSystem.getAudioInputStream(pcmFormat, audioStream);
    return pcmStream;
  }

  public  static Clip loadSound(String path) {
    try {
      AudioInputStream pcmStream = loadSoundStream(path);
      Clip c = AudioSystem.getClip();
      c.open(pcmStream);
      return c;
    } catch (UnsupportedAudioFileException | IOException
        | LineUnavailableException e) {
      logger.error("Can not load audio file: {}", e.getMessage());
    }
    return null;
  }

  private Clip clip;
  float rate = 44100;

  static MediaPlayer create(String resourceName) {
    MediaPlayer result = new MediaPlayer();
    result.clip = loadSound(resourceName);
    return result;
  }

  public void seekTo(int i) {
    clip.setFramePosition(i);
  }

  /** TODO try to fix the pitch thing */
  public void setVolume(float volume, float pitch) {
    FloatControl gainControl =
        (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
    gainControl.setValue(volume);
    /*
    FloatControl rateControl =
        (FloatControl)clip.getControl(FloatControl.Type.SAMPLE_RATE);
    rateControl.setValue(rate * pitch);
    */
  }

  public void start() {
    clip.start();
  }

  public boolean isPlaying() {
    return clip.isRunning();
  }

  public void pause() {
    clip.stop();
  }

}
