package com.armcomptech.akash.simpletimer4;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Objects;

public class setNameAndTimerDialog extends AppCompatDialogFragment {
    private EditText editTextName;
    private EditText editTextTimer;
    private setTimerDialogListener listener;
    private boolean updateExistingTimer = false;
    private boolean creatingNewTimer = false;
    private MultiTimerAdapter.Item holder;
    private ArrayList<Timer> timers;

    public setNameAndTimerDialog(boolean updateExistingTimer, boolean creatingNewTimer, MultiTimerAdapter.Item holder, ArrayList<Timer> timers) {
        this.updateExistingTimer = updateExistingTimer;
        this.creatingNewTimer = creatingNewTimer;
        this.holder = holder;
        this.timers = timers;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_nameandtimerset, null);

        builder.setView(view)
                .setTitle("Timer")
                .setNegativeButton("Cancel", (dialog, which) -> {

                })
                .setPositiveButton("Set Timer", (dialog, which) -> {
                    String time = editTextTimer.getText().toString();
                    String name;
                    if (!editTextName.getText().toString().matches("")) {
                        name = editTextName.getText().toString();
                    } else {
                        name = "General";
                    }

                    if (!(time.matches(""))) {
                        listener.createNewTimerNameAndTime(time, name, this.creatingNewTimer, this.updateExistingTimer, this.holder, this.timers);
                    }
                });

        editTextTimer = view.findViewById(R.id.timerTimeDialog);
        editTextName = view.findViewById(R.id.timerNameDialog);

        editTextTimer.setOnFocusChangeListener((v, hasFocus) -> editTextTimer.post(() -> {
            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT);
        }));
        editTextTimer.requestFocus();

        if (this.updateExistingTimer) {
            editTextName.setText(timers.get(holder.getAdapterPosition()).timerName);

            builder.setMessage("Once timer is updated, it will reset");
            builder.setPositiveButton("Update Timer", (dialog, which) -> {
                String time = editTextTimer.getText().toString();
                String name;
                if (!editTextName.getText().toString().matches("")) {
                    name = editTextName.getText().toString();
                } else {
                    name = "General";
                }

                if (!(time.matches(""))) {
                    listener.createNewTimerNameAndTime(time, name, this.creatingNewTimer, this.updateExistingTimer, this.holder, this.timers);
                }
            });
        } else if (this.creatingNewTimer) {
            builder.setPositiveButton("Set Timer", (dialog, which) -> {
                String time = editTextTimer.getText().toString();
                String name;
                if (!editTextName.getText().toString().matches("")) {
                    name = editTextName.getText().toString();
                } else {
                    name = "General";
                }

                if (!(time.matches(""))) {
                    listener.createNewTimerNameAndTime(time, name, this.creatingNewTimer, this.updateExistingTimer, this.holder, this.timers);
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (setTimerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DialogListener");
        }
    }

    public interface setTimerDialogListener {
        void createNewTimerNameAndTime(String time, String name, boolean creatingNewTimer, boolean updateExistingTimer, MultiTimerAdapter.Item holder, ArrayList<Timer> timers);
    }
}

