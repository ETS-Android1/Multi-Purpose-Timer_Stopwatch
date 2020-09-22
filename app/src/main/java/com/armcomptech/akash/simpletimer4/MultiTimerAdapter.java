package com.armcomptech.akash.simpletimer4;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

        int adapterPosition = holder.getAdapterPosition();
        int layoutPosition = holder.getLayoutPosition();
        int oldPosition = holder.getOldPosition();

        ((Item)holder).timerName.setText("Timer Name: " + timers.get(holder.getAdapterPosition()).timerName);
        ((Item)holder).timerTime.setText(timers.get(holder.getAdapterPosition()).getTimeLeftFormatted());
        if (timers.get(holder.getAdapterPosition()).timerPlaying) {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.VISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);
        } else if (timers.get(holder.getAdapterPosition()).timerPaused) {
            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);
        } else if (timers.get(holder.getAdapterPosition()).timerIsDone) {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);
        } else {
            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            ((Item) holder).timerTime.setTextColor(Color.BLACK);
            ((Item)holder).timerTime.setText(timers.get(holder.getAdapterPosition()).getTimeLeftFormatted());
            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(holder.getAdapterPosition()).mTimeLeftInMillis);
        }

        ((Item)holder).progressBarTimeHorizontal.setMax((int) timers.get(holder.getAdapterPosition()).mStartTimeInMillis);

        ((Item)holder).startButton.setOnClickListener(v -> {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.VISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            timers.get(holder.getAdapterPosition()).timerPlaying = true;
            timers.get(holder.getAdapterPosition()).timerPaused = false;
            timers.get(holder.getAdapterPosition()).timerIsDone = false;

            int beforeMyPosition = holder.getAdapterPosition();
            startTimer((Item) holder, holder.getAdapterPosition());
            int afterMyPosition = holder.getAdapterPosition();
        });

        ((Item)holder).pauseButton.setOnClickListener(v -> {
            int beforeMyPosition = holder.getAdapterPosition();
            pauseTimer(((Item)holder), holder.getAdapterPosition());
            int afterMyPosition = holder.getAdapterPosition();

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);

            timers.get(holder.getAdapterPosition()).timerPlaying = false;
            timers.get(holder.getAdapterPosition()).timerPaused = true;
            timers.get(holder.getAdapterPosition()).timerIsDone = false;
        });

        ((Item)holder).resetButton.setOnClickListener(v -> {
            int beforeMyPosition = holder.getAdapterPosition();
            resetTimer((Item) holder);
            int myPosition = holder.getAdapterPosition();

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            ((Item) holder).timerTime.setTextColor(Color.BLACK);
            ((Item)holder).timerTime.setText(timers.get(myPosition).getTimeLeftFormatted());

            timers.get(myPosition).timerPlaying = false;
            timers.get(myPosition).timerPaused = false;
            timers.get(myPosition).timerIsDone = true;
        });
    }

    private void resetTimer(@NonNull Item holder) {
        int myPosition = holder.getAdapterPosition();

        timers.get(myPosition).mTimeLeftInMillis = timers.get(myPosition).mStartTimeInMillis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mStartTimeInMillis, true);
        } else {
            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mStartTimeInMillis);
        }
        ((Item)holder).progressBarTimeHorizontal.setBackgroundColor(Color.WHITE);
        timers.get(myPosition).mCountDownTimer = null;
    }

    private void pauseTimer(@NonNull Item holder, int position) {
        int myPosition = holder.getAdapterPosition();
        timers.get(myPosition).mCountDownTimer.cancel();
        timers.get(myPosition).timerPlaying = false;
        timers.get(myPosition).timerPaused = true;
    }

    private void startTimer(@NonNull Item holder, int position) {
        counter = 0;
        ticksToPass = 1000 / 100;

//        notifyDataSetChanged(); //this causes holder.getAdapterPosition() to be -1

        if (timers.get(position).mCountDownTimer != null) {
            timers.get(position).mCountDownTimer.cancel();
        }
        timers.get(position).mCountDownTimer = new CountDownTimer(timers.get(position).mTimeLeftInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                //TODO: oveservation: position stats the same but position in viewHolder changes to 0 for top and works like index
                int myPosition = holder.getAdapterPosition();
                //TODo: x is responding indexwise thing but position is not and x is not same as position, so look into that

                //to prevent new timers from continuing from other timer threads
                if (myPosition != -1) {
                    if (!timers.get(myPosition).timerPlaying && !timers.get(myPosition).timerPaused && !timers.get(myPosition).timerIsDone) {
                        resetTimer((Item)holder);
                    } else {
                        timers.get(myPosition).mTimeLeftInMillis = millisUntilFinished;
                        ((Item)holder).timerTime.setText(timers.get(myPosition).getTimeLeftFormatted());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mTimeLeftInMillis, true);
                        } else {
                            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mTimeLeftInMillis);
                        }

                        counter++;
                        if (ticksToPass == counter) {
//                            holder.timerTime.setText(timers.get(position).getTimeLeftFormatted());
//                            saveData(); //saving data every second to prevent lag
                            counter = 0;
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                ((Item)holder).startButton.setVisibility(View.INVISIBLE);
                ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
                ((Item)holder).resetButton.setVisibility(View.VISIBLE);
                ((Item)holder).timerTime.setTextColor(Color.RED);
                ((Item)holder).progressBarTimeHorizontal.setBackgroundColor(Color.RED);
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
        ProgressBar progressBarTimeHorizontal;
        Item(@NonNull View itemView) {
            super(itemView);
            timerName = itemView.findViewById(R.id.timerNameInMultiTimer);
            timerTime = itemView.findViewById(R.id.timerTimeInMultiTimer);
            startButton = itemView.findViewById(R.id.startButtonInMultiTimer);
            pauseButton = itemView.findViewById(R.id.stopButtonInMultiTimer);
            resetButton = itemView.findViewById(R.id.resetButtonInMultiTimer);
            repeatSwitch = itemView.findViewById(R.id.repeat_Switch);
            progressBarTimeHorizontal = itemView.findViewById(R.id.progressBarTimeHorizontal);
        }
    }
}
