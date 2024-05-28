package com.dozingcatsoftware.linux;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.dozingcatsoftware.bouncy.GLFieldView;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

public class BasicJOGLApp implements GLEventListener {

  @Override
  public void dispose(GLAutoDrawable drawable) {
    // Cleanup code here
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width,
      int height) {
    // Window resize code here
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Basic JOGL App");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(800, 600);

      GLProfile profile = GLProfile.get(GLProfile.GL2);
      GLCapabilities capabilities = new GLCapabilities(profile);
      GLCanvas canvas = new GLFieldView(capabilities, null);

      BasicJOGLApp app = new BasicJOGLApp();
      canvas.addGLEventListener(app);

      frame.add(canvas);
      frame.setVisible(true);
    });
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();

    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();

    // Clear the screen and depth buffer
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    // Set up the projection matrix
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
    gl.glLoadIdentity();
    GLU glu = new GLU();
    glu.gluPerspective(45.0, 800.0 / 600.0, 1.0, 100.0);

    // Set up the modelview matrix
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
    gl.glLoadIdentity();
    glu.gluLookAt(5, 5, 5, 0, 0, 0, 0, 1, 0);

    // Draw a cube
    gl.glBegin(GL2.GL_QUADS);

    // Front face
    gl.glColor3f(1, 0, 0);
    gl.glVertex3f(-1, -1, 1);
    gl.glVertex3f(1, -1, 1);
    gl.glVertex3f(1, 1, 1);
    gl.glVertex3f(-1, 1, 1);

    // ...
    gl.glColor3f(0, 1, 0);
    gl.glVertex3f(1, -1, 1);
    gl.glVertex3f(1, -1, -1);
    gl.glVertex3f(1, 1, -1);
    gl.glVertex3f(1, 1, 1);

    // Back face
    gl.glColor3f(0, 0, 1);
    gl.glVertex3f(-1, -1, -1);
    gl.glVertex3f(1, -1, -1);
    gl.glVertex3f(1, 1, -1);
    gl.glVertex3f(-1, 1, -1);

    gl.glEnd();
  }

}
