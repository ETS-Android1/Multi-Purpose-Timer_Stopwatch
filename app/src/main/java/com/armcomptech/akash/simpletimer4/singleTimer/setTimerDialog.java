package com.armcomptech.akash.simpletimer4.singleTimer;

import android.annotation.SuppressLint;
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

import com.armcomptech.akash.simpletimer4.R;

import java.util.Objects;

public class setTimerDialog extends AppCompatDialogFragment {
    private EditText editTextTimer;
    private setTimerDialogListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_dialog_timerset, null);

        builder.setView(view)
                .setTitle("Timer")
                .setNegativeButton("Cancel", (dialog, which) -> {

                })
                .setPositiveButton("Set Timer", (dialog, which) -> {
                    String time = editTextTimer.getText().toString();

                    if (!(time.matches(""))) {
                        listener.applyTimerTime(time);
                    }
                });

        editTextTimer = view.findViewById(R.id.timer);


        editTextTimer.setOnFocusChangeListener((v, hasFocus) -> editTextTimer.post(() -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT);
        }));
        editTextTimer.requestFocus();


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
        void applyTimerTime(String time);
    }
}
