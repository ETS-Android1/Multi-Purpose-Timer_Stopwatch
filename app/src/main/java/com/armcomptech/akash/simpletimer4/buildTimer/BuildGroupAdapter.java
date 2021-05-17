package com.armcomptech.akash.simpletimer4.buildTimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import static com.armcomptech.akash.simpletimer4.buildTimer.buildTimer_Activity.clearFocusBuildTimer1;
import static com.armcomptech.akash.simpletimer4.buildTimer.buildTimer_Activity.isFocusedBuildTimer1;

public class BuildGroupAdapter extends RecyclerView.Adapter {

    private final Context context;
    private static ArrayList<RecyclerView.ViewHolder> holders = null;
    private final MasterInfo masterInfo;

    BuildGroupAdapter(Context context, MasterInfo masterInfo, ArrayList<RecyclerView.ViewHolder> holders) {
        this.context = context;
        BuildGroupAdapter.holders = holders;
        this.masterInfo = masterInfo;
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
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        holders.remove(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holders.add(holder);

        int myPosition = holder.getBindingAdapterPosition();

        ((Item)holder).timerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        ((Item)holder).timerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ((Item)holder).timerRecyclerView.setAdapter(
                new BuildTimerAdapter(
                        context,
                        masterInfo.basicGroupInfoArrayList.get(myPosition).basicTimerInfoArrayList));

        ((Item)holder).addTimerButton.setOnClickListener(v -> {
            if (isFocusedBuildTimer1()) {
                clearFocusBuildTimer1();

                new Handler().postDelayed(() -> addTimer(holder, myPosition), 1000);
            } else {
                addTimer(holder, myPosition);
            }
        });

        ((Item)holder).addSetButton.setOnClickListener(v -> {
            addGroupCount(myPosition);
            updateUI(holder, myPosition);

            if (masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets > 1) {
                ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_remove_black);
            }
        });

        ((Item)holder).subtractSetButton.setOnClickListener(v -> {
            if (masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets == 1) {
                removeGroup(myPosition);
            } else {
                subtractGroupCount(myPosition);
                updateUI(holder, myPosition);
                if (masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets == 1) {
                    ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_baseline_delete_24);
                }
            }
        });

        ((Item)holder).groupName.setOnEditorActionListener((view, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                hideKeyboard(view);
                if (String.valueOf(((Item) holder).groupName.getText()).equals("")){
                    masterInfo.basicGroupInfoArrayList.get(myPosition).groupName = "Group: " + (myPosition + 1);
                } else {
                    masterInfo.basicGroupInfoArrayList.get(myPosition).groupName = String.valueOf(((Item) holder).groupName.getText());
                }
                ((Item)holder).groupName.clearFocus();
                return true;
            }
            return false;
        });

        updateUI(holder, myPosition);
    }

    private void addTimer(@NonNull RecyclerView.ViewHolder holder, int myPosition) {
        openNameAndTimerDialog((Item) holder);
        Objects.requireNonNull(((Item) holder).timerRecyclerView.getAdapter()).notifyDataSetChanged();
        updateUI(holder, myPosition);
    }

    public static boolean isFocusedBuildTimer2() {
        for (RecyclerView.ViewHolder holder : holders) {
            if (((Item)holder).groupName.isFocused()) {
                return true;
            }
        }
        return false;
    }

    public static void clearFocusBuildTimer2() {
        for (RecyclerView.ViewHolder holder : holders) {
            ((Item)holder).groupName.clearFocus();
        }
    }

    public void hideKeyboard(TextView view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void subtractGroupCount(int myPosition) {
        masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets--;
    }

    private void addGroupCount(int myPosition) {
        masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets++;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(RecyclerView.ViewHolder holder, int myPosition) {
        ((Item)holder).groupName.setHint("Group: " + (myPosition + 1));
        if (masterInfo.basicGroupInfoArrayList.get(myPosition).groupName.contains("Group: ")) {
            ((Item)holder).groupName.setText("");
            masterInfo.basicGroupInfoArrayList.get(myPosition).groupName = "Group: " + (myPosition + 1);
        } else {
            ((Item)holder).groupName.setText(masterInfo.basicGroupInfoArrayList.get(myPosition).groupName);
        }

        if (masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets > 1) {
            ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_remove_black);
        } else {
            ((Item)holder).subtractSetButton.setImageResource(R.drawable.ic_baseline_delete_24);
        }

        if (masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets == 1) {
            ((Item)holder).group_repeat_TextView.setText("Once");
        } else if (masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets == 2) {
            ((Item)holder).group_repeat_TextView.setText("Twice");
        } else {
            ((Item)holder).group_repeat_TextView.setText(masterInfo.basicGroupInfoArrayList.get(myPosition).repeatSets + " times");
        }
    }

    private void removeGroup(int myPosition) {
        masterInfo.basicGroupInfoArrayList.remove(myPosition);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return masterInfo.basicGroupInfoArrayList.size();
    }

    public void openNameAndTimerDialog(@NonNull Item holder) {
        setNameAndTimerDialogForBuildTimer setNameAndTimerDialogForBuildTimer = new setNameAndTimerDialogForBuildTimer(
                false,
                true,
                holder.getBindingAdapterPosition(),
                masterInfo.basicGroupInfoArrayList.get(holder.getBindingAdapterPosition()).basicTimerInfoArrayList);
        setNameAndTimerDialogForBuildTimer.show( ((AppCompatActivity) context).getSupportFragmentManager(), "Set Name and Timer Here");
    }
}
