package com.dozingcatsoftware.linux;

public class PreferenceManager {
  static SharedPreferences getDefaultSharedPreferences(Object o) {
    return SharedPreferences.loadSharedPreferences();
  }
}
