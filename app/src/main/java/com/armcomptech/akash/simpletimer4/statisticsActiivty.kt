package com.armcomptech.akash.simpletimer4

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class statisticsActiivty : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    private var timerName = ArrayList<String>()
    private var count = ArrayList<Int>()
    private var timeInSeconds = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics_actiivty)
        //supportActionBar?.setIcon(R.drawable.ic_data_usage)
        supportActionBar?.title = "Statistics"

        loadData()

        recyclerView = findViewById(R.id.timersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = Adapter(this, timerName, count, timeInSeconds)
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("User Past Timer Info", Context.MODE_PRIVATE)
        val gson = Gson()

        val timerNameJson = sharedPreferences.getString("timer name", "")
        val timerNameType = object : TypeToken<ArrayList<String>>() {

        }.type
        timerName = gson.fromJson(timerNameJson, timerNameType)

        val countJson = sharedPreferences.getString("count", null)
        val countType = object : TypeToken<ArrayList<Int>>() {

        }.type
        count = gson.fromJson(countJson, countType)

        val timeInSecondsJson = sharedPreferences.getString("timeInSeconds", null)
        val timeInSecondsType = object : TypeToken<ArrayList<Int>>() {

        }.type
        timeInSeconds = gson.fromJson(timeInSecondsJson, timeInSecondsType)
    }
}
