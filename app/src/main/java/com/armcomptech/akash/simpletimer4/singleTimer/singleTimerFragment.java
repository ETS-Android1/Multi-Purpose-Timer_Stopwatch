package com.armcomptech.akash.simpletimer4.singleTimer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.TabbedView.TabbedActivity;
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

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.App.MAIN_CHANNEL_ID;

/**
 * A placeholder fragment containing a simple view.
 */
public class singleTimerFragment extends Fragment {

    private static final int notification_id = 1;
    private static final String START_TIME = "start_time";
    private static final String BROADCAST_INTENT_FILTER = "com.armcomptech.akash.simpletimer4.timerAction";

    private TextView mTextViewCountDown;
    private FloatingActionButton mButtonStart;
    private FloatingActionButton mButtonPause;
    private FloatingActionButton mButtonReset;
    private ProgressBar mProgressBar;
    private Button mButtonSetTimer;
    private CountDownTimer mCountDownTimer;
    private TextView mMillis;
    private AutoCompleteTextView mTimerNameAutoComplete;
    private TextView mTimerNameTextView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mRepeatSwitch;

    private boolean mTimerRunning;
    private boolean BlinkTimerStopRequest;
    private boolean soundChecked;
    private boolean showNotification;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private int alternate;

    MediaPlayer player;
    private NotificationManagerCompat notificationManager;
    InterstitialAd mResetButtonInterstitialAd;

    ArrayList<String> timerName = new ArrayList<>();
    ArrayList<Integer> count = new ArrayList<>();
    ArrayList<Integer> timeInSeconds = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    private static TabbedActivity instance;
    public String currentTimerName;
    public int currentTimerNamePosition;
    public int ticksToPass;
    public int counter;

    private EditText editTextTimer;
    private io.github.deweyreed.scrollhmspicker.ScrollHmsPicker timePicker;

    private BroadcastReceiver broadcastReceiver;

    //TODO: Change disableFirebaseLogging to false when releasing
    public static Boolean disableFirebaseLogging = true;
    private static FirebaseAnalytics mFirebaseAnalytics;
    private boolean fragmentAttached;

    public static singleTimerFragment newInstance() {
        return new singleTimerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();
        instance = (TabbedActivity) requireContext();
        notificationManager = NotificationManagerCompat.from(requireContext());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String timerAction = intent.getStringExtra("timer_action");
                if (timerAction.equals("pause")) {
                    pauseTimer();
                }
            }
        };
        instance.registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_INTENT_FILTER));

        if (!disableFirebaseLogging) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App Opened");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        }

        if (timerName == null) {
            timerName = new ArrayList<>();
            count = new ArrayList<>();
            timeInSeconds = new ArrayList<>();
        }

        if (!isRemovedAds()) {
            //ad stuff
            //noinspection deprecation
            MobileAds.initialize(getContext(),getString(R.string.admob_app_id));

            //reset button ad
            mResetButtonInterstitialAd = new InterstitialAd(requireContext());
            mResetButtonInterstitialAd.setAdUnitId(getString(R.string.resetButton_interstital_ad_id));

            if (!disableFirebaseLogging) {
                mResetButtonInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_single_timer, container, false);

        mProgressBar = root.findViewById(R.id.progressBar);
        mTextViewCountDown = root.findViewById(R.id.text_view_countdown);
        mButtonStart = root.findViewById(R.id.button_start);
        mButtonPause = root.findViewById(R.id.button_pause);
        mButtonReset = root.findViewById(R.id.button_reset);
        mTimerNameAutoComplete = root.findViewById(R.id.timerNameAutoComplete);
        mTimerNameAutoComplete.setAdapter(new ArrayAdapter<>(
                getContext(), R.layout.timername_autocomplete_textview, timerName));
        mTimerNameAutoComplete.setThreshold(0);
        mTimerNameAutoComplete.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard();
                return true;
            }
            return false;
        });

        mMillis = root.findViewById(R.id.millis);
        mRepeatSwitch = root.findViewById(R.id.repeat_SwitchInMultiTimer);
        mRepeatSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> logFirebaseAnalyticsEvents("Repeat Switch: " + isChecked));

        mTimerNameTextView = root.findViewById(R.id.timerNameTextView);
        mTimerNameTextView.setVisibility(View.INVISIBLE );

        mButtonSetTimer = root.findViewById(R.id.setTimer);
        mButtonSetTimer.setBackgroundColor(Color.TRANSPARENT);

        mButtonSetTimer.setOnClickListener(v -> openTimerDialog());

        mButtonPause.hide();

        setTime(requireContext().getSharedPreferences("shared preferences", MODE_PRIVATE).getLong(START_TIME, 60000)); //default 1 minute timer

        mButtonStart.setOnClickListener( v -> {

            if (!disableFirebaseLogging) {
                Bundle bundle = new Bundle();
                bundle.putString("Event", "Start Timer");
                bundle.putString("Time", String.valueOf(mTimeLeftInMillis/1000));
                bundle.putString("Name", getTimerName());
                mFirebaseAnalytics.logEvent("Start_Timer", bundle);
            }

            mButtonStart.hide();
            mButtonPause.show();
            mButtonReset.hide();

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
                mTimerNameAutoComplete.setVisibility(View.INVISIBLE);
            }
        });

        mButtonPause.setOnClickListener( v -> {
            logFirebaseAnalyticsEvents("Timer Paused");

            mButtonPause.hide();
            mButtonStart.show();
            mButtonReset.show();

            pauseTimer();
        });

        mButtonReset.setOnClickListener(v -> {
            logFirebaseAnalyticsEvents("Reset Timer");

            mButtonStart.show();
            mButtonStart.hide();
            resetTimer();

            mTimerNameTextView.setVisibility(View.INVISIBLE);
            mTimerNameAutoComplete.setVisibility(View.VISIBLE);

            if (!isRemovedAds()) {
                if (mResetButtonInterstitialAd.isLoaded()) {
                    mResetButtonInterstitialAd.show();
                    logFirebaseAnalyticsEvents("Showed Ad");
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                    logFirebaseAnalyticsEvents("Ad not loaded");
                }
            }
        });
        showNotification = true;

        return root;
    }

    public static void logFirebaseAnalyticsEvents(String eventName) {
        if (!disableFirebaseLogging) {
            Bundle bundle = new Bundle();
            bundle.putString("Event", eventName);
            mFirebaseAnalytics.logEvent(eventName.replace(" ", "_"), bundle);
        }
    }

    public boolean isRemovedAds() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("removed_Ads", false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mTimerRunning", mTimerRunning);
        outState.putBoolean("BlinkTimerStopRequest", BlinkTimerStopRequest);
        outState.putBoolean("soundChecked", soundChecked);
        outState.putBoolean("showNotification", showNotification);

        outState.putLong("mStartTimeInMillis", mStartTimeInMillis);
        outState.putLong("mTimeLeftInMillis", mTimeLeftInMillis);

        outState.putInt("alternate", alternate);
        outState.putInt("currentTimerNamePosition", currentTimerNamePosition);
        outState.putInt("ticksToPass", ticksToPass);
        outState.putInt("counter", counter);

        outState.putString("currentTimerName", currentTimerName);
    }

    @Override
    public void onViewStateRestored(@NonNull Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

//        mTimerRunning = savedInstanceState.getBoolean("mTimerRunning");
//        BlinkTimerStopRequest = savedInstanceState.getBoolean("BlinkTimerStopRequest");
//        heartbeatChecked = savedInstanceState.getBoolean("heartbeatChecked");
//        soundChecked = savedInstanceState.getBoolean("soundChecked");
//        showNotification = savedInstanceState.getBoolean("showNotification");
//
//        mStartTimeInMillis = savedInstanceState.getLong("mStartTimeInMillis");
//        mTimeLeftInMillis = savedInstanceState.getLong("mTimeLeftInMillis");
//
//        alternate = savedInstanceState.getInt("alternate");
//        currentTimerNamePosition = savedInstanceState.getInt("currentTimerNamePosition");
//        ticksToPass = savedInstanceState.getInt("ticksToPass");
//        counter = savedInstanceState.getInt("counter");
//
//
//        currentTimerName = savedInstanceState.getString("currentTimerName");
//
//        if (mTimerRunning) {
//            mTimerNameAutoComplete.setText(currentTimerName);
//            mButtonStart.performClick();
//
//            //update interface to show timer name
//            mTimerNameTextView.setVisibility(View.VISIBLE);
//            mTimerNameTextView.setText(currentTimerName);
//            mTimerNameAutoComplete.setVisibility(View.INVISIBLE);
//        } else if (mTimeLeftInMillis != mStartTimeInMillis) {
//            //update interface to show timer name
//            mTimerNameTextView.setVisibility(View.VISIBLE);
//            mTimerNameTextView.setText(currentTimerName);
//            mTimerNameAutoComplete.setVisibility(View.INVISIBLE);
//            updateCountDownText();
//            updateWatchInterface();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mCountDownTimer.cancel();
        notificationManager.cancel(notification_id);
        stopPlayer();
        showNotification = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPlayer();
        showNotification = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        notificationManager.cancel(notification_id);
        showNotification = false;
    }

    @Override
    public void onPause() {
        super.onPause();
//        notificationManager.cancel(notification_id);
        stopPlayer();
        showNotification = true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.fragmentAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.fragmentAttached = false;
    }

    public static TabbedActivity getInstance() {
        return instance;
    }

    private String getTimerName() {
        String timerName = mTimerNameAutoComplete.getText().toString();
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
//        if (getContext().getSharedPreferences("shared preferences", MODE_PRIVATE) == null) {
//            return; //wait this might be something
//        }
        if (getContext() == null) {
            return;
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String timerNameJson = gson.toJson(timerName);
        editor.putString("timerName", timerNameJson);

        String countJson = gson.toJson(count);
        editor.putString("timesTimerRanCounter", countJson);

        String timeInSecondsJson = gson.toJson(timeInSeconds);
        editor.putString("timeInSecond", timeInSecondsJson);

        editor.apply();
    }

    public void timeUp() {
        //TODO: Wake up screen if off
        if (getContext() == null) {
            return;
        }
        Intent openMainActivity = new Intent(getContext(), TabbedActivity.class);
        openMainActivity.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        logFirebaseAnalyticsEvents("Time Up");
//        startActivityIfNeeded(openMainActivity, 0);

        soundChecked = soundChecked = instance.getSharedPreferences("shared preferences", MODE_PRIVATE).getBoolean("SOUND_CHECKED", true);

        if (soundChecked && this.getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            if (player != null) {
                stopPlayer();
            } else {
                player = MediaPlayer.create(getContext(), R.raw.endsong);
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

                        instance.runOnUiThread(() -> {
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
        if (/*heartbeatChecked*/false) {

            player = MediaPlayer.create(getContext(), R.raw.heartbeat);
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

    private void openTimerDialog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext());
        String timePickerPreference = sharedPreferences.getString("singleTimerTimePicker", "Typing");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_dialog_timerset, null);

        builder.setView(view)
                .setTitle("Once Timer Is Updated, It Will Reset")
                .setNegativeButton("Cancel", (dialog, which) -> {

                })
                .setPositiveButton("Set Timer", (dialog, which) -> {

                    if (timePickerPreference.equals("Typing")) {
                        String time = editTextTimer.getText().toString();
                        if (!(time.matches(""))) {
                            applyTimerTime(time, 0, 0, 0);
                        }
                    } else {
                        applyTimerTime("null", timePicker.getHours(), timePicker.getMinutes(), timePicker.getSeconds());
                    }
                });

        editTextTimer = view.findViewById(R.id.timer);
        timePicker = view.findViewById(R.id.scrollHmsPicker);
        if (timePickerPreference.equals("Typing")) {
            timePicker.setEnabled(false);
            timePicker.setVisibility(View.GONE);
            editTextTimer.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6)});

            editTextTimer.setOnFocusChangeListener((v, hasFocus) -> editTextTimer.post(() -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT);
            }));
            editTextTimer.requestFocus();
        } else {
            editTextTimer.setEnabled(false);
            editTextTimer.setVisibility(View.GONE);
            timePicker = view.findViewById(R.id.scrollHmsPicker);
        }

        builder.create().show();
    }

    public void applyTimerTime(String time, int hours, int minutes, int seconds){

        long millisInput;

        if (!time.equals("null")) {
            long input = Long.parseLong(time);
            long hour = input / 10000;
            long minuteRaw = (input - (hour * 10000)) ;
            long minuteOne = minuteRaw / 1000;
            long minuteTwo = (minuteRaw % 1000) / 100;
            long minute = (minuteOne * 10) + minuteTwo;
            long second = input - ((hour * 10000) + (minute * 100));
            long finalSecond = (hour * 3600) + (minute * 60) + second;

            if (time.length() == 0) {
                Toast.makeText(getContext(), "Field can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            millisInput = finalSecond * 1000;
            if (millisInput == 0) {
                Toast.makeText(getContext(), "Please enter a positive number", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (hours == 0 && minutes == 0 && seconds == 0){
                Toast.makeText(getContext(), "Time can't be zero", Toast.LENGTH_SHORT).show();
                return;
            } else {
                millisInput = (hours * 3600000) + (minutes * 60000) + (seconds * 1000);
            }
        }

        mButtonPause.performClick();
        mButtonReset.performClick();
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

                    if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                        try {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(instance.getApplicationContext(), notification);
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
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
        notificationManager.cancel(notification_id);
    }

    public String getTimeLeftFormatted() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        int screenSize;
        if (!this.fragmentAttached) {
            screenSize = Configuration.SCREENLAYOUT_SIZE_NORMAL;
        } else {
            screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        }

        String timeLeftFormatted;

        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);

            if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
                mTextViewCountDown.setTextSize(50);
                mMillis.setTextSize(17);
                if (hours > 9) {
                    mTextViewCountDown.setTextSize(45);
                    mMillis.setTextSize(20);
                }
            } else {
                mTextViewCountDown.setTextSize(60);
                mMillis.setTextSize(25);
                if (hours > 9) {
                    mTextViewCountDown.setTextSize(54);
                    mMillis.setTextSize(30);
                }
            }
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);

            if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
                mTextViewCountDown.setTextSize(55);
                mMillis.setTextSize(25);
            } else {
                mTextViewCountDown.setTextSize(70);
                mMillis.setTextSize(30);
            }
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
            showNotification(timeLeftFormatted, currentTimerName);
        } else {
            notificationManager.cancel(notification_id);
        }
    }

    private void updateWatchInterface() {
        if (fragmentAttached) {
            if (mTimerRunning) {
//            mButtonSetTimer.setVisibility(View.INVISIBLE);
                mButtonReset.hide();
                mButtonStart.hide();
                mButtonPause.show();
            } else {
//            mButtonSetTimer.setVisibility(View.VISIBLE);
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
    }

    private void closeKeyboard() {
        View view = instance.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) instance.getSystemService(INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();

        String timerNameJson = sharedPreferences.getString("timerName", null);
        Type timerNameType = new TypeToken<ArrayList<String>>(){}.getType();
        timerName = gson.fromJson(timerNameJson, timerNameType);

        String countJson = sharedPreferences.getString("timesTimerRanCounter", null);
        Type countType = new TypeToken<ArrayList<Integer>>(){}.getType();
        count = gson.fromJson(countJson, countType);

        String timeInSecondsJson = sharedPreferences.getString("timeInSecond", null);
        Type timeInSecondsType = new TypeToken<ArrayList<Integer>>(){}.getType();
        timeInSeconds = gson.fromJson(timeInSecondsJson, timeInSecondsType);
    }

    public void showNotification(String timeLeft, String currentTimerName) {

        PackageManager client = getContext().getPackageManager();
        final Intent notificationIntent = client.getLaunchIntentForPackage("com.armcomptech.akash.simpletimer4");

        final PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String content;
        if (currentTimerName.equals("General")) {
            content = "Timer: " + timeLeft;
        } else {
            content = "Timer: " + currentTimerName + " - " + timeLeft;
        }

        Notification notification = new NotificationCompat.Builder(getContext(), MAIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer_black)
                .setContentTitle(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setSound(null)
                .setFullScreenIntent(pendingIntent, false)
                .build();

        notificationManager.notify(1, notification);
    }
}