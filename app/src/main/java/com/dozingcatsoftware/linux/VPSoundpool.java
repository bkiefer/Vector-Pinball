package com.dozingcatsoftware.linux;

import static com.dozingcatsoftware.linux.MediaPlayer.loadSound;

import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.Clip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dozingcatsoftware.vectorpinball.model.AudioPlayer;

public class VPSoundpool {
  private static final Logger logger = LoggerFactory.getLogger(VPSoundpool.class);

  private static HashMap<Integer, Clip> mSoundPoolMap = new HashMap<>();
  private static Random mRandom = new Random();

  private static boolean soundEnabled = true;
  private static boolean musicEnabled = true;
  private static int cScore = 0;
  private static int prevDing, currentDing = 0;
  private static int andrModAmt = 10;
  private static MediaPlayer drumbass;
  private static MediaPlayer androidpad;
  //private final static float SEMITONE = 1.059463094f;

  private static volatile boolean soundsLoaded = false;

  static int ID_DING_START    = 0;
  static int NUM_DINGS        = 6;

  static int ID_LAUNCH        = 100;
  static int ID_FLIPPER       = 101;
  static int ID_MESSAGE       = 102;
  static int ID_START         = 103;

  static int ID_ROLLOVER      = 200;

  public static void setSoundEnabled(boolean enabled) {
      soundEnabled = enabled;
  }

  public static void setMusicEnabled(boolean enabled) {
      musicEnabled = enabled;
      if (!musicEnabled) {
          resetMusicState();
      }
  }

  public static void resetMusicState() {
      if (!soundsLoaded) return;

      pauseMusic();
      cScore = 0;
      andrModAmt = 10;
      if (drumbass!=null) drumbass.seekTo(0);
      if (androidpad!=null) androidpad.seekTo(0);
  }

  public static void playScore() {
    if (!soundsLoaded) return;

    //prevent repeated dings
    while (currentDing == prevDing) {
        currentDing = mRandom.nextInt(NUM_DINGS);
    }
    playSound(ID_DING_START + currentDing, 0.5f, 1);
    prevDing = currentDing;

    //start playing the drumbassloop after 20 scoring hits
    cScore++;
    if (musicEnabled && cScore%20 == 0 && drumbass!=null && !drumbass.isPlaying()){
        drumbass.setVolume(1.0f, 1.0f);
        drumbass.start();
    }
    //play the androidpad after 10 scoring hits the first time
    //then increase the mod amount each time the pad is played,
    //so that it will be heard less frequently over time
    if (musicEnabled && androidpad!=null && cScore%andrModAmt == 0){
        androidpad.setVolume(0.5f, 0.5f);
        androidpad.start();
        andrModAmt += 42;
    }
  }

//Play up to three events, each randomly pitched to a different note in the pentatonic scale.
//The rollover ding is E, so in semitones, the other pitches are
//-4 (C), -2 (D), +3 (G), +5 (A), +8 (C). For Soundpool, that translates to:
// 0.7937008 (C), 0.8908991 (D), 1.1892079 (G), 1.3348408 (A), 1.5874025 (C).
  private static final float[] PITCHES = {
    0.7937008f, 0.8908991f, 1f, 1.1892079f, 1.3348408f, 1.5874025f
  };
  private static final float ROLLOVER_DURATION = 0.3f;

  static void playSound(int soundKey, float volume, float pitch) {
      if (soundsLoaded && soundEnabled && mSoundPoolMap != null) {
          Clip soundID = mSoundPoolMap.get(soundKey);
          if (soundID != null) {
              //mSoundPool.play(soundID, volume, volume, 1, 0, pitch);
              soundID.setFramePosition(0);
              soundID.start();
          }
      }
  }

  public static void playRollover() {
    if (!soundsLoaded) return;

    int p1 = mRandom.nextInt(6);
    int p2 = mRandom.nextInt(6);
    int p3 = mRandom.nextInt(6);

    // Only play distinct notes.
    playSound(ID_ROLLOVER, ROLLOVER_DURATION, PITCHES[p1]);
    if (p2 != p1) {
        playSound(ID_ROLLOVER, ROLLOVER_DURATION, PITCHES[p2]);
    }
    if (p3 != p1 && p3 != p2) {
        playSound(ID_ROLLOVER, ROLLOVER_DURATION, PITCHES[p3]);
    }
  }

  public static void playBall() {
    playSound(ID_LAUNCH, 1, 1);
  }

  public static void playFlipper() {
    playSound(ID_FLIPPER, 1, 1);
  }

  public static void playStart() {
    resetMusicState();
    playSound(ID_START, 0.5f, 1);
  }

  public static void playMessage() {
    playSound(ID_MESSAGE, 0.66f, 1);
  }

  public static void pauseMusic() {
    if (!soundsLoaded) return;

    if (drumbass!=null && drumbass.isPlaying()) {
        drumbass.pause();
    }
    if (androidpad!=null && androidpad.isPlaying()) {
        androidpad.pause();
    }
  }

  public static class Player implements AudioPlayer {
    @Override public void playStart() {
        VPSoundpool.playStart();
    }

    @Override public void playBall() {
        VPSoundpool.playBall();
    }

    @Override public void playFlipper() {
        VPSoundpool.playFlipper();
    }

    @Override public void playScore() {
        VPSoundpool.playScore();
    }

    @Override public void playMessage() {
        VPSoundpool.playMessage();
    }

    @Override public void playRollover() {
        VPSoundpool.playRollover();
    }
  }

  // On some devices running Lollipop (seen on Nexus 5 and emulator), SoundPool slows down
  // significantly after loading ~6 sounds, taking several seconds for each additional call to
  // load(). To avoid blocking the main thread (and delaying the app launch), this method is
  // called in an async thread from the main activity.
  // See https://code.google.com/p/android-developer-preview/issues/detail?id=1812
  public static void loadSounds() {
      logger.info("loadSounds start");
      soundsLoaded = false;
      mSoundPoolMap.clear();

      mSoundPoolMap.put(ID_DING_START+0, loadSound("audio/bumper/dinga1.ogg"));
      mSoundPoolMap.put(ID_DING_START+1, loadSound("audio/bumper/dingc1.ogg"));
      mSoundPoolMap.put(ID_DING_START+2, loadSound("audio/bumper/dingc2.ogg"));
      mSoundPoolMap.put(ID_DING_START+3, loadSound("audio/bumper/dingd1.ogg"));
      mSoundPoolMap.put(ID_DING_START+4, loadSound("audio/bumper/dinge1.ogg"));
      mSoundPoolMap.put(ID_DING_START+5, loadSound("audio/bumper/dingg1.ogg"));

      mSoundPoolMap.put(ID_LAUNCH, loadSound("audio/misc/andBounce2.ogg"));
      mSoundPoolMap.put(ID_FLIPPER, loadSound("audio/misc/flipper1.ogg"));
      mSoundPoolMap.put(ID_MESSAGE, loadSound("audio/misc/message2.ogg"));
      mSoundPoolMap.put(ID_START, loadSound("audio/misc/startup1.ogg"));
      mSoundPoolMap.put(ID_ROLLOVER, loadSound("audio/misc/rolloverE.ogg"));

      drumbass = MediaPlayer.create("raw/drumbassloop.ogg");
      androidpad = MediaPlayer.create("raw/androidpad.ogg");

      soundsLoaded = true;
      resetMusicState();
      logger.info("loadSounds finished");

  }

}
