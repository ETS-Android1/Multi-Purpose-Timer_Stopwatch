package com;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import static androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;

public class App extends Application {
    public static final String TIMER_STOPWATCH_ID = "timer_stopwatch_channel";
    public static final String MULTI_TIMER_ID = "multi_timer_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel timer_and_stopwatch_channel = new NotificationChannel(TIMER_STOPWATCH_ID, "Timer and Stopwatch", NotificationManager.IMPORTANCE_HIGH);
            timer_and_stopwatch_channel.setSound(null, null);
            timer_and_stopwatch_channel.setLockscreenVisibility(VISIBILITY_PUBLIC);
            timer_and_stopwatch_channel.setDescription("This is the channel for timer and stopwatch activity");

            NotificationChannel multi_timer_channel = new NotificationChannel(MULTI_TIMER_ID, "Multi-Timer", NotificationManager.IMPORTANCE_DEFAULT);
            multi_timer_channel.setSound(null, null);
            multi_timer_channel.setLockscreenVisibility(VISIBILITY_PUBLIC);
            multi_timer_channel.setDescription("This is the channel for timer and stopwatch activity");

            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(timer_and_stopwatch_channel);
            manager.createNotificationChannel(multi_timer_channel);
        }
    }

}
