package com.dozingcatsoftware.linux;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SharedPreferences {

  private static final String SHARED_PREFERENCES_LOCATION =
      System.getProperty("user.home") + "/.local/share/VectorPinball/prefs.json";

  private Map<String, Object> _prefs = new HashMap<>();

  public class Editor {
    public void putString(String key, String string) {
      _prefs.put(key, string);
    }

    public void putLong(String key, long aLong) {
      _prefs.put(key, aLong);
    }

    public void putInt(String key, int anInt) {
      _prefs.put(key, anInt);
    }

    public void putBoolean(String key, boolean b) {
       _prefs.put(key, b);
    }

    public void commit() {
      JSONObject obj = new JSONObject(_prefs);
      File f = new File(SHARED_PREFERENCES_LOCATION);
      if (! f.getParentFile().exists()) {
        if (! f.getParentFile().mkdirs()) {
          BouncyActivity.logger.error("preferences directory {} can not be created",
              f);
          return;
        }
      }
      try (FileWriter fw = new FileWriter(f)) {
        obj.write(fw);
      } catch (JSONException | IOException e) {
        BouncyActivity.logger.error("preferences could not be saved: {}", e.getMessage());
      }
    }
  }

  public String getString(String key, String string) {
    return _prefs.containsKey(key) ? (String)_prefs.get(key) : string;
  }

  public int getInt(String key, int i) {
    //return _prefs.containsKey(key) ? Integer.parseInt((String)_prefs.get(key)) : i;
    return _prefs.containsKey(key) ? (Integer)_prefs.get(key) : i;
  }

  public long getLong(String key, long l) {
    return _prefs.containsKey(key) ? (Integer)_prefs.get(key) : l;
  }

  public boolean getBoolean(String key, boolean b) {
    return _prefs.containsKey(key) ? (Boolean)_prefs.get(key) : b;
  }

  public Editor edit() {
    return new Editor();
  }

  public static SharedPreferences loadSharedPreferences() {
    File f = new File(SHARED_PREFERENCES_LOCATION);
    SharedPreferences result = new SharedPreferences();
    try {
      if (f.exists()) {
        JSONTokener tokener = new JSONTokener(new FileReader(f));
        result._prefs = new JSONObject(tokener).toMap();
      }
    } catch (IOException ex) {
      BouncyActivity.logger.error("preferences could not be loaded: {}", ex.getMessage());
    }
    return result;
  }
}
