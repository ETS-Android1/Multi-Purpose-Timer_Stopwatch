package com.armcomptech.akash.simpletimer4.buildTimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.TabbedView.TabbedActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

public class BuildTimerAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<RecyclerView.ViewHolder> holders;
    private ArrayList<BasicTimerInfo> timers;
    private FirebaseAnalytics mFirebaseAnalytics;

    public BuildTimerAdapter(Context context, ArrayList<RecyclerView.ViewHolder> holders, ArrayList<BasicTimerInfo> timers) {
        this.context = context;
        this.holders = holders;
        this.timers = timers;

        if (!TabbedActivity.disableFirebaseLogging) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
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
        return new BuildTimerAdapter.Item(row);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int myPosition = holder.getBindingAdapterPosition();

        ((Item)holder).edit_timer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNameAndTimerDialogForBuildTimer setNameAndTimerDialogForBuildTimer = new setNameAndTimerDialogForBuildTimer(
                        true,
                        false,
                        holder.getBindingAdapterPosition(),
                        timers);
                setNameAndTimerDialogForBuildTimer.show( ((AppCompatActivity) context).getSupportFragmentManager(), "Set Name and Timer Here");
                notifyDataSetChanged();
            }
        });

        ((Item)holder).delete_timer_button.setOnClickListener(v -> {
            try {
                timers.remove(myPosition);
                notifyItemRemoved(myPosition);
            } catch (IndexOutOfBoundsException e) {
                Bundle bundle = new Bundle();
                bundle.putString("Event", "Error");
                bundle.putString("Timer_Size", String.valueOf(timers.size()));
                bundle.putString("myPosition", String.valueOf(myPosition));
                bundle.putString("position", String.valueOf(position));
                bundle.putString("Location", "delete_timer_button");
                bundle.putString("Error_Type", "Index out of bound");

                mFirebaseAnalytics.logEvent("Error", bundle);
            }

        });

        String timerName = timers.get(myPosition).timerName;
        String timerTime = timers.get(myPosition).getTimeLeftFormatted();
        ((Item)holder).timer_info_textView.setText(timerName + " - " + timerTime);
    }

    @Override
    public int getItemCount() {
        return timers.size();
    }
}
