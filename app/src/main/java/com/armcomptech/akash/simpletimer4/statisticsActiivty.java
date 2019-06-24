package com.armcomptech.akash.simpletimer4;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

public class statisticsActiivty extends AppCompatActivity{
    RecyclerView recyclerView;

    ArrayList<String> timerName = new ArrayList<String>();
    ArrayList<Integer> count = new ArrayList<Integer>();
    ArrayList<Integer> timeInSeconds = new ArrayList<Integer>();

    //    String[] timerName = {"timerName"};
//    int[] count = {2, 1};
//    int[] timeInSeconds = {36001, 3661};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_actiivty);
        getSupportActionBar().setTitle("statistics");

        //testing
        loadData();

        recyclerView = findViewById(R.id.timersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter(this, timerName, count, timeInSeconds));
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();

        String timerNameJson = sharedPreferences.getString("timer name", null);
        Type timerNameType = new TypeToken<ArrayList<String>>() {}.getType();
        timerName = gson.fromJson(timerNameJson, timerNameType);

        String countJson = sharedPreferences.getString("count", null);
        Type countType = new TypeToken<ArrayList<Integer>>() {}.getType();
        count = gson.fromJson(countJson, countType);

        String timeInSecondsJson = sharedPreferences.getString("timeInSeconds", null);
        Type timeInSecondsType = new TypeToken<ArrayList<Integer>>() {}.getType();
        timeInSeconds = gson.fromJson(timeInSecondsJson, timeInSecondsType);
    }
}
