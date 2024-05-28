package com.dozingcatsoftware.linux;

public class KeyEvent {
  // LEFT KEYS
  public static final int KEYCODE_Z = 0;
  public static final int KEYCODE_DPAD_LEFT = 2;
  // RIGHT KEYS
  public static final int KEYCODE_SLASH = 3;
  public static final int KEYCODE_DPAD_RIGHT = 4;
  // BOTH KEYS
  public static final int KEYCODE_SPACE = 1;
  public static final int KEYCODE_ENTER = 5;
  public static final int KEYCODE_DPAD_CENTER = 6;

  public static final String ACTION_DOWN = "down";
  public static final String ACTION_UP = "up";

  public static KeyEvent DOWN_EVENT = new KeyEvent(ACTION_DOWN);
  public static KeyEvent UP_EVENT = new KeyEvent(ACTION_UP);

  private String action;

  private KeyEvent(String action) { this.action = action; }

  public String getAction() {
    return action;
  }

  public int getRepeatCount() {
    // TODO Auto-generated method stub
    return 0;
  }

}
