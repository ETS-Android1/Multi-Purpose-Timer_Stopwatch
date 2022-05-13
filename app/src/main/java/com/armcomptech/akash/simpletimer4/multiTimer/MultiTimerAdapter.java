package com.armcomptech.akash.simpletimer4.multiTimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.TabbedView.TabbedActivity;
import com.armcomptech.akash.simpletimer4.Timer;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.App.MULTI_TIMER_ID;
import static com.armcomptech.akash.simpletimer4.multiTimer.MultiTimerActivity.clearFocusMultiTimer;
import static com.armcomptech.akash.simpletimer4.multiTimer.MultiTimerActivity.isFocusedMultiTimer;

public class MultiTimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements setNameAndTimerDialog.setTimerDialogListener {

    private FirebaseAnalytics mFirebaseAnalytics;
    private final Context context;
    private final ArrayList<Timer> timers;
    private final ArrayList<RecyclerView.ViewHolder> holders;
    private int ticksToPass = 0;

    private final NotificationManagerCompat notificationManager;
    InterstitialAd mResetButtonInterstitialAd;

    MultiTimerAdapter(Context context, ArrayList<Timer> timers, ArrayList<RecyclerView.ViewHolder> holders) {
        this.context = context;
        this.timers = timers;
        this.holders = holders;

        notificationManager = NotificationManagerCompat.from(this.context);

        if (TabbedActivity.isInProduction) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }

        if (TabbedActivity.isInProduction) {
            logFirebaseAnalyticsEvents("Reset Timer in Multi-Timer");
        }

        if (!isRemovedAds()) {
            MobileAds.initialize(context,
                    initializationStatus -> {

                    });

            //reset button ad
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(
                    context,
                    context.getString(R.string.resetButton_interstitial_ad_id),
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

    public void logFirebaseAnalyticsEvents(String eventName) {
        if (TabbedActivity.isInProduction) {
            eventName = eventName.replace(" ", "_");
            eventName = eventName.replace(":", "");

            Bundle bundle = new Bundle();
            bundle.putString("Event", eventName);
            mFirebaseAnalytics.logEvent(eventName, bundle);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        int myPosition = holder.getBindingAdapterPosition();
        if (myPosition != -1) {
            timers.get(myPosition).setShowNotification(true);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int myPosition = holder.getBindingAdapterPosition();
        if (myPosition != -1) {
            timers.get(myPosition).setShowNotification(false);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        for (int i = 0; i < timers.size(); i++) {
            cancelNotification(i+2);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.multi_timer_recycler_view, parent, false);
        return new Item(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holders.size() != timers.size()) {
            holders.add(holder);
        }

        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                ((Item)holder).timerTime.setTextColor(Color.WHITE);
                ((Item)holder).progressBarTimeHorizontal.setBackgroundColor(Color.BLACK);
                break;
        }

        ((Item)holder).timerName.setText(String.format("Timer Name: %s", timers.get(holder.getBindingAdapterPosition()).getTimerName()));
        ((Item)holder).timerTime.setText(timers.get(holder.getBindingAdapterPosition()).getTimeLeftFormatted());
        if (timers.get(holder.getBindingAdapterPosition()).getTimerPlaying()) {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.VISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);
            startTimer(((Item)holder), holder.getBindingAdapterPosition());
        } else if (timers.get(holder.getBindingAdapterPosition()).getTimerPaused()) {
            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);
            resetTimer(((Item)holder));
        } else if (timers.get(holder.getBindingAdapterPosition()).getTimerIsDone()) {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);
            resetTimer(((Item)holder));
        } else {
            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                ((Item) holder).timerTime.setTextColor(Color.BLACK);
            } else {
                ((Item) holder).timerTime.setTextColor(Color.WHITE);
            }

            ((Item)holder).timerTime.setText(timers.get(holder.getBindingAdapterPosition()).getTimeLeftFormatted());

            resetTimer((Item) holder);
        }

        ((Item)holder).progressBarTimeHorizontal.setMax((int) timers.get(holder.getBindingAdapterPosition()).getStartTimeInMillis());

        ((Item)holder).startButton.setOnClickListener(v -> {
            logFirebaseAnalyticsEvents("Start Timer in Multi-Timer");

            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.VISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            timers.get(holder.getBindingAdapterPosition()).setTimerPlaying(true);
            timers.get(holder.getBindingAdapterPosition()).setTimerPaused(false);
            timers.get(holder.getBindingAdapterPosition()).setTimerIsDone(false);

            startTimer((Item) holder, holder.getBindingAdapterPosition());
        });

        ((Item)holder).pauseButton.setOnClickListener(v -> {
            logFirebaseAnalyticsEvents("Pause Timer in Multi-Timer");

            pauseTimer(((Item)holder));

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);

            timers.get(holder.getBindingAdapterPosition()).setTimerPlaying(false);
            timers.get(holder.getBindingAdapterPosition()).setTimerPaused(true);
            timers.get(holder.getBindingAdapterPosition()).setTimerIsDone(false);
        });

        ((Item)holder).resetButton.setOnClickListener(v -> {
            logFirebaseAnalyticsEvents("Reset Timer in Multi-Timer");

            resetTimer((Item) holder);
            int myPosition = holder.getBindingAdapterPosition();
            cancelNotification(myPosition + 2);

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                ((Item) holder).timerTime.setTextColor(Color.BLACK);
            } else {
                ((Item) holder).timerTime.setTextColor(Color.WHITE);
            }

            ((Item)holder).timerTime.setText(timers.get(myPosition).getTimeLeftFormatted());

            timers.get(myPosition).setTimerPlaying(false);
            timers.get(myPosition).setTimerPaused(false);
            timers.get(myPosition).setTimerIsDone(true);


            if (!isRemovedAds()) {
                if (mResetButtonInterstitialAd != null) {
                    mResetButtonInterstitialAd.show((Activity) context);
                    logFirebaseAnalyticsEvents("Showed Ad");
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                    logFirebaseAnalyticsEvents("Ad not loaded");
                }
            }
        });

        ((Item)holder).invisibleTimeButton.setBackgroundColor(Color.TRANSPARENT); //make button invisible
        ((Item)holder).invisibleTimeButton.setOnClickListener(v -> {
            if (isFocusedMultiTimer()) {
                clearFocusMultiTimer();

                new Handler().postDelayed(() -> openNameAndTimerDialog((Item) holder), 1000);
            } else {
                openNameAndTimerDialog((Item) holder);
            }
        });
    }

    public boolean isRemovedAds() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("shared preferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("removed_Ads", false);
    }

    private void resetTimer(@NonNull Item holder) {
        int myPosition = holder.getBindingAdapterPosition();

        timers.get(myPosition).setTimeLeftInMillis(timers.get(myPosition).getStartTimeInMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).getStartTimeInMillis(), true);
        } else {
            holder.progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).getStartTimeInMillis());
        }

        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            holder.timerTime.setTextColor(Color.BLACK);
            holder.progressBarTimeHorizontal.setBackgroundColor(Color.WHITE);
        } else {
            holder.timerTime.setTextColor(Color.WHITE);
            holder.progressBarTimeHorizontal.setBackgroundColor(Color.BLACK);
        }

        timers.get(myPosition).setCountDownTimer(null);
        timers.get(myPosition).setTimeElapsedInMillis(0);
        timers.get(myPosition).setTimeToStoreInMillis(0);
        timers.get(myPosition).setCounter(0);
    }

    private void pauseTimer(@NonNull Item holder) {
        int myPosition = holder.getBindingAdapterPosition();
        timers.get(myPosition).getCountDownTimer().cancel();
        timers.get(myPosition).setTimerPlaying(false);
        timers.get(myPosition).setTimerPaused(true);
    }

    private void startTimer(@NonNull Item holder, int position) {
        if (!timers.get(holder.getBindingAdapterPosition()).getTimerPlaying() &&
                !timers.get(holder.getBindingAdapterPosition()).getTimerPaused() &&
                !timers.get(holder.getBindingAdapterPosition()).getTimerIsDone()) {
            saveData(timers.get(holder.getBindingAdapterPosition()).getTimerName(), 1, 0);
        }

        int countDownInterval = 100;
        ticksToPass = 1000 / countDownInterval;

        if (timers.get(position).getCountDownTimer() != null) {
            timers.get(position).getCountDownTimer().cancel();
        }
        timers.get(position).setCountDownTimer(new CountDownTimer(timers.get(position).getTimeLeftInMillis(), countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                int myPosition = holder.getBindingAdapterPosition();

                if (timers.get(myPosition).isShowNotification()) {
                    showNotification(timers.get(myPosition).getTimeLeftFormatted(), timers.get(myPosition).getTimerName(), myPosition + 2);
                } else {
                    cancelNotification(myPosition + 2);
                }

                //to prevent new timers from continuing from other timer threads
                if (myPosition != -1) {
                    if (!timers.get(myPosition).getTimerPlaying() && !timers.get(myPosition).getTimerPaused() && !timers.get(myPosition).getTimerIsDone()) {
                        resetTimer(holder);
                    } else {
                        timers.get(myPosition).setTimeLeftInMillis(millisUntilFinished);
                        holder.timerTime.setText(timers.get(myPosition).getTimeLeftFormatted());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            holder.progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).getTimeLeftInMillis(), true);
                        } else {
                            holder.progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).getTimeLeftInMillis());
                        }

                        Timer tempTimer = timers.get(myPosition);
                        tempTimer.setCounter(tempTimer.getCounter() + 1);
                        if (ticksToPass == tempTimer.getCounter()) {
                            //this algoritem still doesn't work prooperly but on right track, counter ofr each timer

                            long currentElapsedTime = tempTimer.getStartTimeInMillis() - tempTimer.getTimeLeftInMillis();
                            long oldElapsedTime = tempTimer.getTimeElapsedInMillis();
                            tempTimer.setTimeElapsedInMillis(currentElapsedTime);
                            long tempTime = (currentElapsedTime - oldElapsedTime) + tempTimer.getTimeToStoreInMillis();
                            saveData(timers.get(myPosition).getTimerName(), 0, (int)(tempTime/1000)); //saving data every second to prevent lag

                            tempTimer.setTimeToStoreInMillis(tempTime%1000);
                            tempTimer.setCounter(0);
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                if (holder.repeatSwitch.isChecked()) {
                    resetTimer(holder);
                    startTimer(holder, holder.getBindingAdapterPosition());
                } else {
                    holder.startButton.setVisibility(View.INVISIBLE);
                    holder.pauseButton.setVisibility(View.INVISIBLE);
                    holder.resetButton.setVisibility(View.VISIBLE);
                    holder.timerTime.setTextColor(Color.RED);
                    holder.progressBarTimeHorizontal.setBackgroundColor(Color.RED);
                }
            }
        }.start());
    }

    public void openNameAndTimerDialog(@NonNull Item holder) {
        setNameAndTimerDialog setNameAndTimerDialog = new setNameAndTimerDialog(true, false, holder, timers);
        setNameAndTimerDialog.show( ((AppCompatActivity) context).getSupportFragmentManager(), "Set Name and Timer Here");
    }

    //this method does nothing but satisfy the compiler
    public void createNewTimerNameAndTime(String time, int hours, int minutes, int seconds, String name, boolean creatingNewTimer, boolean updateExistingTimer, Item holder, ArrayList<Timer> timers){
        long millisInput;
        long finalSecond;

        if (!time.equals("null")) {
            long input = Long.parseLong(time);
            long hour = input / 10000;
            long minuteRaw = (input - (hour * 10000)) ;
            long minuteOne = minuteRaw / 1000;
            long minuteTwo = (minuteRaw % 1000) / 100;
            long minute = (minuteOne * 10) + minuteTwo;
            long second = input - ((hour * 10000) + (minute * 100));
            finalSecond = (hour * 3600) + (minute * 60) + second;

            if (time.length() == 0) {
                Toast.makeText(this.context, "Field can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            millisInput = finalSecond * 1000;
            if (millisInput == 0) {
                Toast.makeText(this.context, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (hours == 0 && minutes == 0 && seconds == 0){
                Toast.makeText(this.context, "Time can't be zero", Toast.LENGTH_SHORT).show();
                return;
            } else {
                millisInput = (hours * 3600000) + (minutes * 60000) + (seconds * 1000);
                finalSecond = millisInput/1000;
            }
        }

        if (updateExistingTimer) {
            timers.get(holder.getBindingAdapterPosition()).setStartTimeInMillis(finalSecond);
            resetTimer(holder);
            logFirebaseAnalyticsEvents("Update Existing Timer in Multi-Timer");
        }

        if (creatingNewTimer) {
            logFirebaseAnalyticsEvents("Creating new timer in Multi-Timer");
        }
    }

    private void saveData(String timerName, int countersToAdd, int timeInSecondsToAdd) {
        //putting array in json
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String timerNameJsonToGet = sharedPreferences.getString("timerName", null);
        Type timerNameType = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> timerNameArray = gson.fromJson(timerNameJsonToGet, timerNameType);

        //timesTimerRanCounter

        //timesTimerRanCounter is new name and count was the past one so it has not data while other two things have data
        String timesTimerRanCounterJsonToGet = sharedPreferences.getString("timesTimerRanCounter", null);
        Type timesTimerRanCounterType = new TypeToken<ArrayList<Integer>>() {}.getType();
        ArrayList<Integer> timesTimerRanCounterArray = gson.fromJson(timesTimerRanCounterJsonToGet, timesTimerRanCounterType);

        String timeInSecondsJsonToGet = sharedPreferences.getString("timeInSecond", null);
        Type timeInSecondsType = new TypeToken<ArrayList<Integer>>() {}.getType();
        ArrayList<Integer> timeInSecondsArray = gson.fromJson(timeInSecondsJsonToGet, timeInSecondsType);

        boolean timerNameExist = false;

        if (timerNameArray != null && timesTimerRanCounterArray != null && timeInSecondsArray != null) {
            if (timesTimerRanCounterArray.size() > 0) {
                for(int i = 0; i < timesTimerRanCounterArray.size(); i++) {
                    if (timerNameArray.get(i).matches(timerName)) {
                        timerNameExist = true;

                        //stuff gets added
                        timesTimerRanCounterArray.set(i, timesTimerRanCounterArray.get(i) + countersToAdd);
                        String timesTimerRanCounterJsonToPut = gson.toJson(timesTimerRanCounterArray);
                        editor.putString("timesTimerRanCounter", timesTimerRanCounterJsonToPut);

                        timeInSecondsArray.set(i, timeInSecondsArray.get(i) + timeInSecondsToAdd);
                        String timeInSecondsJsonToPut = gson.toJson(timeInSecondsArray);
                        editor.putString("timeInSecond", timeInSecondsJsonToPut);

                        editor.apply();
                        break;
                    }
                }
            }

        }

        if (!timerNameExist) {
            if (timerNameArray == null) {
                timerNameArray = new ArrayList<>();
            } else {
                timerNameArray.add(timerName);
            }
            String timerNameJsonToPut = gson.toJson(timerNameArray);
            editor.putString("timerName", timerNameJsonToPut);

            if (timesTimerRanCounterArray == null) {
                timesTimerRanCounterArray = new ArrayList<>();
            } else {
                timesTimerRanCounterArray.add(countersToAdd);
            }

            String timesTimerRanCounterJsonToPut = gson.toJson(timesTimerRanCounterArray);
            editor.putString("timesTimerRanCounter", timesTimerRanCounterJsonToPut);

            if (timeInSecondsArray == null) {
                timeInSecondsArray = new ArrayList<>();
            } else {
                timeInSecondsArray.add(timeInSecondsToAdd);
            }
            String timeInSecondsJsonToPut = gson.toJson(timeInSecondsArray);
            editor.putString("timeInSecond", timeInSecondsJsonToPut);

            editor.apply();
        }
    }

    @Override
    public int getItemCount() {
        if (timers == null) {
            return 0;
        }
        return timers.size();
    }

    public static class Item extends RecyclerView.ViewHolder {
        TextView timerName;
        TextView timerTime;
        FloatingActionButton startButton;
        FloatingActionButton pauseButton;
        FloatingActionButton resetButton;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch repeatSwitch;
        ProgressBar progressBarTimeHorizontal;
        Button invisibleTimeButton;
        Item(@NonNull View itemView) {
            super(itemView);
            timerName = itemView.findViewById(R.id.timerNameInMultiTimer);
            timerTime = itemView.findViewById(R.id.timerTimeInMultiTimer);
            startButton = itemView.findViewById(R.id.startButtonInMultiTimer);
            pauseButton = itemView.findViewById(R.id.stopButtonInMultiTimer);
            resetButton = itemView.findViewById(R.id.resetButtonInMultiTimer);
            repeatSwitch = itemView.findViewById(R.id.repeat_SwitchInMultiTimer);
            progressBarTimeHorizontal = itemView.findViewById(R.id.progressBarTimeHorizontal);
            invisibleTimeButton = itemView.findViewById(R.id.timerTimeInvisibleButtonInMultiTimer);
        }
    }

    public void cancelNotification(int notificationID) {
        notificationManager.cancel(notificationID);
    }

    public void showNotification(String timeLeft, String currentTimerName, int notificationID) {

        Intent notificationIntent = new Intent(MultiTimerActivity.class.getName());
        notificationIntent.setComponent(new ComponentName("com.armcomptech.akash.simpletimer4", "com.armcomptech.akash.simpletimer4.multiTimer.MultiTimerActivity"));

        final PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String content;
        if (currentTimerName.equals("General")) {
            content = "Timer: " + timeLeft;
        } else {
            content = "Timer: " + currentTimerName + " - " + timeLeft;
        }

        Notification notification = new NotificationCompat.Builder(this.context, MULTI_TIMER_ID)
                .setSmallIcon(R.drawable.ic_timer_black)
                .setContentTitle(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setSound(null)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setGroup("multiTimer")
                .build();

        notificationManager.notify(notificationID, notification);
    }
}
