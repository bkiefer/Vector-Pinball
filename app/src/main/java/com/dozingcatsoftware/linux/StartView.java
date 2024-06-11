package com.dozingcatsoftware.linux;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dozingcatsoftware.linux.SharedPreferences.Editor;

/**
 * This class displays the score and game messages above the game view. When there is no game in
 * progress, it cycles between a "Touch to Start" message, last score, and high scores.
 */
public class StartView extends JDialog {

    private static final long serialVersionUID = -4589110350465936240L;

    JLabel helpField;
    JCheckBox useZoom;
    JCheckBox showBallTrails;
    JCheckBox independentFlippers;
    JSpinner lineWidth;
    JCheckBox doShowFPS;

    Icon balls[];

    static NumberFormat SCORE_FORMAT = NumberFormat.getInstance();

    private JLabel newBWLabel() {
      JLabel l = new JLabel();
      l.setBackground(Color.black);
      l.setForeground(Color.white);
      return l;
    }

    private static String camelSplit(String s) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < s.length(); ++i) {
        char c = s.charAt(i);
        if (Character.isUpperCase(c)) {
          sb.append(' ').append(Character.toLowerCase(c));
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }


    JCheckBox makeCheckBox(SharedPreferences prefs, String key, String text,
        boolean value) {
      JCheckBox cb = new JCheckBox(text, prefs.getBoolean(key, value));
      cb.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent ev) {
          Editor e = prefs.edit();
          e.putBoolean(key, cb.isSelected());
          e.commit();
        }
      });
      return cb;
    }

    public StartView(BouncyActivity frame) {
      super(frame, true);
      setLocationRelativeTo(frame);
      SharedPreferences prefs =
          PreferenceManager.getDefaultSharedPreferences(null);
      JPanel c = new JPanel();
      c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));
      setContentPane(c);
      JLabel kt = new JLabel("Key Shortcuts");
      c.add(kt);
      c.add(Box.createRigidArea(new Dimension(0,10)));
      helpField = new JLabel("<html>"
          + "Z -- Left Flipper<br>"
          + "/ -- Right Flipper<br>"
          + "S -- Start Game<br>"
          + "E -- End Game<br>"
          + "P -- Pause Game<br>"
          + "R -- Resume Game</html>"
          );
      //helpField.setAlignmentX(CENTER_ALIGNMENT);
      c.add(helpField);
      c.add(Box.createRigidArea(new Dimension(0,10)));
      //helpField.setAlignmentX(Component.CENTER_ALIGNMENT);
      useZoom = makeCheckBox(prefs, "zoom", "Zoom", true);
      c.add(useZoom);

      showBallTrails= makeCheckBox(prefs, "showBallTrails", "Show Ball Trails", true);
      c.add(showBallTrails);

      independentFlippers = makeCheckBox(prefs, "independentFlippers", "Independent Flippers", true);
      c.add(independentFlippers);

      JPanel lw = new JPanel();
      lw.setMinimumSize(new Dimension(30,0));
      //lw.setBorder(new LineBorder(Color.black));
      lw.setLayout(new FlowLayout(FlowLayout.CENTER));
      lw.add(Box.createRigidArea(new Dimension(5,0)));

      lineWidth = new JSpinner(
          new SpinnerNumberModel(prefs.getInt("lineWidth", 0), 0, 8, 1));
      Dimension d = lineWidth.getPreferredSize();
      d.width = 50;
      lineWidth.setMaximumSize(d);
      lineWidth.setValue(prefs.getInt("lineWidth", 0));
      //lineWidth.setAlignmentX(Component.CENTER_ALIGNMENT);
      lineWidth.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent ev) {
          int val = (Integer)lineWidth.getModel().getValue();
          Editor e = prefs.edit();
          e.putInt("lineWidth", val);
          e.commit();
        }
      });
      lw.add(lineWidth);
      JLabel l = new JLabel("Line Width");
      l.setLabelFor(lineWidth);
      lw.add(l);
      c.add(lw);

      doShowFPS = makeCheckBox(prefs, "showFPS", "show FPS", false);
      c.add(doShowFPS);
      c.add(Box.createRigidArea(new Dimension(0,10)));

      JCheckBox unlimitedBallsCheckBox = new JCheckBox("Unlimited Balls");
      //unlimitedBallsCheckBox.setForeground(Color.black);
      //unlimitedBallsCheckBox.setBackground(Color.white);
      boolean unlimitedBalls = prefs.getBoolean("unlimitedBallsToggle", false);
      unlimitedBallsCheckBox.setSelected(unlimitedBalls);
      unlimitedBallsCheckBox.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent ev) {
          boolean unlimitedBalls = (ev.getStateChange() == ItemEvent.SELECTED);
          Editor e = prefs.edit();
          e.putBoolean("unlimitedBallsToggle", unlimitedBalls);
          e.commit();
        }
      });
      c.add(unlimitedBallsCheckBox);

      c.add(Box.createRigidArea(new Dimension(0,10)));
      JPanel okb = new JPanel();
      okb.setLayout(new FlowLayout(FlowLayout.CENTER));

      JButton okbutton = new JButton(new AbstractAction() {

        private static final long serialVersionUID = 1L;
        @Override
        public Object getValue(String key) {
          if (key == Action.NAME) return "OK";
          return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
          frame.updateFromPreferences();
          frame.doStartGame();
          StartView.this.dispose();
        }});
      okb.add(okbutton);
      this.getRootPane().setDefaultButton(okbutton);

      okb.add(Box.createRigidArea(new Dimension(40,10)));

      JButton helpbutton = new JButton(new AbstractAction() {

        private static final long serialVersionUID = 1L;
        @Override
        public Object getValue(String key) {
          if (key == Action.NAME) return "Help";
          return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
          new AboutDialog(frame);
        }});
      okb.add(helpbutton);

      c.add(okb);

      Dimension frameDim = frame.getSize();
      this.setPreferredSize(new Dimension((int)(frameDim.width * 0.66),
          (int)(frameDim.height * 0.66)));
      this.pack();
      this.setVisible(true);
    }


}
