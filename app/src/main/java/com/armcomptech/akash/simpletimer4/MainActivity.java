package com.armcomptech.akash.simpletimer4;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import static com.App.MAIN_CHANNEL_ID;

public class MainActivity extends AppCompatActivity implements ExampleDialog.ExmapleDialogListner{

    private TextView mTextViewCountDown;
    private FloatingActionButton mButtonStart;
    private FloatingActionButton mButtonPause;
    private FloatingActionButton mButtonReset;
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
    private boolean showNotification;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private int alternate;

    MediaPlayer player;
    private NotificationManagerCompat notificationManager;
    private InterstitialAd mHappyButtonInterstitialAd;

    ArrayList<String> timerName = new ArrayList<>();
    ArrayList<Integer> count = new ArrayList<>();
    ArrayList<Integer> timeInSeconds = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    private static MainActivity instance;
    public String currentTimerName;
    public int currentTimerNamePosition;
    public int ticksToPass;
    public int counter;

    //TODO: Change disableFirebaseLogging to false when releasing
    private static Boolean disableFirebaseLogging = false;
    private static FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        notificationManager = NotificationManagerCompat.from(this);

        loadData(); //load saved data when opening the app

        if (!disableFirebaseLogging) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App Opened");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        }

        if (timerName == null) {
            timerName = new ArrayList<>();
            count = new ArrayList<>();
            timeInSeconds = new ArrayList<>();
        }

        //ad stuff
        //noinspection deprecation
        MobileAds.initialize(this,getString(R.string.admob_app_id));

        //reset button ad
        InterstitialAd mResetButtonInterstitialAd = new InterstitialAd(this);
        mResetButtonInterstitialAd.setAdUnitId(getString(R.string.resetButton_interstital_ad_id));
        if (!disableFirebaseLogging) {
            mResetButtonInterstitialAd.loadAd(new AdRequest.Builder().build());
        } else {
            mResetButtonInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("E5CC1736905A67B0077760DE2AFF519D").build());//test device
        }

        mProgressBar = findViewById(R.id.progressBar);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStart = findViewById(R.id.button_start);
        mButtonPause = findViewById(R.id.button_pause);
        mButtonReset = findViewById(R.id.button_reset);
        mTimerNameEditText = findViewById(R.id.timerNameEditText);
        mMillis = findViewById(R.id.millis);
        mRepeatSwitch = findViewById(R.id.repeat_Switch);
        mRepeatSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> logFirebaseAnalyticsEvents("Repeat Switch: " + isChecked));

        mTimerNameTextView = findViewById(R.id.timerNameTextView);
        mTimerNameTextView.setVisibility(View.INVISIBLE );

        mButtonSetTimer = findViewById(R.id.setTimer);
        mButtonSetTimer.setBackgroundColor(Color.TRANSPARENT);

        mButtonSetTimer.setOnClickListener(v -> openDialog());

        mButtonPause.hide();

        setTime(100000); //default 1 minute timer

        mButtonStart.setOnClickListener( v -> {
            Bundle bundle = new Bundle();
            bundle.putString("Event", "Start Timer");
            bundle.putString("Time", String.valueOf(mTimeLeftInMillis/1000));
            bundle.putString("Name", getTimerName());
            mFirebaseAnalytics.logEvent("Start_Timer", bundle);

            mButtonStart.hide();
            mButtonPause.show();
            mButtonReset.hide();

            if (mTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
                counter = 0;

                //only update during the start
                // mProgressBar.getProgress() ==
                if (mTimeLeftInMillis == mStartTimeInMillis) {
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
        });

        mButtonPause.setOnClickListener( v -> {
            logFirebaseAnalyticsEvents("Timer Paused");

            mButtonPause.hide();
            mButtonStart.show();
            mButtonReset.show();

            if (mTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
                counter = 0;

                //only update during the start
                // mProgressBar.getProgress() ==
                if (mTimeLeftInMillis == mStartTimeInMillis) {
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
        });

        mButtonReset.setOnClickListener(v -> {
            logFirebaseAnalyticsEvents("Reset Timer");

            mButtonStart.show();
            mButtonStart.hide();
            resetTimer();

            mTimerNameTextView.setVisibility(View.INVISIBLE);
            mTimerNameEditText.setVisibility(View.VISIBLE);

            if (mResetButtonInterstitialAd.isLoaded()) {
                mResetButtonInterstitialAd.show();
                logFirebaseAnalyticsEvents("Showed Ad");
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
                logFirebaseAnalyticsEvents("Ad not loaded");
            }
        });
        heartbeatChecked = true;
        soundChecked = true;
        showNotification = true;
    }

    public static void logFirebaseAnalyticsEvents(String eventName) {
        if (!disableFirebaseLogging) {
            Bundle bundle = new Bundle();
            bundle.putString("Event", eventName);
            mFirebaseAnalytics.logEvent(eventName.replace(" ", "_"), bundle);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(1);
        stopPlayer();
        showNotification = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        notificationManager.cancel(1);
        stopPlayer();
        showNotification = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationManager.cancel(1);
        showNotification = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        notificationManager.cancel(1);
        stopPlayer();
        showNotification = true;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    private String getTimerName() {
        String timerName = mTimerNameEditText.getText().toString();
        if (timerName.matches("")) {
            timerName = "General";
        }

        logFirebaseAnalyticsEvents("TimerName: " + timerName);
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        menu.add(0, R.id.privacy_policy, 2, menuIconWithText(getResources().getDrawable(R.drawable.ic_lock_black), "Privacy Policy"));
        menu.add(0, R.id.statistics_activity, 1, menuIconWithText(getResources().getDrawable(R.drawable.ic_data_usage_black), "Statistics"));
//        menu.add(0, Menu.NONE, 3, menuIconWithText(getResources().getDrawable(R.drawable.ic_settings_black), "Settings"));

        return true;
    }

    private CharSequence menuIconWithText(Drawable r, String title) {

        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
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
                logFirebaseAnalyticsEvents("Heartbeat Checked: " + heartbeatChecked);

                //refresh the heartbeat sound
                if (mTimerRunning) {
                    mButtonPause.performClick();
                    mButtonStart.performClick();
                }
                break;

            case R.id.check_sound:
                soundChecked = !soundChecked;
                logFirebaseAnalyticsEvents("Sound Checked: " + heartbeatChecked);

                break;

            case R.id.statistics_activity:
                logFirebaseAnalyticsEvents("Opened Statistics");
                startActivity(new Intent(this, statisticsActivity.class));
                break;

            case R.id.privacy_policy:
                logFirebaseAnalyticsEvents("Opened Privacy Policy");
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://timerpolicy.blogspot.com/2019/06/privacy-policy-armcomptech-built.html"));
                startActivity(myWebLink);
                break;

            case R.id.ad_button:
                if (mHappyButtonInterstitialAd.isLoaded()) {

                    //pause the timer when the ad is opened
                    if (mTimerRunning) {
                        pauseTimer();
                    }

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
        //TODO: Wake up screen if off
        Intent openMainActivity = new Intent(this, MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        logFirebaseAnalyticsEvents("Time Up");
        startActivityIfNeeded(openMainActivity, 0);

        if (soundChecked) {
            if (player != null) {
                stopPlayer();
            } else {
                player = MediaPlayer.create(this, R.raw.endsong);
                player.setOnCompletionListener(mp -> {
                    player.seekTo(0);
                    player.start();
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

                        runOnUiThread(() -> {
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
            player.setOnCompletionListener(mp -> {
                player.seekTo(0);
                //player.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    player.setPlaybackParams(player.getPlaybackParams().setSpeed(Float.parseFloat("1.0")));
                } else {
                    player.start();
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

    public void startTimer() {

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

            @SuppressLint("SetTextI18n")
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

                    mButtonStart.performClick();
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

    public void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateWatchInterface();
        stopPlayer();
//        pauseNotification(getTimeLeftFormatted());
    }

    public void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
        mButtonPause.hide();
        mButtonStart.show();
        setBlinkTimerStopRequest();
        stopPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar.setProgress((int)mStartTimeInMillis,true);
        }
        notificationManager.cancel(1);
    }

    public String getTimeLeftFormatted() {
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

        return timeLeftFormatted;
    }

    private void updateCountDownText() {
        String timeLeftFormatted = getTimeLeftFormatted();

        String millisFormatted;
        millisFormatted = String.format(Locale.getDefault(), "%02d", (mTimeLeftInMillis % 1000));


        mTextViewCountDown.setText(timeLeftFormatted);
        mMillis.setText(millisFormatted);
        mProgressBar.setMax((int)mStartTimeInMillis);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar.setProgress((int)mTimeLeftInMillis,true);
        }

        if (showNotification) {
            showNotification(timeLeftFormatted);
        } else {
            notificationManager.cancel(1);
        }
    }

    private void updateWatchInterface() {
        if (mTimerRunning) {
            mButtonSetTimer.setVisibility(View.INVISIBLE);
            mButtonReset.hide();
            mButtonStart.hide();
            mButtonPause.show();
        } else {
            mButtonSetTimer.setVisibility(View.VISIBLE);
            mButtonStart.show();
            mButtonPause.hide();

            if (mTimeLeftInMillis < 100) {
                mButtonStart.hide();
                mButtonPause.hide();
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.show();
            } else {
                mButtonReset.hide();
            }
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //cancel notification when application is closed
//    @Override
//    protected void onTaskRemoved() {
//        super.onTaskRemoved();
//        pauseNotification(getTimeLeftFormatted());
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        stopPlayer();
//
//        SharedPreferences prefs = getSharedPreferences("currentTimer", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//
//        editor.putLong("startTimeInMillis", mStartTimeInMillis);
//        editor.putLong("millisLeft", mTimeLeftInMillis);
//        editor.putBoolean("timerRunning", mTimerRunning);
//        editor.putLong("endTime", mEndTime);
//
//        editor.apply();
//
//        if (mCountDownTimer != null) {
//            mCountDownTimer.cancel();
//        }
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        SharedPreferences prefs = getSharedPreferences("currentTimer", MODE_PRIVATE);
//
//        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
//        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
//        mTimerRunning = prefs.getBoolean("timerRunning", false);
//
//        updateCountDownText();
//        updateWatchInterface();
//
//        if (mTimerRunning) {
//            mEndTime = prefs.getLong("endTime", 0);
//            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
//
//            if (mTimeLeftInMillis < 0) {
//                mTimeLeftInMillis = 0;
//                mTimerRunning = false;
//                updateCountDownText();
//                updateWatchInterface();
//            } else {
//                startTimer();
//            }
//        }
//    }

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

    //testing
    public void showNotification(String timeLeft) {
        Notification notification = new NotificationCompat.Builder(this, MAIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(timeLeft)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setSound(null)
                .build();

        notificationManager.notify(1, notification);
    }
}
