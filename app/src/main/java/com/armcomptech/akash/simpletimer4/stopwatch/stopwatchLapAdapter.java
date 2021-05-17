package com.armcomptech.akash.simpletimer4.stopwatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;

import java.util.ArrayList;

public class stopwatchLapAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final ArrayList<String> lapTime;
    private final ArrayList<String> lapName;

    public stopwatchLapAdapter(Context context, ArrayList<String> lapTime, ArrayList<String> lapName) {
        this.context = context;
        this.lapTime = lapTime;
        this.lapName = lapName;
    }

    public static class Item extends RecyclerView.ViewHolder {
        EditText lapNameEditText;
        TextView lapTimeTextView;
        Item(@NonNull View itemView) {
            super(itemView);
            lapNameEditText = itemView.findViewById(R.id.stopwatchLapName);
            lapTimeTextView = itemView.findViewById(R.id.stopwatchLapTime);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.lap_listview_adapter, parent, false);
        return new stopwatchLapAdapter.Item(row);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int myPosition = holder.getBindingAdapterPosition();

        ((Item)holder).lapNameEditText.setHint("Lap: " + (myPosition + 1));
        if (lapName.get(myPosition).matches("Lap: " + (myPosition + 1))) {
            ((Item)holder).lapNameEditText.setText("");
            lapName.set(myPosition, "Lap: " + (myPosition + 1));
        } else {
            ((Item)holder).lapNameEditText.setText(lapName.get(myPosition));
        }

        ((Item)holder).lapNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lapName.set(holder.getBindingAdapterPosition(), String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((Item)holder).lapNameEditText.setOnEditorActionListener((view, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                hideKeyboard(view);
                ((Item) holder).lapNameEditText.clearFocus();
                return true;
            }
            return false;
        });

        ((Item)holder).lapTimeTextView.setText(lapTime.get(myPosition));
    }

    @Override
    public int getItemCount() {
        return lapTime.size();
    }

    public void hideKeyboard(TextView view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
