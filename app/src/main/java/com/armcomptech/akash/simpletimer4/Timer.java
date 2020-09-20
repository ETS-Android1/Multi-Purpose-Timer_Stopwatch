package com.armcomptech.akash.simpletimer4;

import android.os.CountDownTimer;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class Timer {
    String timerName;

    Boolean timerPlaying;
    Boolean timerPaused;
    Boolean timerIsDone;
    boolean showNotification;

    long mStartTimeInMillis;
    long mTimeLeftInMillis;
    CountDownTimer mCountDownTimer;
    RecyclerView.ViewHolder myHolder;

    Timer() {
        this.timerPlaying = false;
        this.timerPaused = true;
        this.showNotification = false;
        this.mStartTimeInMillis = 5000; // 1 minute and 40 seconds 100000
        this.mTimeLeftInMillis = 5000;
        this.myHolder = null;
    }

    public String getTimeLeftFormatted() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;

        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d", seconds);
        }

        return timeLeftFormatted;
    }

    public RecyclerView.ViewHolder getHolder() {
        return this.myHolder;
    }
}


