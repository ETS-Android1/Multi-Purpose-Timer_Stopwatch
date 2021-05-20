package com.armcomptech.akash.simpletimer4.statistics;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class StatisticsActivity extends AppCompatActivity{
    RecyclerView recyclerView;

    ArrayList<String> timerName = new ArrayList<>();
    ArrayList<Integer> count = new ArrayList<>();
    ArrayList<Integer> timeInSeconds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setThemeForApp(sharedPreferences);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_activity);
        Objects.requireNonNull(getSupportActionBar()).setTitle("   Statistics");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_data_usage_white);

        loadData();

        recyclerView = findViewById(R.id.timersStatisticRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new StatisticsAdapter(this, timerName, count, timeInSeconds));
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();

        String timerNameJson = sharedPreferences.getString("timerName", null);
        Type timerNameType = new TypeToken<ArrayList<String>>() {}.getType();
        timerName = gson.fromJson(timerNameJson, timerNameType);

        String countJson = sharedPreferences.getString("timesTimerRanCounter", null);
        Type countType = new TypeToken<ArrayList<Integer>>() {}.getType();
        count = gson.fromJson(countJson, countType);

        String timeInSecondsJson = sharedPreferences.getString("timeInSecond", null);
        Type timeInSecondsType = new TypeToken<ArrayList<Integer>>() {}.getType();
        timeInSeconds = gson.fromJson(timeInSecondsJson, timeInSecondsType);
    }

    private void setThemeForApp(SharedPreferences sharedPreferences) {
        switch (Objects.requireNonNull(sharedPreferences.getString("theme", "Follow System Theme"))){
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Follow System Default":
                int currentNightMode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Night mode is not active, we're using the light theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Night mode is active, we're using dark theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }
                break;
        }
    }
}
