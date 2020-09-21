package com.armcomptech.akash.simpletimer4;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

public class MultiTimerActivity extends AppCompatActivity implements setNameAndTimerDialog.setTimerDialogListener{

    RecyclerView recyclerView;
    ExtendedFloatingActionButton addTimerFab;
    private ArrayList<Timer> timers = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> holders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_timer);

        createNewTimer();

        recyclerView = findViewById(R.id.multiTimerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new MultiTimerAdapter(this, timers, holders));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        addTimerFab = findViewById(R.id.addTimerFloatingActionButton);
        addTimerFab.setOnClickListener(v -> {
            openTimerDialog();
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addTimerFab.setTooltipText("Add Timer");
        }
    }

    public void createNewTimer() {
        timers.add(new Timer());
        timers.add(new Timer());
    }

    private void openTimerDialog() {
        setNameAndTimerDialog setNameAndTimerDialog = new setNameAndTimerDialog();
        setNameAndTimerDialog.show(getSupportFragmentManager(), "Set Name and Timer Here");
    }

    public void applyTimerNameAndTime(String time, String name){

        long input = Long.parseLong(time);
        long hour = input / 10000;
        long minuteraw = (input - (hour * 10000)) ;
        long minuteone = minuteraw / 1000;
        long minutetwo = (minuteraw % 1000) / 100;
        long minute = (minuteone * 10) + minutetwo;
        long second = input - ((hour * 10000) + (minute * 100));
        long finalsecond = (hour * 3600) + (minute * 60) + second;

        if (time.length() == 0) {
            Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        //long millisInput = Long.parseLong(time) * 1000;
        long millisInput = finalsecond * 1000;
        if (millisInput == 0) {
            Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
            return;
        }

        timers.add(new Timer(finalsecond * 1000, name));
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
    }
}