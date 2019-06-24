package com.armcomptech.akash.simpletimer4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.lang.*;
import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<String> timerName;
    private ArrayList<Integer> count;
    private ArrayList<Integer> timeInSeconds;

    Adapter(Context context, ArrayList<String> timerName, ArrayList<Integer> count, ArrayList<Integer> timeInSeconds) {
        this.context = context;
        this.timerName = timerName;
        this.count = count;
        this.timeInSeconds = timeInSeconds;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.statistics_recycler_vew, viewGroup, false);
        return new Item(row);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        //prints the name of the timer
        ((Item)viewHolder).textViewTimerName.setText("Timer Name: " + timerName.get(position));

        //prints how many times this particular timer was used
        if (count.get(position) == 1) {
            ((Item)viewHolder).textViewCount.setText("Used only once");
        } else {
            ((Item)viewHolder).textViewCount.setText("Used " + Integer.toString(count.get(position)) + " times");
        }

        //prints total time this timer was used
        String totalTimerTimeString = "";
        StringBuilder newString = new StringBuilder(totalTimerTimeString);
        int temp = (timeInSeconds.get(position));
        int second = temp % 60;
        int minute = (temp / 60) % 60;
        int hour = (temp / 60) / 60;

        //build the string
        while (true) {
            if (hour > 0) {
                if (hour == 1) {
                    newString.append(hour).append(" Hour ");
                } else {
                    newString.append(hour).append(" Hours ");
                }

                if ((minute == 0) || (minute == 1)) {
                    newString.append(minute).append(" Minute ");
                } else {
                    newString.append(minute).append(" Minutes ");
                }

                if ((second == 0) || (second == 1)) {
                    newString.append(second).append(" Second ");
                } else {
                    newString.append(second).append(" Seconds ");
                }
                break;
            }

            if (minute > 0) {
                if (minute == 1) {
                    newString.append(minute).append(" Minute ");
                } else {
                    newString.append(minute).append(" Minutes ");
                }

                if ((second == 0) || (second == 1)) {
                    newString.append(second).append(" Second ");
                } else {
                    newString.append(second).append(" Seconds ");
                }
                break;
            }

            if (second > 0) {
                if (second == 1) {
                    newString.append(second).append(" Second ");
                } else {
                    newString.append(second).append(" Seconds ");
                }
                break;
            }

            if (second == 0) {
                newString.append(second).append(" Second ");
                break;
            }
        }

        totalTimerTimeString = newString.toString();
        ((Item)viewHolder).textViewTimeInSeconds.setText("Total time: " + totalTimerTimeString);
    }

    @Override
    public int getItemCount() {
        if (timerName == null) {
            return 0;
        }
        return timerName.size();
    }

    public class Item extends RecyclerView.ViewHolder {
        TextView textViewTimerName;
        TextView textViewCount;
        TextView textViewTimeInSeconds;
        Item(@NonNull View itemView) {
            super(itemView);
            textViewTimerName = itemView.findViewById(R.id.timerName);
            textViewCount = itemView.findViewById(R.id.count);
            textViewTimeInSeconds = itemView.findViewById(R.id.timeInSeconds);
        }
    }
}
