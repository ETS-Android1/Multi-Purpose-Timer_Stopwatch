package com.armcomptech.akash.simpletimer4.multiTimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.preference.PreferenceManager;

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
    private io.github.deweyreed.scrollhmspicker.ScrollHmsPicker timePicker;

    private boolean updateExistingTimer;
    private boolean creatingNewTimer;
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

        SharedPreferences sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(this.requireContext());
        String timePickerPreference = sharedPreferencesSettings.getString("multiTimerTimePicker", "Scrolling Wheel");

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.layout_dialog_nameandtimerset, (ViewGroup) requireActivity().getCurrentFocus());

        alertDialog.setView(dialogLayout);

        TextView title = dialogLayout.findViewById(R.id.title);
        title.setText("Once Timer Is Updated, It Will Reset");

        Button updateTimerButton = dialogLayout.findViewById(R.id.update_timer);
        Button cancelButton = dialogLayout.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(view -> alertDialog.dismiss());

        editTextTimer = dialogLayout.findViewById(R.id.timerTimeDialog);
        autoCompleteTimerName = dialogLayout.findViewById(R.id.timerNameDialog);
        timePicker = dialogLayout.findViewById(R.id.scrollHmsPicker);

        editTextTimer.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6)});

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String timerNameJson = sharedPreferences.getString("timerName", null);
        Type timerNameType = new TypeToken<ArrayList<String>>(){}.getType();
        ArrayList<String> timerName = gson.fromJson(timerNameJson, timerNameType);

        if (timerName != null) {
            autoCompleteTimerName.setAdapter(new ArrayAdapter<>(
                    requireContext(), R.layout.timername_autocomplete_textview, timerName));
            autoCompleteTimerName.setThreshold(0);
        }

        autoCompleteTimerName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(autoCompleteTimerName.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        setNameAndTimerDialog activity = this;

        if (this.updateExistingTimer) {
            autoCompleteTimerName.setText(timers.get(holder.getBindingAdapterPosition()).getTimerName());
            autoCompleteTimerName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            if (timePickerPreference.equals("Typing")) {
                timePicker.setEnabled(false);
                timePicker.setVisibility(View.GONE);
                editTextTimer.setOnFocusChangeListener((v, hasFocus) -> editTextTimer.post(() -> {
                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT);
                    }
                }));
                editTextTimer.requestFocus();
            } else {
                editTextTimer.setEnabled(false);
                editTextTimer.setVisibility(View.GONE);
                timePicker = dialogLayout.findViewById(R.id.scrollHmsPicker);

                long startTime = timers.get(holder.getBindingAdapterPosition()).getStartTimeInMillis();
                int hours = (int) (startTime / 1000) / 3600;
                int minutes = (int) ((startTime / 1000) % 3600) / 60;
                int seconds = (int) (startTime / 1000) % 60;

                timePicker.setHours(hours);
                timePicker.setMinutes(minutes);
                timePicker.setSeconds(seconds);
            }

//            builder.setMessage("Once timer is updated, it will reset");
            updateTimerButton.setOnClickListener(view -> {
                {
                    String name;
                    if (!autoCompleteTimerName.getText().toString().matches("")) {
                        name = autoCompleteTimerName.getText().toString();
                    } else {
                        name = "General";
                    }

                    String time;
                    if (timePickerPreference.equals("Typing")) {
                        time = editTextTimer.getText().toString();
//                    if (!(time.matches(""))) {
//                        listener.createNewTimerNameAndTime(time, 0, 0, 0, name, this.creatingNewTimer, this.updateExistingTimer, this.holder, this.timers);
//                    }
                        listener.createNewTimerNameAndTime(time, 0, 0, 0, name, activity.creatingNewTimer, activity.updateExistingTimer, activity.holder, activity.timers);
                    } else {
                        listener.createNewTimerNameAndTime("null", timePicker.getHours(), timePicker.getMinutes(), timePicker.getSeconds(), name, activity.creatingNewTimer, activity.updateExistingTimer, activity.holder, activity.timers);
                    }
                    alertDialog.dismiss();
                }
            });
        } else if (this.creatingNewTimer) {
            autoCompleteTimerName.setOnFocusChangeListener((v, hasFocus) -> autoCompleteTimerName.post(() -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(autoCompleteTimerName, InputMethodManager.SHOW_IMPLICIT);
                }
            }));
            autoCompleteTimerName.requestFocus();

            if (timePickerPreference.equals("Typing")) {
                timePicker.setEnabled(false);
                timePicker.setVisibility(View.GONE);
            } else {
                editTextTimer.setEnabled(false);
                editTextTimer.setVisibility(View.GONE);
                timePicker = dialogLayout.findViewById(R.id.scrollHmsPicker);
            }

            updateTimerButton.setText("Set Timer");
            updateTimerButton.setOnClickListener(view -> {
                String name;
                if (!autoCompleteTimerName.getText().toString().matches("")) {
                    name = autoCompleteTimerName.getText().toString();
                } else {
                    name = "General";
                }

                String time;
                if (timePickerPreference.equals("Typing")) {
                    time = editTextTimer.getText().toString();
                    if (!(time.matches(""))) {
                        listener.createNewTimerNameAndTime(time, 0, 0, 0, name, activity.creatingNewTimer, activity.updateExistingTimer, activity.holder, activity.timers);
                    }
                } else {
                    listener.createNewTimerNameAndTime("null", timePicker.getHours(), timePicker.getMinutes(), timePicker.getSeconds(), name, activity.creatingNewTimer, activity.updateExistingTimer, activity.holder, activity.timers);
                }
                alertDialog.dismiss();
            });
        }

        return alertDialog;
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
        void createNewTimerNameAndTime(String time, int hours, int minutes, int seconds, String name, boolean creatingNewTimer, boolean updateExistingTimer, MultiTimerAdapter.Item holder, ArrayList<Timer> timers);
    }
}

