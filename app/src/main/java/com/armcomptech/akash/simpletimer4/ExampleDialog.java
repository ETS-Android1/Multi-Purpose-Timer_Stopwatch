package com.armcomptech.akash.simpletimer4;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Objects;

public class ExampleDialog extends AppCompatDialogFragment {
    private EditText editTextTimer;
    private ExmapleDialogListner listner;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_dialog_timerset, null);

        builder.setView(view)
                .setTitle("Timer")
                .setNegativeButton("Cancel", (dialog, which) -> {

                })
                .setPositiveButton("Set Timer", (dialog, which) -> {
                    String time = editTextTimer.getText().toString();

                    if (!(time.matches(""))) {
                        listner.applyText(time);
                    }
                });

        editTextTimer = view.findViewById(R.id.timer);


        editTextTimer.setOnFocusChangeListener((v, hasFocus) -> editTextTimer.post(() -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editTextTimer, InputMethodManager.SHOW_IMPLICIT);
        }));
        editTextTimer.requestFocus();


        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listner = (ExmapleDialogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ExampleDialogListner");
        }
    }

    public interface ExmapleDialogListner{
        void applyText(String time);
    }
}
