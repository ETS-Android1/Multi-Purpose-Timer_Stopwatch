package com.armcomptech.akash.simpletimer4.singleTimer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.preference.PreferenceManager;

import com.armcomptech.akash.simpletimer4.R;

public class setTimerDialog extends AppCompatDialogFragment {
    private EditText editTextTimer;
    private setTimerDialogListener listener;
    private io.github.deweyreed.scrollhmspicker.ScrollHmsPicker timePicker;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext());
        String timePickerPreference = sharedPreferences.getString("singleTimerTimePicker", "Typing");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_dialog_timerset, null);

        builder.setView(view)
                .setTitle("Once Timer Is Updated, It Will Reset")
                .setNegativeButton("Cancel", (dialog, which) -> {

                })
                .setPositiveButton("Set Timer", (dialog, which) -> {

                    if (timePickerPreference.equals("Typing")) {
                        String time = editTextTimer.getText().toString();
                        if (!(time.matches(""))) {
                            listener.applyTimerTime(time, 0, 0, 0);
                        }
                    } else {
                        listener.applyTimerTime("null", timePicker.getHours(), timePicker.getMinutes(), timePicker.getSeconds());
                    }
                });

        editTextTimer = view.findViewById(R.id.timer);
        timePicker = view.findViewById(R.id.scrollHmsPicker);
        if (timePickerPreference.equals("Typing")) {
            timePicker.setEnabled(false);
            timePicker.setVisibility(View.GONE);
            editTextTimer.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6)});

            editTextTimer.setOnFocusChangeListener((v, hasFocus) -> editTextTimer.post(() -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT);
            }));
            editTextTimer.requestFocus();
        } else {
            editTextTimer.setEnabled(false);
            editTextTimer.setVisibility(View.GONE);
            timePicker = view.findViewById(R.id.scrollHmsPicker);
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
        void applyTimerTime(String time, int hours, int minutes, int seconds);
    }
}
