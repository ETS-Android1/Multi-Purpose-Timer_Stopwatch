package com.armcomptech.akash.simpletimer4;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements  ExampleDialog.ExmapleDialogListner{

    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private ProgressBar mProgressBar;
    private Button mButtonSetTimer;
    private CountDownTimer mCountDownTimer;
    private TextView mMillis;
    private EditText mRepeatText;

    private boolean mTimerRunning;
    private boolean BlinkTimerStopRequest;
    private boolean keepRepeating;
    private boolean onceMore;


    private boolean heartbeatChecked;
    private boolean soundChecked;
    private boolean repeatTimerChecked;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private int alternate;
    private int timesRepeat;
    private int timeRepeatCount;

    MediaPlayer player;


    private Button mDonate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mDonate = findViewById(R.id.Donate);
        mButtonSetTimer = findViewById(R.id.setTimer);
        mProgressBar = findViewById(R.id.progressBar);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);

        mButtonSetTimer.setBackgroundColor(Color.TRANSPARENT);
        mMillis = findViewById(R.id.millis);
        mRepeatText = findViewById(R.id.repeat_text);

        mButtonSetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeRepeatCount = 1;
                timesRepeat = 1;
                resetTimer();
            }
        });


        heartbeatChecked = true;
        soundChecked = true;
        repeatTimerChecked = true;
        timesRepeat = 1;
        timeRepeatCount = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }

        switch (id) {
            case R.id.check_heartbeat:
                heartbeatChecked = !heartbeatChecked;
                break;

            case R.id.check_sound:
                soundChecked = !soundChecked;
                break;

            case R.id.repeat_timer:
                repeatTimerChecked = !repeatTimerChecked;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void repeatTimeUp() {
        stopPlayer();
        player = MediaPlayer.create(this, R.raw.tune1);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mButtonReset.performClick();
                mButtonStartPause.performClick();
            }
        });
    }

    public void repeatTimeUpp() {
        if (soundChecked) {
            if (player != null) {
                stopPlayer();
            } else {
                player = MediaPlayer.create(this, R.raw.tune1);
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        //stopPlayer();
                        BlinkTimerStopRequest = true;
                        mCountDownTimer.cancel();
                        mCountDownTimer.start();
                    }
                });
            }
            player.start();
        }

        alternate = 0;
        BlinkTimerStopRequest = false;
        Thread blink = new Thread() {
            @Override
            public  void run() {
                for (int i = 0; i < 2; i++) {
                    try {
                        Thread.sleep(400);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (alternate % 2 == 0) {
                                    alternate++;
                                    mProgressBar.setProgress(0, false);
                                }
                                else {
                                    alternate++;
                                    mProgressBar.setProgress((int)mStartTimeInMillis, false);
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        blink.start();
    }

    public void timeUp() {
        if (soundChecked) {
            if (player != null) {
                stopPlayer();
            } else {
                player = MediaPlayer.create(this, R.raw.tune1);
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        player.seekTo(0);
                        player.start();
                    }
                });
            }
            player.start();
        }

        alternate = 0;
        BlinkTimerStopRequest = false;
        Thread blink = new Thread() {
          @Override
          public void run() {
              while((!isInterrupted()) && (!BlinkTimerStopRequest)) {
                  try {
                      Thread.sleep(400);

                      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              if (alternate % 2 == 0) {
                                  alternate++;
                                  mProgressBar.setProgress(0, false);
                              }
                              else {
                                  alternate++;
                                  mProgressBar.setProgress((int)mStartTimeInMillis, false);
                              }
                          }
                      });
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }
        };
        blink.start();
    }

    boolean under5 = false;
    boolean under15 = false;
    boolean under35 = false;
    boolean over35 = false;

    public void heartbeat() {
        if (heartbeatChecked) {

            player = MediaPlayer.create(this, R.raw.heartbeatnormal2);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.seekTo(0);
                    player.start();
                }
            });
            player.start();
        }
    }

    public void heartbeatTemp() {
        double mdTimeLeftInMillis = (double)mTimeLeftInMillis;
        double mdStartTimeInMillis = (double)mStartTimeInMillis;
        double percentThroughTimer = mdTimeLeftInMillis/mdStartTimeInMillis * 100;

        if (percentThroughTimer < 5) {
            under5 = true;
        } else if (percentThroughTimer < 15) {
            under15 = true;
        } else if (percentThroughTimer < 35) {
            under35 = true;
        } else {
            over35 = true;
        }

        if ((percentThroughTimer < 5) && (under5)) {
            Toast.makeText(this, "Under 5", Toast.LENGTH_SHORT).show();
            stopPlayer();
            player = MediaPlayer.create(this, R.raw.heartbeatfastest);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.seekTo(0);
                    player.start();
                }
            });
        } else if ((percentThroughTimer < 5) && (under15)) {
            Toast.makeText(this, "Under 15", Toast.LENGTH_SHORT).show();
            stopPlayer();
            player = MediaPlayer.create(this, R.raw.heartbeatfast);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.seekTo(0);
                    player.start();
                }
            });
        } else if ((percentThroughTimer < 5) && (under35)) {
            Toast.makeText(this, "Under 35", Toast.LENGTH_SHORT).show();
            stopPlayer();
            player = MediaPlayer.create(this, R.raw.heartbeatnormal);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.seekTo(0);
                    player.start();
                }
            });
        } else {
            stopPlayer();
            player = MediaPlayer.create(this, R.raw.heartbeatslow);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.seekTo(0);
                    player.start();
                }
            });
        }

        player.start();
    }

    private void setBlinkTimerStopRequest() {
        BlinkTimerStopRequest = true;
    }

    private void stopPlayer() {
        if (player != null) {
            player.release();
            player = null;
//            Toast.makeText(this, "Song stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDialog() {
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(), "Set Timer Here");
    }

    public void applyText(String time){

        long input = Long.parseLong(time);
        long hour = input / 10000;
        long minuteraw = (input - (hour * 10000)) ;
        long minuteone = minuteraw / 1000;
        long minutetwo = (minuteraw % 1000) / 100;
        long minute = (minuteone * 10) + minutetwo;
        long second = input - ((hour * 10000) + (minute * 100));
        long finalsecond = (hour * 3600) + (minute * 60) + second;

        if (time.length() == 0) {
            Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        //long millisInput = Long.parseLong(time) * 1000;
        long millisInput = finalsecond * 1000;
        if (millisInput == 0) {
            Toast.makeText(MainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
            return;
        }

        setTime(millisInput);
    }

    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();

    }

    private void onceMore() {
        if (onceMore) {
            stopPlayer();
            player = MediaPlayer.create(this, R.raw.tune1);
            player.start();

            mCountDownTimer = new CountDownTimer(2000, 400) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (alternate % 2 == 0) {
                        alternate++;
                        mProgressBar.setProgress(0, false);
                    }
                    else {
                        alternate++;
                        mProgressBar.setProgress((int)mStartTimeInMillis, false);
                    }
                }

                @Override
                public void onFinish() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Timer Repeat Count: " + timeRepeatCount + " Times to repeat: " + timesRepeat + " Done ",
                            Toast.LENGTH_SHORT);

                    toast.show();
                }
            };

//            Thread wait = new Thread() {
//
//                public void run() {
//                        try {
//                            Thread.sleep(400);
//
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (alternate % 2 == 0) {
//                                        alternate++;
//                                        mProgressBar.setProgress(0, false);
//                                    }
//                                    else {
//                                        alternate++;
//                                        mProgressBar.setProgress((int)mStartTimeInMillis, false);
//                                    }
//                                }
//                            });
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                }
//            };
//            wait.start();

            Toast toast = Toast.makeText(getApplicationContext(),
                    "Timer Repeat Count: " + timeRepeatCount + " Times to repeat: " + timesRepeat,
                    Toast.LENGTH_SHORT);

            toast.show();
            //mCountDownTimer.start();
            stopPlayer();
        }
    }

    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        heartbeat();
        //timesRepeat = Integer.parseInt(mRepeatText.getText().toString());

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeRepeatCount++;
                mTimerRunning = false;
                updateWatchInterface();
                mTimeLeftInMillis = 0;
                mMillis.setText("000");
                mProgressBar.setProgress(0, true);
                stopPlayer();
                timeUp();

//                if (repeatTimerChecked && (timeRepeatCount < timesRepeat)) {
//                    onceMore = true;
//                    repeatTimeUp();
//                }
//                else {
//                    onceMore = false;
//                    timeUp();
//                }
            }
        }.start();

        mTimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateWatchInterface();
        stopPlayer();
    }

    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
        mButtonStartPause.setBackgroundResource(R.drawable.playicon);
        setBlinkTimerStopRequest();
        stopPlayer();
        mProgressBar.setProgress((int)mStartTimeInMillis,true);

        if (!onceMore) {
            timeRepeatCount = 0;
        }
    }


    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;

        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
            mTextViewCountDown.setTextSize(60);
            mMillis.setTextSize(25);
            if (hours > 9) {
                mTextViewCountDown.setTextSize(54);
                mMillis.setTextSize(30);
            }
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
            mTextViewCountDown.setTextSize(70);
            mMillis.setTextSize(30);
        }

        String millisFormatted;
        millisFormatted = String.format(Locale.getDefault(), "%02d", (mTimeLeftInMillis % 1000));


        mTextViewCountDown.setText(timeLeftFormatted);
        mMillis.setText(millisFormatted);
        mProgressBar.setMax((int)mStartTimeInMillis);
        mProgressBar.setProgress((int)mTimeLeftInMillis,true);

    }

    private void updateWatchInterface() {
        if (mTimerRunning) {
            mButtonSetTimer.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setBackgroundResource(R.drawable.pauseicon6);
        } else {
            mButtonSetTimer.setVisibility(View.VISIBLE);
            if (mCountDownTimer != null)
            {
                mButtonStartPause.setBackgroundResource(R.drawable.playicon);
            } else {
                mButtonStartPause.setBackgroundResource(R.drawable.playicon);
            }

            if (mTimeLeftInMillis < 100) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }
}
