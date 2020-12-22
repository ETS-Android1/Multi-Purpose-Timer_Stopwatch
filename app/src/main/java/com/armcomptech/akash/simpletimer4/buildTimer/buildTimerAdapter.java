package com.armcomptech.akash.simpletimer4.buildTimer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.Timer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class buildTimerAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<RecyclerView.ViewHolder> holders;
    private ArrayList<Timer> timers;

    public buildTimerAdapter(Context context, ArrayList<RecyclerView.ViewHolder> holders, ArrayList<Timer> timers) {
        this.context = context;
        this.holders = holders;
        this.timers = timers;
    }

    public static class Item extends RecyclerView.ViewHolder {
        FloatingActionButton edit_timer_button;
        FloatingActionButton delete_timer_button;
        TextView timer_info_textView;
        Item(@NonNull View itemView) {
            super(itemView);
            edit_timer_button = itemView.findViewById(R.id.edit_timer);
            delete_timer_button = itemView.findViewById(R.id.delete_timer);
            timer_info_textView = itemView.findViewById(R.id.timer_info_build_timer);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.build_timer_recycler_view, parent, false);
        return new buildTimerAdapter.Item(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int myPosition = holder.getAdapterPosition();

        ((Item)holder).edit_timer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ((Item)holder).delete_timer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timers.remove(myPosition);
                notifyItemRemoved(myPosition);
            }
        });

        String timerName = timers.get(myPosition).getTimerName();
        String timerTime = timers.get(myPosition).getTimeLeftFormatted();
        ((Item)holder).timer_info_textView.setText(timerName + " - " + timerTime);
    }

    @Override
    public int getItemCount() {
        return timers.size();
    }
}
