package com.dozingcatsoftware.linux;

public class PreferenceManager {
  private static SharedPreferences instance = null;
  static SharedPreferences getDefaultSharedPreferences(Object o) {
    if (instance == null)
      instance = SharedPreferences.loadSharedPreferences();
    return instance;
  }
}
