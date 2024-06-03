package com.dozingcatsoftware.linux;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.GameMessage;
import com.dozingcatsoftware.vectorpinball.model.GameState;

/**
 * This class displays the score and game messages above the game view. When there is no game in
 * progress, it cycles between a "Touch to Start" message, last score, and high scores.
 */
public class ScoreView extends JPanel {

    private static final long serialVersionUID = -4589110350465936240L;
    Field field;

    List<Long> highScores;
    Long lastUpdateTime;

    static final int TOUCH_TO_START_MESSAGE = 0;
    static final int LAST_SCORE_MESSAGE = 1;
    static final int HIGH_SCORE_MESSAGE = 2;

    int gameOverMessageIndex = TOUCH_TO_START_MESSAGE;
    int highScoreIndex = 0;
    int gameOverMessageCycleTime = 3500;

    double fps;
    boolean showFPS = false;

    String debugMessage = null;
    Filler fill;
    JLabel scoreField;
    JLabel multField;
    JLabel fpsField;
    JLabel ballField;

    Icon balls[];

    BouncyActivity frame;

    static NumberFormat SCORE_FORMAT = NumberFormat.getInstance();

    private static JLabel newBWLabel() {
      JLabel l = new JLabel();
      l.setBackground(Color.black);
      l.setForeground(Color.white);
      return l;
    }

    public ScoreView(BouncyActivity f) {
      frame = f;
      this.setBackground(Color.black);
      fill = (Filler)Box.createRigidArea(new Dimension(10,0));
      add(fill);
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      add(Box.createHorizontalGlue());
      scoreField = newBWLabel();
      Font font = scoreField.getFont();
      scoreField.setFont(font.deriveFont(1.5f * font.getSize()));
      scoreField.setForeground(Color.yellow);
      scoreField.setText("0");
      scoreField.setAlignmentX(Component.CENTER_ALIGNMENT);
      add(scoreField);
      add(Box.createHorizontalGlue());
      JPanel right = new JPanel();
      right.setBackground(Color.black);
      right.setLayout(new BoxLayout(right, BoxLayout.LINE_AXIS));
      add(right);
      fpsField = newBWLabel();
      right.add(fpsField);
      right.add(Box.createRigidArea(new Dimension(10,0)));
      multField = newBWLabel();
      multField.setText("");
      right.add(multField);
      right.add(Box.createRigidArea(new Dimension(10,0)));
      ballField = new JLabel();
      right.add(ballField);
      balls = new Icon[5];
      for (int i = 0; i < balls.length; ++i) {
        java.net.URL imageURL = ClassLoader.getSystemResource("icons/balls"+ i +".png");
        ImageIcon imageIcon = new ImageIcon(imageURL);
        Image image = imageIcon.getImage(); // transform it
        float scale = 1.5f;
        Image newimg = image.getScaledInstance(
            (int)(imageIcon.getIconWidth() / scale),
            (int)(imageIcon.getIconHeight() / scale),
            java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  balls[i].
        balls[i] = new ImageIcon(newimg);
      }
      ballField.setIcon(balls[0]);

      addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent componentEvent) {
          fill.changeShape(right.getMinimumSize(), right.getPreferredSize(),
              right.getMaximumSize());
        }
      });
    }

    private Context getContext() {
      return frame;
    }

    //@Override
    public void update() {
        GameMessage msg = null;
        boolean gameInProgress = false;
        boolean ballInPlay = false;
        int totalBalls = 0;
        boolean unlimitedBalls = false;
        int currentBall = 0;
        double multiplier = 0;
        long score = 0;
        synchronized (field) {
            // Show custom message if present.
            msg = field.getGameMessage();
            GameState state = field.getGameState();
            gameInProgress = state.isGameInProgress();
            totalBalls = state.getTotalBalls();
            unlimitedBalls = state.hasUnlimitedBalls();
            currentBall = state.getBallNumber();
            multiplier = state.getScoreMultiplier();
            score = state.getScore();
            ballInPlay = field.getBalls().size() > 0;
        }

        String displayString = (msg != null) ? msg.text : null;
        if (displayString == null) {
            // Show score if game is in progress, otherwise cycle between
            // "Touch to start"/previous score/high score.
            if (gameInProgress) {
                displayString = formatScore(score, unlimitedBalls);

                scoreField.setText(Long.toString(score));
                if (showFPS && fps > 0) {
                  fpsField.setText(String.format("%.1f", fps));
                } else {
                  fpsField.setText("");
                }
                if (unlimitedBalls) {
                  ballField.setIcon(balls[4]);
                } else {
                  if (! gameInProgress) {
                    ballField.setIcon(balls[3]);
                  } else {
                    ballField.setIcon(balls[totalBalls - currentBall + 1
                                            - (ballInPlay ? 1 : 0)]);
                  }
                }
                if (multiplier > 1) {
                  int intValue = (int) multiplier;
                  String multiplierString = (multiplier == intValue) ?
                          intValue + "x" : String.format("%.2fx", multiplier);
                  multField.setText(multiplierString);
                } else {
                  multField.setText("");
                }
            } else {
                long now = currentMillis();
                if (lastUpdateTime == null) {
                    lastUpdateTime = now;
                }
                else if (now - lastUpdateTime > gameOverMessageCycleTime) {
                    cycleGameOverMessage(score);
                    lastUpdateTime = now;
                }
                displayString = displayedGameOverMessage(score, unlimitedBalls);
            }
        }
        if (displayString != null) {
          scoreField.setText(displayString);
        }
        this.invalidate();
    }

    long currentMillis() {
        return System.currentTimeMillis();
    }


    // Cycles to the next message to show when there is not game in progress. This can be
    // "Touch to start", the last score if available, or one of the previous high scores.
    void cycleGameOverMessage(long lastScore) {
        switch (gameOverMessageIndex) {
            case TOUCH_TO_START_MESSAGE:
                if (lastScore > 0) {
                    gameOverMessageIndex = LAST_SCORE_MESSAGE;
                }
                else if (highScores.get(0) > 0) {
                    gameOverMessageIndex = HIGH_SCORE_MESSAGE;
                    highScoreIndex = 0;
                }
                break;
            case LAST_SCORE_MESSAGE:
                if (highScores.get(0) > 0) {
                    gameOverMessageIndex = HIGH_SCORE_MESSAGE;
                    highScoreIndex = 0;
                }
                break;
            case HIGH_SCORE_MESSAGE:
                highScoreIndex++;
                if (highScoreIndex >= highScores.size() || highScores.get(highScoreIndex) <= 0) {
                    highScoreIndex = 0;
                    gameOverMessageIndex = TOUCH_TO_START_MESSAGE;
                }
                break;
            default:
                throw new IllegalStateException(
                        "Unknown gameOverMessageIndex: " + gameOverMessageIndex);
        }
    }

    // Returns message to show when game is not in progress.
    String displayedGameOverMessage(long lastScore, boolean unlimitedBalls) {
        switch (gameOverMessageIndex) {
            case TOUCH_TO_START_MESSAGE:
                return getContext().getString("touch_to_start_message");
            case LAST_SCORE_MESSAGE:
                return getContext().getString(
                        "last_score_message", formatScore(lastScore, unlimitedBalls));
            case HIGH_SCORE_MESSAGE:
                // highScoreIndex could be too high if we just switched from a different table.
                int index = Math.min(highScoreIndex, this.highScores.size() - 1);
                // High scores are never recorded when using unlimited balls.
                String formattedScore = formatScore(this.highScores.get(index), false);
                if (index == 0) {
                    return getContext().getString("top_high_score_message", formattedScore);
                }
                else {
                    return getContext().getString(
                            "other_high_score_message", index + 1, formattedScore);
                }
            default:
                throw new IllegalStateException(
                        "Unknown gameOverMessageIndex: " + gameOverMessageIndex);
        }
    }

    private String formatScore(long score, boolean unlimitedBalls) {
        String s = SCORE_FORMAT.format(score);
        return (unlimitedBalls) ? s + "*" : s;
    }

    public void setField(Field value) {
        field = value;
    }

    public void setHighScores(List<Long> value) {
        highScores = value;
    }

    public void setFPS(double value) {
        fps = value;
    }

    public void setShowFPS(boolean value) {
        showFPS = value;
    }

    public void setDebugMessage(String msg) {
        debugMessage = msg;
    }
}
