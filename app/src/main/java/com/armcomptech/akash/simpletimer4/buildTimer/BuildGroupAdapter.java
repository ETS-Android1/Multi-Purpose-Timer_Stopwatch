package com.armcomptech.akash.simpletimer4.buildTimer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.Timer;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

public class BuildGroupAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<ArrayList<Timer>> groups;
    private ArrayList<RecyclerView.ViewHolder> holders;
    private ArrayList<Integer> groupSetCounts;

    BuildGroupAdapter(Context context, ArrayList<ArrayList<Timer>> groups, ArrayList<Integer> groupSetCounts, ArrayList<RecyclerView.ViewHolder> holders) {
        this.context = context;
        this.groups = groups;
        this.holders = holders;
        this.groupSetCounts = groupSetCounts;
    }

    public static class Item extends RecyclerView.ViewHolder {
        EditText groupName;
        RecyclerView timerRecyclerView;
        FloatingActionButton subtractSetButton;
        FloatingActionButton addSetButton;
        ExtendedFloatingActionButton addTimerButton;
        TextView group_repeat_TextView;
        Item(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.editText_group_name_build_timer);
            timerRecyclerView = itemView.findViewById(R.id.build_timer_add_timer_recyclerView);
            subtractSetButton = itemView.findViewById(R.id.subtract_repeat_group);
            addSetButton = itemView.findViewById(R.id.add_repeat_group);
            addTimerButton = itemView.findViewById(R.id.addTimer_BuildTimer_FloatingActionButton);
            group_repeat_TextView = itemView.findViewById(R.id.group_repeat_textView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.build_group_recycler_view, parent, false);
        return new Item(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int myPosition = holder.getAdapterPosition();
        ArrayList<Timer> timers = groups.get(myPosition);

        ((Item)holder).timerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        ((Item)holder).timerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ((Item)holder).timerRecyclerView.setAdapter(new buildTimerAdapter(context, holders, groups.get(myPosition)));

        ((Item)holder).addTimerButton.setOnClickListener(v -> {
            groups.get(myPosition).add(new Timer(60000, "group"));
            Objects.requireNonNull(((Item)holder).timerRecyclerView.getAdapter()).notifyDataSetChanged();
            updateUI(holder, myPosition);
        });

        ((Item)holder).addSetButton.setOnClickListener(v -> {
            addGroupCount(myPosition);
            updateUI(holder, myPosition);

            if (groupSetCounts.get(myPosition) > 1) {
                ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_remove_white);
            }
        });

        ((Item)holder).subtractSetButton.setOnClickListener(v -> {
            if (groupSetCounts.get(myPosition) == 1) {
                removeGroup(myPosition);
            } else {
                subtractGroupCount(myPosition);
                updateUI(holder, myPosition);
                if (groupSetCounts.get(myPosition) == 1) {
                    ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_baseline_delete_24);
                }
            }
        });

        ((Item)holder).groupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI(holder, myPosition);
            }
        });

        updateUI(holder, myPosition);
    }

    private void subtractGroupCount(int myPosition) {
        groupSetCounts.set(myPosition, groupSetCounts.get(myPosition) - 1);
    }

    private void addGroupCount(int myPosition) {
        groupSetCounts.set(myPosition, groupSetCounts.get(myPosition) + 1);
    }

    private void updateUI(RecyclerView.ViewHolder holder, int myPosition) {
        ((Item)holder).groupName.setHint("Group: " + (myPosition + 1));

        if (groupSetCounts.get(myPosition) > 1) {
            ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_remove_white);
        } else {
            ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_baseline_delete_24);
        }

        if (groupSetCounts.get(myPosition) == 1) {
            ((Item)holder).group_repeat_TextView.setText("Once");
        } else if (groupSetCounts.get(myPosition) == 2) {
            ((Item)holder).group_repeat_TextView.setText("Twice");
        } else {
            ((Item)holder).group_repeat_TextView.setText(groupSetCounts.get(myPosition) + " times");
        }
    }

    private void removeGroup(int myPosition) {
        groupSetCounts.remove(myPosition);
        groups.remove(myPosition);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }
}
