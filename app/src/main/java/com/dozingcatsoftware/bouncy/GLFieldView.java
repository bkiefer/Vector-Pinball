package com.dozingcatsoftware.bouncy;

/*
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
*/
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.AttributeSet;

import com.dozingcatsoftware.linux.BouncyActivity;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

public class GLFieldView extends GLCanvas implements KeyListener
//extends GLSurfaceView
{

    private static final long serialVersionUID = 6474829816957064530L;

    // This view class just handles input events. OpenGL calls to draw field elements are done in
    // GL10Renderer and GL20Renderer.
    public GLFieldView(GLCapabilities cap, AttributeSet attrs) {
        //super(context, attrs);
      super(cap);
      setFocusable(true);
      addKeyListener(this);
    }

    FieldViewManager manager;

    public void setManager(FieldViewManager value) {
        this.manager = value;
    }

    /**
     * Called when the view is touched. Activates flippers, starts a new game if one is not in
     * progress, and launches a ball if one is not in play.
     *
    @Override public boolean onTouchEvent(MotionEvent event) {
        return manager.handleTouchEvent(event);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        return manager.handleKeyDown(keyCode, event);
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        return manager.handleKeyUp(keyCode, event);
    }
    */

    @Override public void keyTyped(KeyEvent event) {

    }

    private static Map<Integer, Integer> KEYMAP = new HashMap<>();
    static {
      KEYMAP.put(KeyEvent.VK_Z, com.dozingcatsoftware.linux.KeyEvent.KEYCODE_Z);
      KEYMAP.put(KeyEvent.VK_SLASH, com.dozingcatsoftware.linux.KeyEvent.KEYCODE_SLASH);
    }

    @Override
    public void keyPressed(KeyEvent e) {
      Integer code = KEYMAP.get(e.getKeyCode());
      if (code == null) return;
      boolean done = manager.handleKeyDown(code, com.dozingcatsoftware.linux.KeyEvent.DOWN_EVENT);
      BouncyActivity.logger.info("Flipper up: {}", done ? "yes" : "no");
    }

    @Override
    public void keyReleased(KeyEvent e) {
      Integer code = KEYMAP.get(e.getKeyCode());
      if (code == null) return;
      boolean done = manager.handleKeyUp(code, com.dozingcatsoftware.linux.KeyEvent.UP_EVENT);
      BouncyActivity.logger.info("Flipper down: {}", done ? "yes" : "no");
    }

    public void onPause() {
      // stop rendering GL if necessary.
    }

    public void onResume() {
      // restart rendering GL if necessary
    }

}
