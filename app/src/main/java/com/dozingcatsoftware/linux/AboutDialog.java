package com.dozingcatsoftware.linux;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class displays the score and game messages above the game view. When there is no game in
 * progress, it cycles between a "Touch to Start" message, last score, and high scores.
 */
public class AboutDialog extends JDialog {

    private static final long serialVersionUID = -4589110350465936240L;

    BouncyActivity frame;

    JLabel helpField;


    String toHtmlList(String input) {
      String htmlList = input.replaceAll("\\\\n-([^\\\\]*)", "<li>$1</li>\n");
      int firstLi = htmlList.indexOf("<li>");
      int lastLi = htmlList.lastIndexOf("</li>");
      htmlList = htmlList.substring(0, firstLi)
          + "<ul>" + htmlList.substring(firstLi, lastLi + 5) + "</ul>"
          + htmlList.substring(lastLi + 5);
      return htmlList;
    }

    String toHtmlLineBreaks(String input) {
      String html = input.replaceAll("\\\\n", "<br>\n");
      return html;
    }

    public AboutDialog(BouncyActivity fr) {
      super(fr, true);
      frame = fr;
      setLocationRelativeTo(fr);
      Container c = getContentPane();
      String about_text = fr.getString("about_text");

      String fieldName = "table" + fr.currentLevel + "_rules";
      String helpText = fr.getString(fieldName);
      about_text = toHtmlList(about_text);
      about_text = about_text.replace("[TABLE_RULES]", toHtmlList(helpText));
      helpText = "<html>" + toHtmlLineBreaks(about_text) + "</html>";

      helpField = new JLabel(helpText);
      //helpField.setAlignmentX(CENTER_ALIGNMENT);
      c.add(helpField, BorderLayout.CENTER);

      JButton okbutton = new JButton(new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public Object getValue(String key) {
          if (key == Action.NAME) return "OK";
          /*
          if (key == Action.ACCELERATOR_KEY) {
            return KeyStroke.getKeyStroke(KeyEvent.KEYCODE_ENTER, 0);
          }
          */
          return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
          fr.updateFromPreferences();
          AboutDialog.this.dispose();
        }});
      JPanel south = new JPanel();
      south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
      south.add(Box.createHorizontalGlue());
      south.add(okbutton);
      south.add(Box.createHorizontalGlue());
      this.getRootPane().setDefaultButton(okbutton);
      c.add(south, BorderLayout.SOUTH);

      Dimension frameDim = frame.getSize();
      this.setPreferredSize(new Dimension((int)(frameDim.width * 0.66),
          (int)(frameDim.height * 0.66)));
      this.pack();
      this.setVisible(true);
    }


}
