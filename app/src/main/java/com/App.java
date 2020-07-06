package com;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String MAIN_CHANNEL_ID = "mainChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mainChannel = new NotificationChannel(MAIN_CHANNEL_ID, "Main Channel", NotificationManager.IMPORTANCE_LOW);
            mainChannel.setDescription("This is the main Channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(mainChannel);
        }
    }

}
