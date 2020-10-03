package com.armcomptech.akash.simpletimer4.multiTimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.Timer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class setNameAndTimerDialog extends AppCompatDialogFragment {
    private AutoCompleteTextView autoCompleteTimerName;
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
                    if (!autoCompleteTimerName.getText().toString().matches("")) {
                        name = autoCompleteTimerName.getText().toString();
                    } else {
                        name = "General";
                    }

                    if (!(time.matches(""))) {
                        listener.createNewTimerNameAndTime(time, name, this.creatingNewTimer, this.updateExistingTimer, this.holder, this.timers);
                    }
                });

        editTextTimer = view.findViewById(R.id.timerTimeDialog);
        autoCompleteTimerName = view.findViewById(R.id.timerNameDialog);
        editTextTimer.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6)});

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String timerNameJson = sharedPreferences.getString("timerName", null);
        Type timerNameType = new TypeToken<ArrayList<String>>(){}.getType();
        ArrayList<String> timerName = gson.fromJson(timerNameJson, timerNameType);

        if (timerName != null) {
            autoCompleteTimerName.setAdapter(new ArrayAdapter<String>(
                    requireContext(), R.layout.timername_autocomplete_textview, timerName));
            autoCompleteTimerName.setThreshold(0);
        }

        autoCompleteTimerName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(autoCompleteTimerName.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        if (this.updateExistingTimer) {
            autoCompleteTimerName.setEnabled(false);
            autoCompleteTimerName.setText(timers.get(holder.getAdapterPosition()).getTimerName());

            editTextTimer.setOnFocusChangeListener((v, hasFocus) -> editTextTimer.post(() -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT);
            }));
            editTextTimer.requestFocus();

            builder.setMessage("Once timer is updated, it will reset");
            builder.setPositiveButton("Update Timer", (dialog, which) -> {
                String time = editTextTimer.getText().toString();
                String name;
                if (!autoCompleteTimerName.getText().toString().matches("")) {
                    name = autoCompleteTimerName.getText().toString();
                } else {
                    name = "General";
                }

                if (!(time.matches(""))) {
                    listener.createNewTimerNameAndTime(time, name, this.creatingNewTimer, this.updateExistingTimer, this.holder, this.timers);
                }
            });
        } else if (this.creatingNewTimer) {
            autoCompleteTimerName.setOnFocusChangeListener((v, hasFocus) -> autoCompleteTimerName.post(() -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.showSoftInput(autoCompleteTimerName, InputMethodManager.SHOW_IMPLICIT);
            }));
            autoCompleteTimerName.requestFocus();

            builder.setPositiveButton("Set Timer", (dialog, which) -> {
                String time = editTextTimer.getText().toString();
                String name;
                if (!autoCompleteTimerName.getText().toString().matches("")) {
                    name = autoCompleteTimerName.getText().toString();
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

