package com.dozingcatsoftware.linux;

public interface Context {

  public Assets getAssets();

  public String getString(String string, Object ... args);

}
