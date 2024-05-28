package com.dozingcatsoftware.linux;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Box;
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
    /*
    Paint textPaint = new Paint();
    Rect textRect = new Rect();

    Paint fpsPaint = new Paint();

    Paint usedBallPaint = new Paint();
    Paint remainingBallPaint = new Paint();
    Paint multiplierPaint = new Paint();
    int backgroundColor = Color.argb(255, 24, 24, 24);
    DisplayMetrics metrics = new DisplayMetrics();
*/

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

    JLabel scoreField;
    JLabel fpsField;
    JLabel ballField;

    Icon balls[];

    static NumberFormat SCORE_FORMAT = NumberFormat.getInstance();

    public ScoreView() {
      this.setBackground(Color.black);
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      JLabel sc = new JLabel("Score: ");
      sc.setBackground(Color.black);
      sc.setForeground(Color.white);
      add(sc);
      scoreField = new JLabel();
      scoreField.setBackground(Color.black);
      scoreField.setForeground(Color.white);
      scoreField.setText("0");
      add(scoreField);
      add(Box.createHorizontalGlue());
      fpsField = new JLabel();
      fpsField.setBackground(Color.black);
      fpsField.setForeground(Color.white);
      fpsField.setText("0");
      add(fpsField);
      add(Box.createRigidArea(new Dimension(10,0)));
      ballField = new JLabel();
      add(ballField);
      balls = new Icon[5];
      for (int i = 0; i < balls.length; ++i) {
        java.net.URL imageURL = ClassLoader.getSystemResource("icons/balls"+ i +".png");
        ImageIcon imageIcon = new ImageIcon(imageURL);
        Image image = imageIcon.getImage(); // transform it
        int scale = 3;
        Image newimg = image.getScaledInstance(
            imageIcon.getIconWidth() / scale,
            imageIcon.getIconHeight() / scale,
            java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  balls[i].
        balls[i] = new ImageIcon(newimg);
      }
      ballField.setIcon(balls[0]);

      /*
        textPaint.setARGB(255, 255, 255, 0);
        textPaint.setAntiAlias(true);
        // setTextSize uses absolute pixels, get screen density to scale.
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        textPaint.setTextSize(24 * metrics.density);

        fpsPaint.setARGB(255, 255, 255, 0);
        fpsPaint.setTextSize(9 * metrics.density);
        fpsPaint.setAntiAlias(true);

        multiplierPaint.setARGB(255, 32, 224, 32);
        multiplierPaint.setTextSize(12 * metrics.density);
        multiplierPaint.setAntiAlias(true);

        usedBallPaint.setARGB(255, 128, 128, 128);
        usedBallPaint.setStyle(Paint.Style.STROKE);
        usedBallPaint.setAntiAlias(true);
        remainingBallPaint.setARGB(255, 224, 224, 224);
        remainingBallPaint.setStyle(Paint.Style.FILL);
        remainingBallPaint.setAntiAlias(true);
        */
    }

    //@Override
    public void onDraw(Canvas c) {
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

        //c.drawColor(backgroundColor);
        String displayString = (msg != null) ? msg.text : null;
/*
        if (displayString == null) {
            // Show score if game is in progress, otherwise cycle between
            // "Touch to start"/previous score/high score.
            if (gameInProgress) {
  */
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
          ballField.setIcon(balls[currentBall]);
        }
        /*
            }
            else {
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
        }*/

        /*
        int width = this.getWidth();
        int height = this.getHeight();
        textPaint.getTextBounds(displayString, 0, displayString.length(), textRect);
        // textRect ends up being too high
        c.drawText(
                displayString,
                width / 2.0f - textRect.width() / 2.0f, height / 2.0f + textRect.height() / 3.0f,
                textPaint);
        if (showFPS && fps > 0) {
            c.drawText(String.format("%.1f fps", fps), 16 * metrics.density, height * 0.25f, fpsPaint);
        }
        if (debugMessage != null) {
            c.drawText(debugMessage, width * 0.02f, height * 0.75f, fpsPaint);
        }
        if (gameInProgress) {
            // Draw balls.
            float ballRadius = height / 10f;
            float ballPaintWidth = ballRadius / 3.2f;
            usedBallPaint.setStrokeWidth(ballPaintWidth);
            remainingBallPaint.setStrokeWidth(ballPaintWidth);
            float ballOuterMargin = 2 * ballRadius;
            float ballCenterY = height - (ballOuterMargin + ballRadius);
            float ballRightmostCenterX = width - ballOuterMargin - ballRadius;
            float distanceBetweenBallCenters = 2 * ballRadius + ballRadius;
            if (unlimitedBalls) {
                // Attempt to show an "infinite" series of balls getting progressively smaller.
                float vanishingBallRadius = ballRadius;
                for (int i = 4; i >= 0; i--) {
                    float ballCenterX = ballRightmostCenterX - (i * distanceBetweenBallCenters);
                    c.drawCircle(ballCenterX, ballCenterY, vanishingBallRadius, remainingBallPaint);
                    vanishingBallRadius *= 0.8f;
                }
            }
            else {
                for (int i = 0; i < totalBalls; i++) {
                    float ballCenterX = ballRightmostCenterX - (i * distanceBetweenBallCenters);
                    // "Remove" ball from display when launched.
                    boolean isRemaining = (currentBall + i + (ballInPlay ? 1 : 0) <= totalBalls);
                    c.drawCircle(ballCenterX, ballCenterY, ballRadius,
                            isRemaining ? remainingBallPaint : usedBallPaint);
                }
            }
            // Draw multiplier if >1. Use X position of ball third from the right.
            if (multiplier > 1) {
                int intValue = (int) multiplier;
                String multiplierString = (multiplier == intValue) ?
                        intValue + "x" : String.format("%.2fx", multiplier);
                float messageStartX = ballRightmostCenterX - 2 * distanceBetweenBallCenters - ballRadius;
                c.drawText(multiplierString, messageStartX, height * 0.4f, multiplierPaint);
            }
        }*/
    }

    long currentMillis() {
        return System.currentTimeMillis();
    }

    /*
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
                return getContext().getString(R.string.touch_to_start_message);
            case LAST_SCORE_MESSAGE:
                return getContext().getString(
                        R.string.last_score_message, formatScore(lastScore, unlimitedBalls));
            case HIGH_SCORE_MESSAGE:
                // highScoreIndex could be too high if we just switched from a different table.
                int index = Math.min(highScoreIndex, this.highScores.size() - 1);
                // High scores are never recorded when using unlimited balls.
                String formattedScore = formatScore(this.highScores.get(index), false);
                if (index == 0) {
                    return getContext().getString(R.string.top_high_score_message, formattedScore);
                }
                else {
                    return getContext().getString(
                            R.string.other_high_score_message, index + 1, formattedScore);
                }
            default:
                throw new IllegalStateException(
                        "Unknown gameOverMessageIndex: " + gameOverMessageIndex);
        }
    }*/

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
