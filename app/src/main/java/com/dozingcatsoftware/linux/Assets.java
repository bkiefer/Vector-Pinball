package com.dozingcatsoftware.linux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Assets {

  File rootDir;

  public Assets(File root) {
    rootDir = root;
  }

  public String[] list(String string) throws IOException {
    File dir = new File(rootDir, string);
    File[] files = dir.listFiles();
    String[] names = new String[files.length];
    for (int i = 0; i < names.length; ++i) {
      names[i] = files[i].getName();
    }
    return names;
  }

  public InputStream open(String assetPath) throws Exception {
    return new FileInputStream(new File(rootDir, assetPath));
  }

}
