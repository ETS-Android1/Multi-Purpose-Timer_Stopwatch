package com.armcomptech.akash.simpletimer4;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MultiTimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private ArrayList<Timer> timers;
    private ArrayList<RecyclerView.ViewHolder> holders;
    private int counter = 0;
    private int ticksToPass = 0;

    MultiTimerAdapter(Context context, ArrayList<Timer> timers, ArrayList<RecyclerView.ViewHolder> holders) {
        this.context = context;
        this.timers = timers;
        this.holders = holders;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.multi_timer_recycler_view, parent, false);
        return new Item(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holders.size() != timers.size()) {
            holders.add(holder);
        }

        ((Item)holder).timerName.setText("Timer Name: " + timers.get(position).timerName);
        ((Item)holder).timerTime.setText(timers.get(position).getTimeLeftFormatted());
        ((Item)holder).startButton.setVisibility(View.VISIBLE);
        ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
        ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

        ((Item)holder).startButton.setOnClickListener(v -> {
            startTimer((Item) holder, position);

            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.VISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            timers.get(position).timerPlaying = true;
            timers.get(position).timerPaused = false;
            timers.get(position).timerIsDone = false;
        });

        ((Item)holder).pauseButton.setOnClickListener(v -> {
            pauseTimer(position);

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);

            timers.get(position).timerPlaying = false;
            timers.get(position).timerPaused = true;
            timers.get(position).timerIsDone = false;
        });

        ((Item)holder).resetButton.setOnClickListener(v -> {
            resetTimer(position);

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            ((Item) holder).timerTime.setTextColor(Color.BLACK);
            ((Item)holder).timerTime.setText(timers.get(position).getTimeLeftFormatted());

            timers.get(position).timerPlaying = false;
            timers.get(position).timerPaused = false;
            timers.get(position).timerIsDone = true;
        });
    }

    private void resetTimer(int position) {
        timers.get(position).mTimeLeftInMillis = timers.get(position).mStartTimeInMillis;
    }

    private void pauseTimer(int position) {
        timers.get(position).mCountDownTimer.cancel();
        timers.get(position).timerPlaying = false;
        timers.get(position).timerPaused = true;
    }

    private void startTimer(@NonNull Item holder, int position) {
        counter = 0;
        ticksToPass = 1000 / 100;

//        timers.get(position).

        if (timers.get(position).mCountDownTimer != null) {
            timers.get(position).mCountDownTimer.cancel();
        }
        timers.get(position).mCountDownTimer = new CountDownTimer(timers.get(position).mTimeLeftInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timers.get(position).mTimeLeftInMillis = millisUntilFinished;

                ((Item)holder).timerTime.setText(timers.get(position).getTimeLeftFormatted());

                if (holders.size() != timers.size()) {
                    notifyDataSetChanged();
                }

                counter++;
                if (ticksToPass == counter) {
//                    holder.timerTime.setText(timers.get(position).getTimeLeftFormatted());
//                    saveData(); //saving data every second to prevent lag
                    counter = 0;
                }
            }

            @Override
            public void onFinish() {
                ((Item)holder).startButton.setVisibility(View.INVISIBLE);
                ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
                ((Item)holder).resetButton.setVisibility(View.VISIBLE);
                ((Item)holder).timerTime.setTextColor(Color.RED);
//                blink();
            }
        }.start();
    }

    @Override
    public int getItemCount() {
        if (timers == null) {
            return 0;
        }
        return timers.size();
    }

    public static class Item extends RecyclerView.ViewHolder {
        TextView timerName;
        TextView timerTime;
        FloatingActionButton startButton;
        FloatingActionButton pauseButton;
        FloatingActionButton resetButton;
        Switch repeatSwitch;
        Item(@NonNull View itemView) {
            super(itemView);
            timerName = itemView.findViewById(R.id.timerNameInMultiTimer);
            timerTime = itemView.findViewById(R.id.timerTimeInMultiTimer);
            startButton = itemView.findViewById(R.id.startButtonInMultiTimer);
            pauseButton = itemView.findViewById(R.id.stopButtonInMultiTimer);
            resetButton = itemView.findViewById(R.id.resetButtonInMultiTimer);
            repeatSwitch = itemView.findViewById(R.id.repeat_Switch);
        }
    }
}
