package com.armcomptech.akash.simpletimer4.singleTimer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.armcomptech.akash.simpletimer4.R;

import java.util.Locale;
import java.util.Objects;

import static com.App.MAIN_CHANNEL_ID;

public class timerWithService extends Service {

    CountDownTimer countDownTimer;

    long timeRemaining;
    String timerName;
    BroadcastReceiver broadcastReceiver2;

    public long getTimeRemaining() {
        return this.timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        timeRemaining = intent.getLongExtra("TimeValue", 0);
        timerName = intent.getStringExtra("timerName");

        startTimer(timeRemaining);
        IntentFilter intentFilter2 = new IntentFilter("timerPlayer");
        broadcastReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("player");
                switch (Objects.requireNonNull(action)) {
                    case "Start":
                        startTimer(timeRemaining);
                        break;

                    case "Pause":
                        pauseTimer();
                        break;

                    case "Reset":
                        resetTimer();
                        break;
                }
            }
        };

        registerReceiver(broadcastReceiver2, intentFilter2);

        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        cleanUp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    public void startTimer(long timeRemaining) {

        countDownTimer = new CountDownTimer(timeRemaining, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                Intent intent1local = new Intent("onTick");
                intent1local.putExtra("TimeRemaining", millisUntilFinished);
                sendBroadcast(intent1local);

                setTimeRemaining(millisUntilFinished);
                showNotification(getTimeLeftFormatted(), timerName);
            }

            @Override
            public void onFinish() {
                Intent intent1local = new Intent("onFinish");
                sendBroadcast(intent1local);
            }
        }.start();
    }

    public void pauseTimer() {
        stopSelf();
        countDownTimer.cancel();
        NotificationManagerCompat.from(this).cancel(1);
    }

    public void resetTimer() {
        stopSelf();
        countDownTimer.cancel();
        NotificationManagerCompat.from(this).cancel(1);
    }

    public void cleanUp() {
        unregisterReceiver(broadcastReceiver2);
        NotificationManagerCompat.from(this).cancel(1);
        stopSelf();
        countDownTimer.cancel();
    }

    public void showNotification(String timeLeft, String currentTimerName) {

        PackageManager client = this.getPackageManager();
        final Intent notificationIntent = client.getLaunchIntentForPackage("com.armcomptech.akash.simpletimer4");

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String content;
        if (currentTimerName.equals("General")) {
            content = "Timer: " + timeLeft;
        } else {
            content = "Timer: " + currentTimerName + " - " + timeLeft;
        }

        Notification notification = new NotificationCompat.Builder(this, MAIN_CHANNEL_ID)
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

        startForeground(1, notification);
    }

    public String getTimeLeftFormatted() {
        int hours = (int) (timeRemaining / 1000) / 3600;
        int minutes = (int) ((timeRemaining / 1000) % 3600) / 60;
        int seconds = (int) (timeRemaining / 1000) % 60;

        String timeLeftFormatted;

        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        return timeLeftFormatted;
    }
}
