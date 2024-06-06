package com.dozingcatsoftware.linux;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.View;

import org.json.JSONException;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dozingcatsoftware.bouncy.FieldLayoutReader;
import com.dozingcatsoftware.bouncy.FieldViewManager;
import com.dozingcatsoftware.bouncy.GL20Renderer;
import com.dozingcatsoftware.bouncy.GLFieldView;
import com.dozingcatsoftware.vectorpinball.model.AudioPlayer;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.FieldDriver;
import com.dozingcatsoftware.vectorpinball.model.GameState;
import com.dozingcatsoftware.vectorpinball.model.IStringResolver;
import com.dozingcatsoftware.vectorpinball.util.IOUtils;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLES2;
import com.jogamp.opengl.GLProfile;


public class BouncyActivity extends JFrame implements Context, KeyListener {
  public static final Logger logger =
      LoggerFactory.getLogger(BouncyActivity.class);

  private static final long serialVersionUID = 2466269111148015178L;

  /** <code>Terminator</code> defines action to be done when closing a frame.
   */
  private WindowAdapter terminator = new  WindowAdapter() {
    /** This method is called when the user initiated closing the window. At
     *  this point, this is only a request, not the real closing, and the close
     *  operation can still be cancelled, provided the defaultCloseOperation is
     *  set properly.
     */
    @Override
    public void windowClosing(WindowEvent we) {
      int reaction = JOptionPane.showConfirmDialog(BouncyActivity.this,
          "Really Exit?", "Cancel", JOptionPane.OK_CANCEL_OPTION);
      switch (reaction) {
      case JOptionPane.CANCEL_OPTION:
        return; // do not close window
      case JOptionPane.OK_OPTION:
        break;
      }
      we.getWindow().dispose();
    }
  };

  public static class RunnableAction extends AbstractAction {
    private Runnable _r;

    public RunnableAction(String title, Runnable r) {
      super(title);
      _r = r;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      _r.run();
    }
  }

  public class HighScorePanel extends JDialog {
    private static final long serialVersionUID = 1455615378637103355L;

    public HighScorePanel() {
      super();
      setHighScores(Collections.emptyList());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setHighScores(List<Long> hs) {
      JList<Long> l = new JList(hs.toArray());
      setContentPane(l);
    }
  }


  private static WindowAdapter closeWindow = new WindowAdapter() {
    @Override
    public void windowClosing(WindowEvent e) {
      e.getWindow().dispose();
    }
  };

  private static final String SHADER_ROOT="src/main/assets/";

  private enum gameButtons {
    start("Start Game"),
    end("End Game"),
    pause("Pause"),
    resume("Resume");

    public static Dimension BUTTONSIZE = new Dimension(100, 25);

    private final String label;
    private gameButtons(String l) { label = l; }
    public String getLabel() { return label; }

    JButton[] buttons;

    public JButton addButton(ActionListener act) {
      if (buttons == null) {
        buttons = new JButton[values().length];
      }
      JButton butt = new JButton(getLabel());
      butt.setActionCommand(name());
      butt.setVerticalTextPosition(AbstractButton.CENTER);
      butt.setPreferredSize(BUTTONSIZE);
      butt.setMaximumSize(BUTTONSIZE);
      butt.addActionListener(act);
      butt.setBackground(Color.black);
      butt.setForeground(Color.white);
      butt.setBorder(new LineBorder(Color.white, 1));
      buttons[ordinal()] = butt;
      return butt;
    }

    public JButton getButton() {
      return buttons[this.ordinal()];
    }
  };

  JMenu fileMenu;

  GLFieldView glFieldView;
  GL20Renderer<GL3> gl20Renderer;

  StringResolver stringResolver;

  boolean unlimitedBallsToggle = false;

  ScoreView scoreView = new ScoreView(this);

  public static Dimension _preferredSize = new Dimension(935, 1028);
  protected Dimension _preferredButtonSize = new Dimension(120, 30);

  int numberOfLevels;
  int currentLevel = 1;
  List<Long> highScores;
  Long lastScore = 0L;
  boolean showingHighScores = false;
  static int MAX_NUM_HIGH_SCORES = 5;
  static String HIGHSCORES_PREFS_KEY = "highScores";
  static String LAST_SCORE_PREFS_KEY = "lastScore";
  static String OLD_HIGHSCORE_PREFS_KEY = "highScore";
  static String INITIAL_LEVEL_PREFS_KEY = "initialLevel";

  boolean useZoom = false;
  static final float ZOOM_FACTOR = 1.5f;

  FieldDriver fieldDriver = new FieldDriver();
  FieldViewManager fieldViewManager = new FieldViewManager();

  boolean scoreUpdated = false;

  public Object getBaseContext() { return null; }

  private class StringResolver implements IStringResolver {
    Map<String, Object> rsrcs;
    Map<String, String> strings = new HashMap<>();

    @SuppressWarnings("unchecked")
    private void readStringResources(File recfile) {
      try {
        rsrcs = XML.toJSONObject(new FileReader(recfile)).toMap();
        rsrcs = (Map<String, Object>)rsrcs.get("resources");
        List<Map<String, String>>l = (List<Map<String, String>>) rsrcs.get("string");
        for (Map<String, String> m : l) {
            strings.put(m.get("name"), m.get("content"));
        }
      } catch (JSONException | FileNotFoundException e) {
        logger.error("Could not read string resources: {}", e);
      }
    }

    public StringResolver() {
      // load defaults
      File stringResourceFile =
              new File("./src/main/res/values/strings.xml");
      readStringResources(stringResourceFile);

      String localeLanguage = System.getenv("LANG");
      int i = localeLanguage.indexOf('_');
      localeLanguage = localeLanguage.substring(0, i);
      if (! localeLanguage.equals("en")) {
        stringResourceFile = new File("./src/main/res/values-" + localeLanguage + "/strings.xml");
        if (stringResourceFile.exists()) {
          readStringResources(stringResourceFile);
        }
      }

    }

    @Override
    public String resolveString(String key, Object... params) {
      return String.format(strings.get(key), params);
    }
  }

  // TODO: PLAY AUDIO
  Field field = new Field(System::currentTimeMillis,
      stringResolver = new StringResolver(),
      //new VPSoundpool.Player()
      AudioPlayer.NoOpPlayer.getInstance()
      );

  void updateButtons() {
    GameState state = field.getGameState();
    boolean paused = state.isPaused();
    boolean gameInProgress = state.isGameInProgress();
    if (gameInProgress) {
      // No table switching during game
      fileMenu.getMenuComponent(0).setEnabled(false);
      fileMenu.getMenuComponent(1).setEnabled(false);
      fileMenu.getMenuComponent(2).setEnabled(false);
      //unlimitedBallsCheckBox.setEnabled(false);
      if (paused) {
        // resume and end game active
        gameButtons.start.getButton().setEnabled(false);
        gameButtons.end.getButton().setEnabled(true);
        gameButtons.pause.getButton().setEnabled(false);
        gameButtons.resume.getButton().setEnabled(true);
      } else {
        // Pause & end game active
        gameButtons.start.getButton().setEnabled(false);
        gameButtons.end.getButton().setEnabled(true);
        gameButtons.pause.getButton().setEnabled(true);
        gameButtons.resume.getButton().setEnabled(false);
      }
    } else {
      fileMenu.getMenuComponent(0).setEnabled(true);
      fileMenu.getMenuComponent(1).setEnabled(true);
      fileMenu.getMenuComponent(2).setEnabled(true);
      // Game not in progress,
      // show high score active, unlimited balls checker active
      //unlimitedBallsCheckBox.setEnabled(true);
      gameButtons.start.getButton().setEnabled(true);
      gameButtons.end.getButton().setEnabled(false);
      gameButtons.pause.getButton().setEnabled(false);
      gameButtons.resume.getButton().setEnabled(false);
    }
  }

  //@Override
  public void onResume() {
    //super.onResume();

    // Go to full screen mode here; if we only do it in onCreate then the UI can get stuck
    // with the navigation bar on top of the field.
    //enterFullscreenMode();
    // Reset frame rate since app or system settings that affect performance could have changed.
    fieldDriver.resetFrameRate();
    unpauseGame();
  }

  //@Override
  public void onPause() {
    pauseGame();
    //super.onPause();
  }

  //@Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    // This handles the main activity pausing and resuming.
    //super.onWindowFocusChanged(hasWindowFocus);
    if (!hasWindowFocus) {
      pauseGame();
    } else {
      // If game is in progress, return to the paused menu rather than immediately resuming.
      if (field.getGameState().isGameInProgress()) {
        if (glFieldView != null) {
          // This may result in multiple calls to onResume, but that seems to be ok.
          glFieldView.onResume();
        }
        fieldViewManager.draw();
        updateButtons();
      }
      else {
        unpauseGame();
      }
    }
  }


  public void pauseGame() {
      //VPSoundpool.pauseMusic();
      GameState state = field.getGameState();
      if (state.isPaused() || !state.isGameInProgress()) return;
      state.setPaused(true);

      fieldDriver.stop();
      if (glFieldView != null) glFieldView.onPause();
      showingHighScores = false;

      updateButtons();
  }

  public void unpauseGame() {
      if (!field.getGameState().isPaused()) return;
      field.getGameState().setPaused(false);

      //handler.postDelayed(this::tick, 75);

      fieldDriver.start();
      if (glFieldView != null) glFieldView.onResume();
      showingHighScores = false;

      updateButtons();
  }

  // Button action methods defined by android:onClick values in main.xml.
  public void doStartGame(View view) {
      if (field.getGameState().isPaused()) {
          unpauseGame();
          return;
      }
      if (!field.getGameState().isGameInProgress()) {
          // https://github.com/dozingcat/Vector-Pinball/issues/91
          // These actions need to be synchronized so that we don't try to
          // start the game while the FieldDriver thread is updating the
          // Box2d world. It's not clear what should be synchronized and what
          // shouldn't; for example pauseGame() above should not be
          // synchronized because that can deadlock the FieldDriver thread.
          // All of this concurrency is badly in need of refactoring.
          synchronized (field) {
              resetFieldForCurrentLevel();
              scoreUpdated = false;
              if (//unlimitedBallsToggle.isChecked()
                  unlimitedBallsToggle) {
                  field.startGameWithUnlimitedBalls();
              }
              else {
                  field.startGame();
              }
          }
          //VPSoundpool.playStart();
          updateButtons();
      }
  }

  public void doEndGame(View view) {
      // Game might be paused, if manually ended from button.
      unpauseGame();
      synchronized (field) {
          field.endGame();
      }
      updateHighScoreAndButtonPanel();
  }

  // Store separate high scores for each field, using unique suffix in prefs key.
  String highScorePrefsKeyForLevel(int theLevel) {
      return HIGHSCORES_PREFS_KEY + "." + theLevel;
  }

  // Store separate high scores for each field, using unique suffix in prefs key.
  String lastScorePrefsKeyForLevel(int theLevel) {
      return LAST_SCORE_PREFS_KEY + "." + theLevel;
  }

  /**
   * Returns a list of the high score stored in SharedPreferences. Always returns a nonempty
   * list, which will be [0] if no high scores have been stored.
   */
  List<Long> highScoresFromPreferences(int theLevel) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
      String scoresAsString = prefs.getString(highScorePrefsKeyForLevel(theLevel), "");
      if (scoresAsString.length() > 0) {
          try {
              String[] fields = scoresAsString.split(",");
              List<Long> scores = new ArrayList<>();
              for (String f : fields) {
                  scores.add(Long.valueOf(f));
              }
              return scores;
          }
          catch (NumberFormatException ex) {
              return Collections.singletonList(0L);
          }
      }
      else {
          // Check pre-1.5 single high score.
          long oldPrefsScore = prefs.getLong(OLD_HIGHSCORE_PREFS_KEY + "." + currentLevel, 0);
          return Collections.singletonList(oldPrefsScore);
      }
  }

  Long lastScoreFromPreferences(int theLevel) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
      return prefs.getLong(lastScorePrefsKeyForLevel(theLevel), 0L);
  }

  void writeHighScoresToPreferences(int level, List<Long> scores, long lastScore) {
      StringBuilder scoresAsString = new StringBuilder();
      scoresAsString.append(scores.get(0));
      for (int i = 1; i < scores.size(); i++) {
          scoresAsString.append(",").append(scores.get(i));
      }
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString(highScorePrefsKeyForLevel(level), scoresAsString.toString());
      editor.putLong(lastScorePrefsKeyForLevel(level), lastScore);
      editor.commit();
  }

  List<Long> highScoresFromPreferencesForCurrentLevel() {
      return highScoresFromPreferences(currentLevel);
  }

  Long lastScoreFromPreferencesForCurrentLevel() {
      return lastScoreFromPreferences(currentLevel);
  }

  // Called every 100 milliseconds while app is visible, to update score view and high score.
  void tick() {
      scoreView.setFPS(fieldDriver.getAverageFPS());
      scoreView.setDebugMessage(field.getDebugMessage());
      scoreView.update();
      updateHighScoreAndButtonPanel();
  }

  /**
   * If the score of the current or previous game is greater than the previous high score,
   * update high score in preferences and ScoreView. Also show button panel if game has ended.
   */
  void updateHighScoreAndButtonPanel() {
      synchronized (field) {
          GameState state = field.getGameState();
          if (! state.isGameInProgress() && !scoreUpdated) {
              scoreUpdated = true;
              updateButtons();

              // No high scores for unlimited balls.
              if (!state.hasUnlimitedBalls()) {
                  long score = field.getGameState().getScore();
                  // Add to high scores list if the score beats the lowest existing high score,
                  // or if all the high score slots aren't taken.
                  if (score > highScores.get(highScores.size() - 1) ||
                          highScores.size() < MAX_NUM_HIGH_SCORES) {
                      this.updateHighScoreForCurrentLevel(score);
                  }
              }
          }
      }
  }

  /** Updates the high score in the ScoreView display, and persists it to SharedPreferences. */
  void updateHighScore(int theLevel, long score) {
      List<Long> newHighScores = new ArrayList<>(this.highScores);
      newHighScores.add(score);
      Collections.sort(newHighScores);
      Collections.reverse(newHighScores);
      if (newHighScores.size() > MAX_NUM_HIGH_SCORES) {
          newHighScores = newHighScores.subList(0, MAX_NUM_HIGH_SCORES);
      }
      this.highScores = newHighScores;
      this.lastScore = score;
      writeHighScoresToPreferences(theLevel, this.highScores, this.lastScore);
      scoreView.setHighScores(this.highScores);
  }

  void updateHighScoreForCurrentLevel(long score) {
    updateHighScore(currentLevel, score);
  }

  int getInitialLevel() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    int startLevel = prefs.getInt(INITIAL_LEVEL_PREFS_KEY, 1);
    if (startLevel < 1 || startLevel > numberOfLevels) startLevel = 1;
    return startLevel;
  }

  void setInitialLevel(int level) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(INITIAL_LEVEL_PREFS_KEY, level);
    editor.commit();
  }

  void switchToTable(int tableNum) {
      scoreView.setHighScores(highScores);
      this.currentLevel = tableNum;
      synchronized (field) {
          resetFieldForCurrentLevel();
      }
      this.setInitialLevel(currentLevel);
      this.highScores = this.highScoresFromPreferencesForCurrentLevel();
      this.lastScore = this.lastScoreFromPreferencesForCurrentLevel();
      // Performance can be different on different tables.
      fieldDriver.resetFrameRate();
  }

  public void doSwitchTable(View view) {
      doNextTable(view);
  }

  public void doNextTable(View view) {
      int nextTableNum = (currentLevel == numberOfLevels) ? 1 : currentLevel + 1;
      switchToTable(nextTableNum);
  }

  public void doPreviousTable(View view) {
      int prevTableNum = (currentLevel == 1) ? numberOfLevels : currentLevel - 1;
      switchToTable(prevTableNum);
  }

  void resetFieldForCurrentLevel() {
      field.resetForLayoutMap(FieldLayoutReader.layoutMapForLevel(this, currentLevel));
  }

  // Update settings from preferences, called at launch and when preferences activity finishes.
  void updateFromPreferences() {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
      fieldViewManager.setIndependentFlippers(prefs.getBoolean("independentFlippers", true));
      // TODO: is this also showing the current score?
      scoreView.setShowFPS(prefs.getBoolean("showFPS", false));

      // If switching line width or OpenGL/Canvas, reset frame rate manager because maximum
      // achievable frame rate may change.
      int lineWidth = 0;
      try {
          lineWidth = prefs.getInt("lineWidth", 0);
      }
      catch (NumberFormatException ignored) {}
      if (lineWidth != fieldViewManager.getCustomLineWidth()) {
          fieldViewManager.setCustomLineWidth(lineWidth);
      }

      unlimitedBallsToggle = prefs.getBoolean("unlimitedBallsToggle", false);

      boolean showBallTrails = prefs.getBoolean("showBallTrails", true);
      field.setBallTrailsEnabled(showBallTrails);

      fieldViewManager.setFieldRenderer(gl20Renderer);

      useZoom = prefs.getBoolean("zoom", true);
      fieldViewManager.setZoom(useZoom ? ZOOM_FACTOR : 1.0f);


      //VPSoundpool.setSoundEnabled(prefs.getBoolean("sound", true));
      //VPSoundpool.setMusicEnabled(prefs.getBoolean("music", true));
      //useHapticFeedback = prefs.getBoolean("haptic", false);
  }

  private void onCreate() {
    this.numberOfLevels = FieldLayoutReader.getNumberOfLevels(this);
    this.currentLevel = getInitialLevel();
    resetFieldForCurrentLevel();

    fieldViewManager.setField(field);
    fieldViewManager.setStartGameAction(() -> doStartGame(null));

    //scoreView = findViewById(R.id.scoreView);
    scoreView.setField(field);

    fieldDriver.setField(field);
    fieldDriver.setDrawFunction(() ->
    {
      BouncyActivity.this.fieldViewManager.draw();
      BouncyActivity.this.tick();
    });

    highScores = this.highScoresFromPreferencesForCurrentLevel();
    lastScore = this.lastScoreFromPreferencesForCurrentLevel();
    scoreView.setHighScores(highScores);

//    buttonPanel = findViewById(R.id.buttonPanel);
//    selectTableRow = findViewById(R.id.selectTableRow);
//    highScorePanel = findViewById(R.id.highScorePanel);
//    nextTableButton = findViewById(R.id.nextTableButton);
//    previousTableButton = findViewById(R.id.previousTableButton);
//    startGameButton = findViewById(R.id.startGameButton);
//    resumeGameButton = findViewById(R.id.resumeGameButton);
//    endGameButton = findViewById(R.id.endGameButton);
//    aboutButton = findViewById(R.id.aboutButton);
//    preferencesButton = findViewById(R.id.preferencesButton);
//    quitButton = findViewById(R.id.quitButton);
//    unlimitedBallsToggle = findViewById(R.id.unlimitedBallsToggle);
//    showHighScoreButton = findViewById(R.id.highScoreButton);
//    hideHighScoreButton = findViewById(R.id.hideHighScoreButton);
//    highScoreListLayout = findViewById(R.id.highScoreListLayout);
//    noHighScoresTextView = findViewById(R.id.noHighScoresTextView);
//    pauseButton = findViewById(R.id.pauseIcon);

    // Ugly workaround that seems to be required when supporting keyboard navigation.
    // In main.xml, all buttons have `android:focusableInTouchMode` set to true.
    // If it's not, then they don't get focused even when using the dpad on a
    // Motorola Droid or plugging in a keyboard to a Pixel 3a. (Android documentation
    // says that the UI should automatically go in and out of touch mode, but that
    // seems to not happen). With that setting, the default touch behavior on a
    // non-focused button is to focus it but not click it. We want a click in that case,
    // so we have to set a touch listener and call `performClick` on a ACTION_UP event
    // (after checking that the event was within the button bounds). This is likely
    // fragile but seems to be working ok.
//    List<View> allButtons = Arrays.asList(
//            nextTableButton, previousTableButton, startGameButton, resumeGameButton, endGameButton,
//            aboutButton, preferencesButton, quitButton, unlimitedBallsToggle, showHighScoreButton, hideHighScoreButton);
//    for (View button : allButtons) {
//        button.setOnTouchListener((view, motionEvent) -> {
//            // Log.i(TAG, "Button motion event: " + motionEvent);
//            // Log.i(TAG, "View: " + view);
//            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                Rect r = new Rect();
//                view.getLocalVisibleRect(r);
//                // Log.i(TAG, "Button rect: " + r);
//                // Log.i(TAG, "Event location: " + motionEvent.getX() + " " + motionEvent.getY());
//                if (r.contains((int)motionEvent.getX(), (int)motionEvent.getY())) {
//                    // Log.i(TAG, "Button click, focused: " + view.hasFocus());
//                    // This calls the button's click action, but for some reason doesn't
//                    // do the ripple animation if the button was previously focused.
//                    view.requestFocus();
//                    view.performClick();
//                    return true;
//                }
//            }
//            return false;
//        });
//    }

    // TODO: allow field configuration to specify whether tilting is allowed
    /*
    orientationListener = new OrientationListener(this, SensorManager.SENSOR_DELAY_GAME,
            new OrientationListener.Delegate() {
        public void receivedOrientationValues(float azimuth, float pitch, float roll) {
            field.receivedOrientationValues(azimuth, pitch, roll);
        }
    });
     */
    updateFromPreferences();

    // Initialize audio, loading resources in a separate thread.
    /* TODO: ACTIVATE SOUND AT SOME TIME
    VPSoundpool.initSounds(this);
    (new Thread(VPSoundpool::loadSounds)).start();
    this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    */
 }

  protected void initFrame() {
    /*
    try {
      _iconPath = getResourcesDir().getAbsolutePath() + "/icons/";
      String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
      UIManager.setLookAndFeel(lookAndFeel);
      if (! new File(_iconPath).isDirectory()) {
        if (lookAndFeel.contains("GTK")) {
          _iconPath = "/usr/share/icons/gnome/";
        }
      }
    } catch (ClassNotFoundException e) {
      // well, we're content with everything we get
    } catch (InstantiationException e) {
      // well, we're content with everything we get
    } catch (IllegalAccessException e) {
      // well, we're content with everything we get
    } catch (UnsupportedLookAndFeelException e) {
      // well, we're content with everything we get
    }
    */

    // use native windowing system to position new frames
    this.setLocationByPlatform(true);
    // set preferred size
    this.setPreferredSize(_preferredSize);
    // set handler for closing operations
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    //this.addWindowListener(terminator);

    // create content panel and add it to the frame
    JPanel contentPane = new JPanel(new BorderLayout());
    this.setContentPane(contentPane);

    GLProfile profile = GLProfile.get(GLProfile.GL3);
    GLCapabilities capabilities = new GLCapabilities(profile);
    // is a GLCanvas
    glFieldView = new GLFieldView(capabilities, null);
    // TODO: GET A SHADERLOOKUPFUNCTION FOR THE SECOND ARGUMENT
    Function<String, String> fn = (String shaderPath) -> {
      try {
        InputStream input = new FileInputStream(SHADER_ROOT + shaderPath);
        return IOUtils.utf8FromStream(input);
      }
      catch(IOException ex) {
        throw new RuntimeException(ex);
      }
    };
    // is a GLEventListener
    gl20Renderer = new GL20Renderer<>(glFieldView, fn);
    gl20Renderer.setManager(fieldViewManager);

    contentPane.add(glFieldView, BorderLayout.CENTER);
    contentPane.add(scoreView, BorderLayout.NORTH);
    this.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowLostFocus(WindowEvent e) { onWindowFocusChanged(false); }

      @Override
      public void windowGainedFocus(WindowEvent e) { onWindowFocusChanged(true); }
    });

    // TODO: MAYBE REMOVE BUTTONS ALTOGETHER
    JPanel buttonPanel = new JPanel();
    buttonPanel.setForeground(Color.white);
    buttonPanel.setBackground(Color.black);
    buttonPanel.setBorder(new EmptyBorder(0,0,0,0));
    ActionListener act = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        gameButtons b = gameButtons.valueOf(e.getActionCommand());
        switch (b) {
        case start: doStartGame(null); break;
        case end: doEndGame(null); break;
        case pause: onPause(); break;
        case resume: onResume(); break;
        }
      }
    };
    for (gameButtons lab : gameButtons.values()) {
      buttonPanel.add(lab.addButton(act));
    }

    contentPane.add(buttonPanel, BorderLayout.SOUTH);
    // TODO: ADD THE MENUS TO THE MENU BAR AND THE BUTTONS TO A PANEL IN THE BOTTOM

    // create menu bar
    JMenuBar menuBar = new JMenuBar();
    menuBar.setOpaque(true);
    menuBar.setPreferredSize(new Dimension(400, 20));
    fileMenu = new JMenu("File");
    fileMenu.add(new RunnableAction("Next Table", new Runnable() {
      @Override
      public void run() {
        BouncyActivity.this.doNextTable(null);
      }}
    ));
    fileMenu.add(new RunnableAction("Previous Table", new Runnable() {
      @Override
      public void run() {
        BouncyActivity.this.doPreviousTable(null);
      }}));
    fileMenu.add(new RunnableAction("Preferences", new Runnable() {
      @Override
      public void run() {
        StartView prefsDialog = new StartView(BouncyActivity.this);
        updateFromPreferences();
      }}));
    fileMenu.add(new RunnableAction("Exit", new Runnable() {
      @Override
      public void run() {
          doEndGame(null);
          BouncyActivity.this.dispose();
      }}));
    menuBar.add(fileMenu);
    this.setJMenuBar(menuBar);
    onCreate();
    //unpauseGame();
    // display the frame
    this.pack();
    this.setVisible(true);
  }

  public BouncyActivity(String title) {
    super(title);
    initFrame();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new BouncyActivity("Basic JOGL App");
    });
  }

  @Override
  public Assets getAssets() {
    return new Assets(new File("src/main/assets/"));
  }

  @Override
  /** Get the localized format string for the given key and return the
   *  formatted string given the arguments.
   */
  public String getString(String string, Object... args) {
    return stringResolver.resolveString(string, args);
  }

  @Override
  public void keyTyped(KeyEvent e) {
    // TODO Auto-generated method stub
    switch (Character.toLowerCase(e.getKeyChar())) {
    case 's':
      doStartGame(null);
      break;
    case 'e':
      doEndGame(null);
      break;
    case 'p':
      onPause();
      break;
    case 'r':
      onResume();
      break;
    }
  }

  @Override
  public void keyPressed(KeyEvent e) { }

  @Override
  public void keyReleased(KeyEvent e) { }


}
