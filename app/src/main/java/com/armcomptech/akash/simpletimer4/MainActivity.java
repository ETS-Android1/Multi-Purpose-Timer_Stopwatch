package com.armcomptech.akash.simpletimer4;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.applovin.sdk.AppLovinSdk;
import com.chartboost.sdk.Chartboost;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import com.applovin.sdk.AppLovinSdk;

public class MainActivity extends AppCompatActivity implements  ExampleDialog.ExmapleDialogListner{

    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private ProgressBar mProgressBar;
    private Button mButtonSetTimer;
    private CountDownTimer mCountDownTimer;
    private TextView mMillis;
    private EditText mTimerNameEditText;
    private TextView mTimerNameTextView;
    private Switch mRepeatSwitch;

    private boolean mTimerRunning;
    private boolean BlinkTimerStopRequest;
    private boolean heartbeatChecked;
    private boolean soundChecked;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private int alternate;

    MediaPlayer player;
    private InterstitialAd mResetButtonInterstitialAd;
    private InterstitialAd mHappyButtonInterstitialAd;

    ArrayList<String> timerName = new ArrayList<String>();
    ArrayList<Integer> count = new ArrayList<Integer>();
    ArrayList<Integer> timeInSeconds = new ArrayList<Integer>();


    public String currentTimerName;
    public int currentTimerNamePosition;
    public int ticksToPass;
    public int counter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        loadData(); //load saved data when opening the app

        //ad stuff
        MobileAds.initialize(this,getString(R.string.admob_app_id));

        Chartboost.startWithAppId(this, "5d12507d18272d0bbe13eced", "e750c201ec23522c7ea3c688bb971ef68823ad5f");
        Chartboost.onCreate(this);

        SdkConfiguration mResetButtonInterstitialAdMoPub = new SdkConfiguration.Builder("7d26297661ba4a1784b331a6f3bde078").build();
        SdkConfiguration mHappyButtonInterstitialAdMoPub = new SdkConfiguration.Builder("a692a5880d0d48ce9463f1e8b4348a22").build();
        MoPub.initializeSdk(getApplication().getApplicationContext(), mResetButtonInterstitialAdMoPub, null);
        MoPub.initializeSdk(getApplication().getApplicationContext(), mHappyButtonInterstitialAdMoPub, null);

        AppLovinSdk.initializeSdk(getApplication().getApplicationContext());

        //reset button ad
        mResetButtonInterstitialAd = new InterstitialAd(this);
        mResetButtonInterstitialAd.setAdUnitId(getString(R.string.resetButton_interstital_ad_id));
        mResetButtonInterstitialAd.loadAd(new AdRequest.Builder().build());
//        mResetButtonInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("E5CC1736905A67B0077760DE2AFF519D").build());//test device

        //happy face ad
        mHappyButtonInterstitialAd = new InterstitialAd(this);
        mHappyButtonInterstitialAd.setAdUnitId(getString(R.string.happyButton_interstital_ad_id));
        mHappyButtonInterstitialAd.loadAd(new AdRequest.Builder().build());
//        mHappyButtonInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("E5CC1736905A67B0077760DE2AFF519D").build());//test device

        mProgressBar = findViewById(R.id.progressBar);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);
        mTimerNameEditText = findViewById(R.id.timerNameEditText);
        mMillis = findViewById(R.id.millis);
        mRepeatSwitch = findViewById(R.id.repeat_Switch);

        mTimerNameTextView = findViewById(R.id.timerNameTextView);
        mTimerNameTextView.setVisibility(View.INVISIBLE );

        mButtonSetTimer = findViewById(R.id.setTimer);
        mButtonSetTimer.setBackgroundColor(Color.TRANSPARENT);

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
                    counter = 0;

                    //only update during the start
                    if (mProgressBar.getProgress() == mStartTimeInMillis) {
                        currentTimerName = getTimerName();

                        //get position of timer name and -1 if it doesn't exist
                        currentTimerNamePosition = timerNamePosition(currentTimerName, timerName);

                        if (currentTimerNamePosition == -1) {
                            timerName.add(currentTimerName);
                            count.add(1);
                            timeInSeconds.add(0);
                            currentTimerNamePosition = timeInSeconds.size() - 1; //make a new position since adding new value which is at the end
                        } else {
                            //increment count
                            count.set(currentTimerNamePosition, count.get(currentTimerNamePosition) + 1);
                        }
                        saveData(); //save data

                        //just to be safe because sometimes second is one less in statistics
                        if (mStartTimeInMillis >= 4000) { //when timer is set more than 4 seconds
                            timeInSeconds.set(currentTimerNamePosition, timeInSeconds.get(currentTimerNamePosition) + 1);
                        }

                        //update interface to show timer name
                        mTimerNameTextView.setVisibility(View.VISIBLE);
                        mTimerNameTextView.setText(currentTimerName);
                        mTimerNameEditText.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();

                mTimerNameTextView.setVisibility(View.INVISIBLE);
                mTimerNameEditText.setVisibility(View.VISIBLE);

                if (mResetButtonInterstitialAd.isLoaded()) {
                    mResetButtonInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
            }
        });
        heartbeatChecked = true;
        soundChecked = true;

    }

    private String getTimerName() {
        String timerName = mTimerNameEditText.getText().toString();
        if (timerName.matches("")) {
            timerName = "General";
        }
        return timerName;
    }

    private int timerNamePosition(String currentTimerName, ArrayList<String> timerName) {
        if (timerName == null) {
            return -1;
        }

        for (int i = 0; i < timerName.size(); i++) {
            if (timerName.get(i).matches(currentTimerName)) {
                return i;
            }
        }
        return -1;
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String timerNameJson = gson.toJson(timerName);
        editor.putString("timer name", timerNameJson);

        String countJson = gson.toJson(count);
        editor.putString("count", countJson);

        String timeInSecondsJson = gson.toJson(timeInSeconds);
        editor.putString("timeInSeconds", timeInSecondsJson);

        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        resetTimer(); //reset the timer when the app starts up
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

                //refresh the heartbeat sound
                mButtonStartPause.performClick();
                mButtonStartPause.performClick();
                break;

            case R.id.check_sound:
                soundChecked = !soundChecked;
                break;

            case R.id.statistics_activity:
                startActivity(new Intent(this, statisticsActiivty.class));
                break;

            case R.id.privacy_policy:
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://timerpolicy.blogspot.com/2019/06/privacy-policy-armcomptech-built.html"));
                startActivity(myWebLink);
                break;

            case R.id.ad_button:
                if (mHappyButtonInterstitialAd.isLoaded()) {
                    mHappyButtonInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void timeUp() {
        if (soundChecked) {
            if (player != null) {
                stopPlayer();
            } else {
                player = MediaPlayer.create(this, R.raw.endsong);
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
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        mProgressBar.setProgress(0, false);
                                    }
                                }
                                else {
                                    alternate++;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        mProgressBar.setProgress((int)mStartTimeInMillis, false);
                                    }
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

    public void heartbeat() {
        if (heartbeatChecked) {

            player = MediaPlayer.create(this, R.raw.heartbeat);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.seekTo(0);
                    //player.start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        player.setPlaybackParams(player.getPlaybackParams().setSpeed(Float.parseFloat("1.0")));
                    } else {
                        player.start();
                    }
                }
            });
            //player.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                player.setPlaybackParams(player.getPlaybackParams().setSpeed(Float.parseFloat("1.0")));
            } else {
                player.start();
            }
        }
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

    private void startTimer() {

        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        heartbeat();
        int countDownInterval = 100;
        if (mStartTimeInMillis <= 30000) {
            countDownInterval = 50;
        }

        ticksToPass = 1000 / countDownInterval;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();

                //basically increment by one every second
                counter++;
                if (ticksToPass == counter) {
                    timeInSeconds.set(currentTimerNamePosition, timeInSeconds.get(currentTimerNamePosition) + 1);
                    saveData();
                    counter = 0;
                }
            }

            @Override
            public void onFinish() {
                if (mRepeatSwitch.isChecked()) {
                    resetTimer();
                    mTimerRunning = false;

                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mButtonStartPause.performClick();
                } else {
                    mTimerRunning = false;
                    updateWatchInterface();
                    mTimeLeftInMillis = 0;
                    mMillis.setText("000");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        mProgressBar.setProgress(0, true);
                    }
                    stopPlayer();
                    timeUp();
                }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar.setProgress((int)mStartTimeInMillis,true);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar.setProgress((int)mTimeLeftInMillis,true);
        }


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

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();

        String timerNameJson = sharedPreferences.getString("timer name", null);
        Type timerNameType = new TypeToken<ArrayList<String>>() {}.getType();
        timerName = gson.fromJson(timerNameJson, timerNameType);

        String countJson = sharedPreferences.getString("count", null);
        Type countType = new TypeToken<ArrayList<Integer>>() {}.getType();
        count = gson.fromJson(countJson, countType);

        String timeInSecondsJson = sharedPreferences.getString("timeInSeconds", null);
        Type timeInSecondsType = new TypeToken<ArrayList<Integer>>() {}.getType();
        timeInSeconds = gson.fromJson(timeInSecondsJson, timeInSecondsType);
    }
}
