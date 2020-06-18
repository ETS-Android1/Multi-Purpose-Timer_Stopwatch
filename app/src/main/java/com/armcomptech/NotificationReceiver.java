package com.armcomptech;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.armcomptech.akash.simpletimer4.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("Pause");
        if (action.equals("Pause")){
            MainActivity.getInstance().pauseTimer();
        } else {
            MainActivity.getInstance().pauseTimer();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(1);
    }
}
