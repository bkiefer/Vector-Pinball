package com.dozingcatsoftware.linux;

import java.util.function.Function;

import javax.swing.JFrame;

import com.dozingcatsoftware.bouncy.FieldViewManager;
import com.dozingcatsoftware.bouncy.GL20Renderer;
import com.dozingcatsoftware.bouncy.GLFieldView;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;



public class VectorPinballMain {
  static String SHADER_ROOT="src/main/assets/shaders/";

  public static void main(String[] args) {
      GLProfile glp = GLProfile.get(GLProfile.GLES2);//
      GLCapabilities cap = new GLCapabilities(glp);//
      FieldViewManager fwm = new FieldViewManager();
      Function<String, String> fn = (String shaderPath) -> SHADER_ROOT + shaderPath;
      GLFieldView canvas = new GLFieldView(cap, null);
      // canvas.setManager(fwm); // render.setManager does that for me
      GL20Renderer render = new GL20Renderer(canvas, fn);
      render.setManager(fwm);
      //canvas.addGLEventListener(render); // done in constructor of GLFieldView
      canvas.setSize(500, 500);
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          JFrame frame = new JFrame("Simple Swing JFrame using JOGL");
          frame.getContentPane().add(canvas);
          frame.setSize(480,480);
          frame.setVisible(true);
        }
      });

      /*Frame frame = new Frame (" Simple Frame using GLCanvas ");
      frame.add(canvas);
      frame.setSize( 580, 580 );
      frame.setVisible(true);*/
   }
}
