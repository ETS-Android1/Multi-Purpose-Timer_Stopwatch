package com.armcomptech.akash.simpletimer4.buildTimer;

import java.util.Locale;

public class BasicTimerInfo {
    String timerName;
    long mStartTimeInMillis;

    public BasicTimerInfo(long startTimeInMillis, String name) {
        this.mStartTimeInMillis = startTimeInMillis;
        this.timerName = name;
    }

    public String getTimeLeftFormatted() {
        int hours = (int) (mStartTimeInMillis / 1000) / 3600;
        int minutes = (int) ((mStartTimeInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mStartTimeInMillis / 1000) % 60;
        int millis = (int) mStartTimeInMillis % 1000;

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
}
