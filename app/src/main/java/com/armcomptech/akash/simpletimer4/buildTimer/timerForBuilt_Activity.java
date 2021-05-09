package com.armcomptech.akash.simpletimer4.buildTimer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.TabbedView.TabbedActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class timerForBuilt_Activity extends AppCompatActivity {

    FloatingActionButton pauseButton;
    FloatingActionButton startButton;
    FloatingActionButton resetButton;
    FloatingActionButton nextButton;
    ListView upNextListView;
    ProgressBar currentProgressBar;
    ProgressBar totalProgressBar;
    TextView text_countdown;
    TextView text_millis;
    TextView text_groupAndTimerName;

    ArrayList<String> timerNameArray = new ArrayList<>();
    ArrayList<String> groupNameArray = new ArrayList<>();
    ArrayList<Long> timerTimeArray = new ArrayList<>();
    ArrayList<String> stringOfTimerArray = new ArrayList<>();

    ArrayList<String> originalTimerNameArray = new ArrayList<>();
    ArrayList<String> originalGroupNameArray = new ArrayList<>();
    ArrayList<Integer> originalTimerTimeArray = new ArrayList<>();
    ArrayList<String> originalStringOfTimerArray = new ArrayList<>();

    boolean timerPlaying = false;
    boolean timerPaused = false;
    boolean timerReset = true;

    long mStartTimeInMillis = 0;
    long mLeftTimeInMillis = 0;
    long totalStartTime = 0;
    long totalElapsedTime = 0;

    private int alternate;
    private Thread blinkThread;

    ArrayAdapter upNextArrayAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    private AdView banner_adView;
    AdRequest banner_adRequest;
    InterstitialAd mResetButtonInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_for_built);

        if (!isRemovedAds()) {
            banner_adView = findViewById(R.id.banner_ad);
            banner_adRequest = new AdRequest.Builder().build();
            banner_adView.loadAd(banner_adRequest);
            banner_adView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    banner_adView.setVisibility(View.VISIBLE);
                    logFirebaseAnalyticsEvents("Loaded banner ad");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    banner_adView.setVisibility(View.GONE);
                    logFirebaseAnalyticsEvents("Failed to load banner ad");
                }
            });
        }

        if (!TabbedActivity.disableFirebaseLogging) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle("   Your Built Timer: " + getIntent().getStringExtra("masterName"));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_build_white);
        }

        pauseButton = findViewById(R.id.button_pause);
        startButton = findViewById(R.id.button_start);
        resetButton = findViewById(R.id.button_reset);
        nextButton = findViewById(R.id.button_next);
        upNextListView = findViewById(R.id.upNext_Info);
        currentProgressBar = findViewById(R.id.currentTimerProgress);
        totalProgressBar = findViewById(R.id.totalProgress);
        text_countdown = findViewById(R.id.text_view_countdown);
        text_millis = findViewById(R.id.millis);
        text_groupAndTimerName = findViewById(R.id.currentGroupAndTimerName);

        originalTimerNameArray = getIntent().getStringArrayListExtra("timerName");
        originalGroupNameArray = getIntent().getStringArrayListExtra("groupName");
        originalTimerTimeArray = getIntent().getIntegerArrayListExtra("timerTime");
        originalStringOfTimerArray = getIntent().getStringArrayListExtra("stringOfTimer");

        timerNameArray.addAll(Objects.requireNonNull(originalTimerNameArray));
        groupNameArray.addAll(Objects.requireNonNull(originalGroupNameArray));
        for (int time: Objects.requireNonNull(originalTimerTimeArray)) {
            timerTimeArray.add((long) time);
        }
        stringOfTimerArray.addAll(Objects.requireNonNull(originalStringOfTimerArray));

        onCreatePrepareUI();

        upNextArrayAdapter = new ArrayAdapter<>(this, R.layout.timername_autocomplete_textview, R.id.autoComplete_name_textView, stringOfTimerArray);
        upNextListView.setAdapter(upNextArrayAdapter);

        pauseButton.setOnClickListener(v -> {
            startButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.INVISIBLE);
            resetButton.setVisibility(View.VISIBLE);
            pauseTimer();

            logFirebaseAnalyticsEvents("Built Timer Paused");
        });

        startButton.setOnClickListener(v -> {
            startButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            resetButton.setVisibility(View.INVISIBLE);
            startTimer();

            logFirebaseAnalyticsEvents("Built Timer Started");
        });

        resetButton.setOnClickListener(v -> {
            pauseButton.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.VISIBLE);
            resetButton.setVisibility(View.INVISIBLE);
            resetTimer();

            logFirebaseAnalyticsEvents("Built Timer Reset");

            if (blinkThread != null) {
                blinkThread.interrupt();
            }

            if (!isRemovedAds()) {
                if (mResetButtonInterstitialAd != null) {
                    mResetButtonInterstitialAd.show(timerForBuilt_Activity.this);
                    logFirebaseAnalyticsEvents("Showed Ad");
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                    logFirebaseAnalyticsEvents("Ad not loaded");
                }
            }
        });

        nextButton.setOnClickListener(v -> {
            nextTimer();
            logFirebaseAnalyticsEvents("Built Timer Next Timer");
        });

        IntentFilter intentFilter1 = new IntentFilter("BuildTimerOnTick");
        BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mLeftTimeInMillis = intent.getLongExtra("TimeRemaining", 0);
                onTick();
            }
        };

        IntentFilter intentFilter2 = new IntentFilter("BuildTimerOnFinish");
        BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onFinish();
            }
        };

        registerReceiver(broadcastReceiver1, intentFilter1);
        registerReceiver(broadcastReceiver2, intentFilter2);

        if (!isRemovedAds()) {
            MobileAds.initialize(this,
                    initializationStatus -> {

                    });

            //reset button ad
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(
                    this,
                    getString(R.string.resetButton_interstitial_ad_id),
                    adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mResetButtonInterstitialAd = interstitialAd;
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            mResetButtonInterstitialAd = null;
                        }
                    }
            );
        }
    }

    public boolean isRemovedAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("removed_Ads", false);
    }

    @SuppressLint("SetTextI18n")
    private void onCreatePrepareUI() {
        resetButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);

        if (groupNameArray.size() == 0 || timerNameArray.size() == 0) {
            finish();
            return;
        }

        text_groupAndTimerName.setText(groupNameArray.get(0) + " - " + timerNameArray.get(0));

        mStartTimeInMillis = timerTimeArray.get(0);
        mLeftTimeInMillis = timerTimeArray.get(0);
        setUITimerTime();

        currentProgressBar.setMax(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentProgressBar.setProgress(100, true);
        } else {
            currentProgressBar.setProgress(100);
        }

        for (long time: timerTimeArray) {
            totalStartTime += time;
        }

        totalProgressBar.setMax((int) totalStartTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            totalProgressBar.setProgress((int) totalStartTime, true);
        } else {
            totalProgressBar.setProgress((int) totalStartTime);
        }

        if (timerTimeArray.size() <= 1) {
            nextButton.setVisibility(View.INVISIBLE);
            upNextListView.setVisibility(View.GONE);
        }
    }

    private void startTimer() {
        Intent intentService = new Intent(this, buildTimerWithService.class);

        if (timerPaused) {
            Intent intent1local = new Intent();
            intent1local.setAction("BuildTimerTimerPlayer");
            intent1local.putExtra("BuildTimerPlayer", "Resume");
            sendBroadcast(intent1local);
        } else if (timerReset) {
            mStartTimeInMillis = timerTimeArray.get(0);
            mLeftTimeInMillis = timerTimeArray.get(0);

            String nameToSend = groupNameArray.get(0) + " - " + timerNameArray.get(0);
            intentService.putExtra("BuildTimerTimeValue", mStartTimeInMillis);
            intentService.putExtra("BuildTimerTimerAndGroupName", nameToSend);
            startService(intentService);
        }

        timerPlaying = true;
        timerPaused = false;
        timerReset = false;
    }

    private void pauseTimer() {
        timerPlaying = false;
        timerPaused = true;
        timerReset = false;

        Intent intent1local = new Intent();
        intent1local.setAction("BuildTimerTimerPlayer");
        intent1local.putExtra("BuildTimerPlayer", "Pause");
        sendBroadcast(intent1local);
    }

    @SuppressLint("SetTextI18n")
    private void nextTimer() {
        if (timerPaused) {
            timerPlaying = false;
            timerPaused = false;
            timerReset = true;
        }
        totalElapsedTime += mStartTimeInMillis;

        updateUIForNextTimer();
        prepareForNextTimer();
        updateProgressBar();
        setUITimerTime();
        text_groupAndTimerName.setText(groupNameArray.get(0) + " - " + timerNameArray.get(0));

        if (timerPlaying) {
            Intent intentService = new Intent(this, buildTimerWithService.class);
            String nameToSend = groupNameArray.get(0) + " - " + timerNameArray.get(0);
            intentService.putExtra("BuildTimerTimeValue", mStartTimeInMillis);
            intentService.putExtra("BuildTimerTimerAndGroupName", nameToSend);
            startService(intentService);
        }
    }

    @SuppressLint("SetTextI18n")
    private void resetTimer() {
        timerPlaying = false;
        timerPaused = false;
        timerReset = true;

        Intent intent1local = new Intent();
        intent1local.setAction("BuildTimerTimerPlayer");
        intent1local.putExtra("BuildTimerPlayer", "Reset");
        sendBroadcast(intent1local);

        timerNameArray.clear();
        timerNameArray.addAll(Objects.requireNonNull(originalTimerNameArray));
        groupNameArray.clear();
        groupNameArray.addAll(Objects.requireNonNull(originalGroupNameArray));
        timerTimeArray.clear();
        for (int time: Objects.requireNonNull(originalTimerTimeArray)) {
            timerTimeArray.add((long) time);
        }
        stringOfTimerArray.clear();
        stringOfTimerArray.addAll(Objects.requireNonNull(originalStringOfTimerArray));

        mStartTimeInMillis = timerTimeArray.get(0);
        mLeftTimeInMillis = timerTimeArray.get(0);
        text_groupAndTimerName.setText(groupNameArray.get(0) + " - " + timerNameArray.get(0));

        totalElapsedTime = 0;

        setUITimerTime();
        updateProgressBar();

        if (stringOfTimerArray.size() - 1 <= 0) {
            nextButton.setVisibility(View.INVISIBLE);
            upNextListView.setVisibility(View.GONE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            upNextListView.setVisibility(View.VISIBLE);
        }
        upNextArrayAdapter.notifyDataSetChanged();
    }

    private void onTick() {
        setUITimerTime();
        updateProgressBar();
    }

    @SuppressLint("SetTextI18n")
    private void onFinish() {
        if (timerTimeArray.size() <= 1) {
            blink();
            text_groupAndTimerName.setText("");
            text_countdown.setText("00.000");
            pauseButton.setVisibility(View.INVISIBLE);
            resetButton.setVisibility(View.VISIBLE);

            logFirebaseAnalyticsEvents("Built Timer Time Up");
        } else {
            nextTimer();
        }
    }

    private void blink() {
        currentProgressBar.setMax(100);
        totalProgressBar.setMax(100);
        alternate = 0;
        blinkThread = new Thread() {
            @Override
            public void run() {
                while(!isInterrupted() && !timerReset) {
                    try {
                        Thread.sleep(400);
                        runOnUiThread(() -> {
                            if (alternate % 2 == 0) {
                                currentProgressBar.setProgress(100);
                                totalProgressBar.setProgress(100);
                            }
                            else {
                                currentProgressBar.setProgress(0);
                                totalProgressBar.setProgress(0);
                            }
                            alternate++;
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        blinkThread.start();
    }

    private void updateUIForNextTimer() {
        nextButton.setVisibility(View.VISIBLE);
        upNextListView.setVisibility(View.VISIBLE);

        timerNameArray.remove(0);
        groupNameArray.remove(0);
        timerTimeArray.remove(0);
        stringOfTimerArray.remove(0);
        upNextArrayAdapter.notifyDataSetChanged();

        if (stringOfTimerArray.size() - 1 <= 0) {
            nextButton.setVisibility(View.INVISIBLE);
            upNextListView.setVisibility(View.GONE);
        }
    }

    private void prepareForNextTimer() {
        mStartTimeInMillis = timerTimeArray.get(0);
        mLeftTimeInMillis = timerTimeArray.get(0);
        stopService(new Intent(this, buildTimerWithService.class));
    }

    private void updateProgressBar() {
        long tempLeftTime = totalStartTime - (totalElapsedTime + (mStartTimeInMillis - mLeftTimeInMillis));

        currentProgressBar.setMax((int) mStartTimeInMillis);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentProgressBar.setProgress((int) mLeftTimeInMillis, true);
        } else {
            currentProgressBar.setProgress((int) mLeftTimeInMillis);
        }

        totalProgressBar.setMax((int) totalStartTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            totalProgressBar.setProgress((int) tempLeftTime, true);
        } else {
            totalProgressBar.setProgress((int) tempLeftTime);
        }
    }

    private void setUITimerTime() {
        int minutes = (int) (((mLeftTimeInMillis / 1000) % 3600) / 60);
        text_countdown.setText(getCountDownTimeFormatted((int) mLeftTimeInMillis));
        if (minutes >= 1) {
            text_millis.setText(getMillis(mLeftTimeInMillis));
        } else {
            text_millis.setVisibility(View.INVISIBLE);
        }
    }

    public String getCountDownTimeFormatted(int mTimeLeftInMillis) {
        int hours = mTimeLeftInMillis / 1000 / 3600;
        int minutes = ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (mTimeLeftInMillis / 1000) % 60;
        int millis = mTimeLeftInMillis % 1000;

        String timeLeftFormatted;

        if (hours >= 10) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d:%02d", hours, minutes, seconds);
        } else if (hours >= 1) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes >= 1) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d.%03d", seconds, millis);
        }

        return timeLeftFormatted;
    }

    public String getMillis(long mTimeLeftInMillis) {
        int millis = (int) (mTimeLeftInMillis % 1000);
        return String.valueOf(millis);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopService(new Intent(this, buildTimerWithService.class));
    }

    public void logFirebaseAnalyticsEvents(String eventName) {
        if (!TabbedActivity.disableFirebaseLogging) {
            eventName = eventName.replace(" ", "_");
            eventName = eventName.replace(":", "");

            Bundle bundle = new Bundle();
            bundle.putString("Event", eventName);
            mFirebaseAnalytics.logEvent(eventName, bundle);
        }
    }
}