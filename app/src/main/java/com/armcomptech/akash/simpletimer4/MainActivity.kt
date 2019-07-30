package com.armcomptech.akash.simpletimer4


//import com.App.CHANNEL_ID
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.App.Companion.CHANNEL_ID
import com.applovin.sdk.AppLovinSdk
import com.armcomptech.NotificationReceiver
import com.chartboost.sdk.Chartboost
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import java.util.*

class MainActivity : AppCompatActivity(), ExampleDialog.ExmapleDialogListner {

    private var mTextViewCountDown: TextView? = null
    private var mButtonStartPause: Button? = null
    private var mButtonReset: Button? = null
    private var mProgressBar: ProgressBar? = null
    private var mButtonSetTimer: Button? = null
    private var mCountDownTimer: CountDownTimer? = null
    private var mMillis: TextView? = null
    private var mTimerNameEditText: EditText? = null
    private var mTimerNameTextView: TextView? = null
    private var mRepeatSwitch: Switch? = null

    private var mTimerRunning: Boolean = false
    private var blinkTimerStopRequest: Boolean = false
    private var heartbeatChecked: Boolean = false
    private var soundChecked: Boolean = false

    private var mStartTimeInMillis: Long = 0
    private var mTimeLeftInMillis: Long = 0
    private var mEndTime: Long = 0
    private var alternate: Int = 0

    private var player: MediaPlayer? = null
    private var notificationManager: NotificationManagerCompat? = null
    private var mResetButtonInterstitialAd: InterstitialAd? = null
    private var mHappyButtonInterstitialAd: InterstitialAd? = null

    private var timerName: ArrayList<String>? = ArrayList()
    private var count = ArrayList<Int>()
    private var timeInSeconds = ArrayList<Int>()


    private lateinit var currentTimerName: String
    var currentTimerNamePosition: Int = 0
    var ticksToPass: Int = 0
    var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        notificationManager = NotificationManagerCompat.from(this)

        loadData() //load saved data when opening the app

        if (timerName == null) {
            timerName = ArrayList()
            count = ArrayList()
            timeInSeconds = ArrayList()
        }

        //ad stuff
        MobileAds.initialize(this, getString(R.string.admob_app_id))

        Chartboost.startWithAppId(this, "5d12507d18272d0bbe13eced", "e750c201ec23522c7ea3c688bb971ef68823ad5f")
        Chartboost.onCreate(this)

        val mResetButtonInterstitialAdMoPub = SdkConfiguration.Builder("7d26297661ba4a1784b331a6f3bde078").build()
        val mHappyButtonInterstitialAdMoPub = SdkConfiguration.Builder("a692a5880d0d48ce9463f1e8b4348a22").build()
        MoPub.initializeSdk(this, mResetButtonInterstitialAdMoPub, null)
        MoPub.initializeSdk(this, mHappyButtonInterstitialAdMoPub, null)

        AppLovinSdk.initializeSdk(this)

        //reset button ad
        mResetButtonInterstitialAd = InterstitialAd(this)
        mResetButtonInterstitialAd!!.adUnitId = getString(R.string.resetButton_interstital_ad_id)
        mResetButtonInterstitialAd!!.loadAd(AdRequest.Builder().build())
        //        mResetButtonInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("E5CC1736905A67B0077760DE2AFF519D").build());//test device

        //happy face ad
        mHappyButtonInterstitialAd = InterstitialAd(this)
        mHappyButtonInterstitialAd!!.adUnitId = getString(R.string.happyButton_interstital_ad_id)
        mHappyButtonInterstitialAd!!.loadAd(AdRequest.Builder().build())
        //        mHappyButtonInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("E5CC1736905A67B0077760DE2AFF519D").build());//test device

        mProgressBar = findViewById(R.id.progressBar)
        mTextViewCountDown = findViewById(R.id.text_view_countdown)
        mButtonStartPause = findViewById(R.id.button_start_pause)
        mButtonReset = findViewById(R.id.button_reset)
        mTimerNameEditText = findViewById(R.id.timerNameEditText)
        mMillis = findViewById(R.id.millis)
        mRepeatSwitch = findViewById(R.id.repeat_Switch)

        mTimerNameTextView = findViewById(R.id.timerNameTextView)
        mTimerNameTextView!!.visibility = View.INVISIBLE

        mButtonSetTimer = findViewById(R.id.setTimer)
        mButtonSetTimer!!.setBackgroundColor(Color.TRANSPARENT)

        mButtonSetTimer!!.setOnClickListener { openDialog() }

        mButtonStartPause!!.setOnClickListener {

            if (mTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
                counter = 0

                //only update during the start
                // mProgressBar.getProgress() ==
                if (mTimeLeftInMillis == mStartTimeInMillis) {
                    currentTimerName = getTimerName()

                    //get position of timer name and -1 if it doesn't exist
                    currentTimerNamePosition = timerNamePosition(currentTimerName, timerName)

                    if (currentTimerNamePosition == -1) {
                        timerName!!.add(currentTimerName)
                        count.add(1)
                        timeInSeconds.add(0)
                        currentTimerNamePosition = timeInSeconds.size - 1 //make a new position since adding new value which is at the end
                    } else {
                        //increment count
                        count[currentTimerNamePosition] = count[currentTimerNamePosition] + 1
                    }
                    saveData() //save data

                    //just to be safe because sometimes second is one less in statistics
                    if (mStartTimeInMillis >= 4000) { //when timer is set more than 4 seconds
                        timeInSeconds[currentTimerNamePosition] = timeInSeconds[currentTimerNamePosition] + 1
                    }

                    //update interface to show timer name
                    mTimerNameTextView!!.visibility = View.VISIBLE
                    mTimerNameTextView!!.text = currentTimerName
                    mTimerNameEditText!!.visibility = View.INVISIBLE
                }
            }
        }

        mButtonReset!!.setOnClickListener {
            resetTimer()

            mTimerNameTextView!!.visibility = View.INVISIBLE
            mTimerNameEditText!!.visibility = View.VISIBLE

            if (mResetButtonInterstitialAd!!.isLoaded) {
                mResetButtonInterstitialAd!!.show()
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.")
            }
        }
        heartbeatChecked = true
        soundChecked = true

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        resetTimer() //reset the timer when the app starts up
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        item.isChecked = !item.isChecked

        when (id) {
            R.id.check_heartbeat -> {
                heartbeatChecked = !heartbeatChecked

                //refresh the heartbeat sound
                if (mTimerRunning) {
                    mButtonStartPause!!.performClick()
                    mButtonStartPause!!.performClick()
                }
            }

            R.id.check_sound -> soundChecked = !soundChecked

            R.id.statistics_activity -> {
                if (timerName!!.size == 0){
                    Toast.makeText(applicationContext, "No data to show", Toast.LENGTH_LONG).show()
                } else {
                    startActivity(Intent(this, statisticsActiivty::class.java))
                }
            }

            R.id.privacy_policy -> {
                val myWebLink = Intent(Intent.ACTION_VIEW)
                myWebLink.data = Uri.parse("https://timerpolicy.blogspot.com/2019/06/privacy-policy-armcomptech-built.html")
                startActivity(myWebLink)
            }

            R.id.ad_button -> if (mHappyButtonInterstitialAd!!.isLoaded) {

                //pause the timer when the ad is opened
                if (mTimerRunning) {
                    pauseTimer()
                }

                mHappyButtonInterstitialAd!!.show()
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.")
            }

            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getTimerName(): String {
        var timerName = mTimerNameEditText!!.text.toString()
        if (timerName.matches("".toRegex())) {
            timerName = "General"
        }
        return timerName
    }

    private fun timerNamePosition(currentTimerName: String, timerName: ArrayList<String>?): Int {
        if (timerName == null) {
            return -1
        }

        for (i in timerName.indices) {
            if (timerName[i].matches(currentTimerName.toRegex())) {
                return i
            }
        }
        return -1
    }

    fun timeUp() {
        if (soundChecked) {
            if (player != null) {
                stopPlayer()
            } else {
                player = MediaPlayer.create(this, R.raw.endsong)
                player!!.setOnCompletionListener {
                    player!!.seekTo(0)
                    player!!.start()
                }
            }
            player!!.start()
        }

        alternate = 0
        blinkTimerStopRequest = false
        val blink = object : Thread() {
            override fun run() {
                while (!isInterrupted && !blinkTimerStopRequest) {
                    try {
                        sleep(400)

                        runOnUiThread {
                            if (alternate % 2 == 0) {
                                alternate++
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    mProgressBar!!.setProgress(0, false)
                                }
                            } else {
                                alternate++
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    mProgressBar!!.setProgress(mStartTimeInMillis.toInt(), false)
                                }
                            }
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        blink.start()
    }

    private fun heartbeat() {
        if (heartbeatChecked) {

            player = MediaPlayer.create(this, R.raw.heartbeat)
            player!!.setOnCompletionListener {
                player!!.seekTo(0)
                //player.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    player!!.playbackParams = player!!.playbackParams.setSpeed(java.lang.Float.parseFloat("1.0"))
                } else {
                    player!!.start()
                }
            }
            //player.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                player!!.playbackParams = player!!.playbackParams.setSpeed(java.lang.Float.parseFloat("1.0"))
            } else {
                player!!.start()
            }
        }
    }

    private fun setBlinkTimerStopRequest() {
        blinkTimerStopRequest = true
    }

    private fun stopPlayer() {
        if (player != null) {
            player!!.release()
            player = null
            //            Toast.makeText(this, "Song stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private fun openDialog() {
        ExampleDialog().show(supportFragmentManager, "Set Timer Here")
    }

    override fun applyText(time: String) {

        val input = java.lang.Long.parseLong(time)
        val hour = input / 10000
        val minuteraw = input - hour * 10000
        val minuteone = minuteraw / 1000
        val minutetwo = minuteraw % 1000 / 100
        val minute = minuteone * 10 + minutetwo
        val second = input - (hour * 10000 + minute * 100)
        val finalsecond = hour * 3600 + minute * 60 + second

        if (time.isEmpty()) {
            Toast.makeText(this@MainActivity, "Field can't be empty", Toast.LENGTH_SHORT).show()
            return
        }

        //long millisInput = Long.parseLong(time) * 1000;
        val millisInput = finalsecond * 1000
        if (millisInput == 0L) {
            Toast.makeText(this@MainActivity, "Please enter a positive number", Toast.LENGTH_SHORT).show()
            return
        }

        setTime(millisInput)
    }

    private fun setTime(milliseconds: Long) {
        mStartTimeInMillis = milliseconds
        resetTimer()
        //closeKeyboard()
    }

    private fun startTimer() {

        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis
        heartbeat()
        var countDownInterval = 100
        if (mStartTimeInMillis <= 30000) {
            countDownInterval = 50
        }

        ticksToPass = 1000 / countDownInterval

        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, countDownInterval.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                updateCountDownText()

                //basically increment by one every second
                counter++
                if (ticksToPass == counter) {
                    timeInSeconds[currentTimerNamePosition] = timeInSeconds[currentTimerNamePosition] + 1
                    saveData()
                    counter = 0
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                if (mRepeatSwitch!!.isChecked) {
                    resetTimer()
                    mTimerRunning = false

                    try {
                        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val r = RingtoneManager.getRingtone(applicationContext, notification)
                        r.play()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    mButtonStartPause!!.performClick()
                } else {
                    mTimerRunning = false
                    updateWatchInterface()
                    mTimeLeftInMillis = 0
                    mMillis!!.text = "000"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        mProgressBar!!.setProgress(0, true)
                    }
                    stopPlayer()
                    timeUp()
                }
            }
        }.start()

        mTimerRunning = true
        updateWatchInterface()
    }

    private fun pauseTimer() {
        mCountDownTimer!!.cancel()
        mTimerRunning = false
        updateWatchInterface()
        stopPlayer()
    }

    private fun resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis
        updateCountDownText()
        updateWatchInterface()
        mButtonStartPause!!.setBackgroundResource(R.drawable.playicon)
        setBlinkTimerStopRequest()
        stopPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar!!.setProgress(mStartTimeInMillis.toInt(), true)
        }
    }


    private fun updateCountDownText() {
        val hours = (mTimeLeftInMillis / 1000).toInt() / 3600
        val minutes = (mTimeLeftInMillis / 1000 % 3600).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

        val timeLeftFormatted: String

        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds)
            mTextViewCountDown!!.textSize = 60f
            mMillis!!.textSize = 25f
            if (hours > 9) {
                mTextViewCountDown!!.textSize = 54f
                mMillis!!.textSize = 30f
            }
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds)
            mTextViewCountDown!!.textSize = 70f
            mMillis!!.textSize = 30f
        }

        val millisFormatted: String = String.format(Locale.getDefault(), "%02d", mTimeLeftInMillis % 1000)


        mTextViewCountDown!!.text = timeLeftFormatted
        mMillis!!.text = millisFormatted
        mProgressBar!!.max = mStartTimeInMillis.toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar!!.setProgress(mTimeLeftInMillis.toInt(), true)
        }

        //showNotification(timeLeftFormatted);
    }

    private fun updateWatchInterface() {
        if (mTimerRunning) {
            mButtonSetTimer!!.visibility = View.INVISIBLE
            mButtonReset!!.visibility = View.INVISIBLE
            mButtonStartPause!!.setBackgroundResource(R.drawable.pauseicon6)
        } else {
            mButtonSetTimer!!.visibility = View.VISIBLE
            if (mCountDownTimer != null) {
                mButtonStartPause!!.setBackgroundResource(R.drawable.playicon)
            } else {
                mButtonStartPause!!.setBackgroundResource(R.drawable.playicon)
            }

            if (mTimeLeftInMillis < 100) {
                mButtonStartPause!!.visibility = View.INVISIBLE
            } else {
                mButtonStartPause!!.visibility = View.VISIBLE
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset!!.visibility = View.VISIBLE
            } else {
                mButtonReset!!.visibility = View.INVISIBLE
            }
        }
    }

//    private fun View.closeKeyboard() {
//        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(windowToken, 0)
//    }

    override fun onStop() {
        super.onStop()
        stopPlayer()

        val prefs = getSharedPreferences("currentTimer", 0)
        val editor = prefs.edit()

        editor.putLong("startTimeInMillis", mStartTimeInMillis)
        editor.putLong("millisLeft", mTimeLeftInMillis)
        editor.putBoolean("timerRunning", mTimerRunning)
        editor.putLong("endTime", mEndTime)

        editor.apply()

        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
    }

    override fun onStart() {
        super.onStart()

        val prefs = getSharedPreferences("currrentTimer", 0)

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000)
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis)
        mTimerRunning = prefs.getBoolean("timerRunning", false)

        updateCountDownText()
        updateWatchInterface()

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0)
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis()

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0
                mTimerRunning = false
                updateCountDownText()
                updateWatchInterface()
            } else {
                startTimer()
            }
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("User Past Timer Info", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        val timerNameJson = gson.toJson(timerName)
        editor.putString("timer name", timerNameJson)

        val countJson = gson.toJson(count)
        editor.putString("count", countJson)

        val timeInSecondsJson = gson.toJson(timeInSeconds)
        editor.putString("timeInSeconds", timeInSecondsJson)

        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("User Past Timer Info", Context.MODE_PRIVATE)
        val gson = Gson()

        val timerNameJson = sharedPreferences.getString("timer name", null)
        val timerNameType = object : TypeToken<ArrayList<String>>() {

        }.type
        timerName = gson.fromJson<ArrayList<String>>(timerNameJson, timerNameType)

        if (timerName == null) return //gives error otherwise

        val countJson = sharedPreferences.getString("count", null)
        val countType = object : TypeToken<ArrayList<Int>>() {

        }.type
        count = gson.fromJson(countJson, countType)

        val timeInSecondsJson = sharedPreferences.getString("timeInSeconds", null)
        val timeInSecondsType = object : TypeToken<ArrayList<Int>>() {

        }.type
        timeInSeconds = gson.fromJson(timeInSecondsJson, timeInSecondsType)
    }


    fun showNotification(timeLeft: String) {
        val collapsedView = RemoteViews(packageName, R.layout.notificaiton_collapsed_heartbeat)
        val expendedView = RemoteViews(packageName, R.layout.notification_expanded_heartbeat)

        val clickIntent = Intent(this, NotificationReceiver::class.java)
        val clickPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        expendedView.setOnClickPendingIntent(R.id.text_view_expanded, clickPendingIntent)

        collapsedView.setTextViewText(R.id.text_view_collapsed, timeLeft)
        expendedView.setTextViewText(R.id.text_view_expanded, timeLeft)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expendedView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .build()

        notificationManager!!.notify(1, notification)

        DataHolder.instance.setNotificationUp(true)
    }
}
