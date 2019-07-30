package com.armcomptech.akash.simpletimer4

class DataHolder {
    //getter
    var notificationUp: Boolean = false
        private set

    //setter
    fun setNotificationUp(notificationUp: Boolean?) {
        this.notificationUp = notificationUp!!
    }

    companion object {

        val instance = DataHolder()
    }
}
