package com.armcomptech.akash.simpletimer4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class statisticsActiivty extends AppCompatActivity{
    RecyclerView recyclerView;
    String[] Items = {"timerName", "timerName2"};
    int[] count = {2, 1};
    int[] timeInSeconds = {36001, 3661};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_actiivty);

        getSupportActionBar().setTitle("Statistics");

        recyclerView = findViewById(R.id.timersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter(this, Items, count, timeInSeconds));
    }
}
