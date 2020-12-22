package com.armcomptech.akash.simpletimer4;

import android.os.CountDownTimer;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class Timer {

    String timerName;
    CountDownTimer countDownTimer;
    boolean mRepeat = false;
    boolean timerPlaying;
    boolean timerPaused;
    boolean timerIsDone;
    boolean showNotification;

    long mStartTimeInMillis;

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public Boolean getTimerPlaying() {
        return timerPlaying;
    }

    public void setTimerPlaying(Boolean timerPlaying) {
        this.timerPlaying = timerPlaying;
    }

    public Boolean getTimerPaused() {
        return timerPaused;
    }

    public void setTimerPaused(Boolean timerPaused) {
        this.timerPaused = timerPaused;
    }

    public Boolean getTimerIsDone() {
        return timerIsDone;
    }

    public void setTimerIsDone(Boolean timerIsDone) {
        this.timerIsDone = timerIsDone;
    }

    public boolean isShowNotification() {
        return showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    public long getStartTimeInMillis() {
        return mStartTimeInMillis;
    }

    public void setStartTimeInMillis(long mStartTimeInMillis) {
        this.mStartTimeInMillis = mStartTimeInMillis;
    }

    public long getTimeLeftInMillis() {
        return mTimeLeftInMillis;
    }

    public void setTimeLeftInMillis(long mTimeLeftInMillis) {
        this.mTimeLeftInMillis = mTimeLeftInMillis;
    }

    public long getTimeToStoreInMillis() {
        return mTimeToStoreInMillis;
    }

    public void setTimeToStoreInMillis(long mTimeToStoreInMillis) {
        this.mTimeToStoreInMillis = mTimeToStoreInMillis;
    }

    public long getTimeElapsedInMillis() {
        return mTimeElapsedInMillis;
    }

    public void setTimeElapsedInMillis(long mTimeElapsedInMillis) {
        this.mTimeElapsedInMillis = mTimeElapsedInMillis;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public CountDownTimer getCountDownTimer() {
        return mCountDownTimer;
    }

    public void setCountDownTimer(CountDownTimer mCountDownTimer) {
        this.mCountDownTimer = mCountDownTimer;
    }

    public boolean isRepeat() {
        return mRepeat;
    }

    public void setRepeat(boolean mRepeat) {
        this.mRepeat = mRepeat;
    }

    long mTimeLeftInMillis;
    long mTimeToStoreInMillis;
    long mTimeElapsedInMillis;

    int counter;
    CountDownTimer mCountDownTimer;
    RecyclerView.ViewHolder myHolder;

    public Timer(long startTimeInMillis, String name) {
        this.timerPlaying = false;
        this.timerPaused = false;
        this.timerIsDone = false;
        this.showNotification = false;
        this.mStartTimeInMillis = startTimeInMillis;
        this.mTimeLeftInMillis = startTimeInMillis;
        this.mTimeToStoreInMillis = 0;
        this.mTimeElapsedInMillis = 0;
        this.timerName = name;
        this.myHolder = null;
        this.counter = 0;
    }

    public String getTimeLeftFormatted() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        int millis = (int) mTimeLeftInMillis % 1000;

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

    public RecyclerView.ViewHolder getHolder() {
        return this.myHolder;
    }

    public void clean() {
        if (this.mCountDownTimer != null) {
            this.mCountDownTimer.cancel();
        }
        this.mCountDownTimer = null;
        this.timerPlaying = false;
        this.timerPaused = false;
        this.timerIsDone = false;
        this.showNotification = false;
        this.mStartTimeInMillis = 1000;
        this.mTimeLeftInMillis = 1000;
        this.timerName = null;
    }


}


