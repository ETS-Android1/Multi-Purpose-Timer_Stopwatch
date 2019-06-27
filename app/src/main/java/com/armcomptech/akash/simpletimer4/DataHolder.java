package com.armcomptech.akash.simpletimer4;

public class DataHolder {
    private boolean notificationUp;

    //getter
    public boolean getNotificationUp() {return notificationUp;}

    //setter
    public void setNotificationUp(Boolean notificationUp) {this.notificationUp = notificationUp;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
