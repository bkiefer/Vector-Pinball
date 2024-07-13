package com.dozingcatsoftware.linux;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import org.junit.Ignore;
import org.junit.Test;

public class TestSoundPlay {

  static boolean testAllSounds(Predicate<Path> cons) throws IOException {
    boolean[] error = {false};
    try (Stream<Path> s = Files.find(Path.of("./src/main/assets/audio/"), 99999,
        new BiPredicate<Path, BasicFileAttributes>() {

          @Override
          public boolean test(Path t, BasicFileAttributes u) {
            Path f = t.getFileName();
            String n  = f.toString();
            return n.endsWith("ogg");
          }
        })) {
      s.forEach(p -> { error[0] |= cons.test(p); });
    }
    return error[0];
  }

  Clip loadAndPlaySound(Path p) {
    String name = p.subpath(4, p.getNameCount()).toString();
    Clip c = MediaPlayer.loadSound(name);
    c.setFramePosition(0);
    // values have min/max values, for now don't check for outOfBounds values
    FloatControl gainControl = (FloatControl)c.getControl(FloatControl.Type.MASTER_GAIN);
    gainControl.setValue(0.7f);
    c.start(); // c.drain();
    return c;
  }

  @Ignore
  @Test
  public void testTinyConvClipPlay() throws IOException {
    List<Clip> clips = new ArrayList<>();
    assertFalse(testAllSounds(p -> {
        try {
          //System.err.println("Try "+ p.toString());
          clips.add(loadAndPlaySound(p));
        } catch (Exception e) {
          System.err.println("Can not read "+ p.toString() +  " " + e);
          return true;
        }
        return false;}));
    while (!clips.isEmpty()) {
      for (int i = 0; i < clips.size(); ++i) {
        Clip c = clips.get(i);
        if (! c.isRunning()) {
          c.close();
          clips.remove(i);
        }
      }
    }
  }
}
