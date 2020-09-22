package com.armcomptech.akash.simpletimer4;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MultiTimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements setNameAndTimerDialog.setTimerDialogListener{

    private Context context;
    private ArrayList<Timer> timers;
    private ArrayList<RecyclerView.ViewHolder> holders;
    private int counter = 0;
    private int ticksToPass = 0;

    MultiTimerAdapter(Context context, ArrayList<Timer> timers, ArrayList<RecyclerView.ViewHolder> holders) {
        this.context = context;
        this.timers = timers;
        this.holders = holders;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.multi_timer_recycler_view, parent, false);
        return new Item(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holders.size() != timers.size()) {
            holders.add(holder);
        }

        ((Item)holder).timerName.setText("Timer Name: " + timers.get(holder.getAdapterPosition()).timerName);
        ((Item)holder).timerTime.setText(timers.get(holder.getAdapterPosition()).getTimeLeftFormatted());
        if (timers.get(holder.getAdapterPosition()).timerPlaying) {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.VISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);
        } else if (timers.get(holder.getAdapterPosition()).timerPaused) {
            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);
        } else if (timers.get(holder.getAdapterPosition()).timerIsDone) {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);
        } else {
            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            ((Item) holder).timerTime.setTextColor(Color.BLACK);
            ((Item)holder).timerTime.setText(timers.get(holder.getAdapterPosition()).getTimeLeftFormatted());

            resetTimer((Item) holder);
        }

        ((Item)holder).progressBarTimeHorizontal.setMax((int) timers.get(holder.getAdapterPosition()).mStartTimeInMillis);

        ((Item)holder).startButton.setOnClickListener(v -> {
            ((Item)holder).startButton.setVisibility(View.INVISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.VISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            timers.get(holder.getAdapterPosition()).timerPlaying = true;
            timers.get(holder.getAdapterPosition()).timerPaused = false;
            timers.get(holder.getAdapterPosition()).timerIsDone = false;

            startTimer((Item) holder, holder.getAdapterPosition());
        });

        ((Item)holder).pauseButton.setOnClickListener(v -> {
            pauseTimer(((Item)holder), holder.getAdapterPosition());

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.VISIBLE);

            timers.get(holder.getAdapterPosition()).timerPlaying = false;
            timers.get(holder.getAdapterPosition()).timerPaused = true;
            timers.get(holder.getAdapterPosition()).timerIsDone = false;
        });

        ((Item)holder).resetButton.setOnClickListener(v -> {
            resetTimer((Item) holder);
            int myPosition = holder.getAdapterPosition();

            ((Item)holder).startButton.setVisibility(View.VISIBLE);
            ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
            ((Item)holder).resetButton.setVisibility(View.INVISIBLE);

            ((Item) holder).timerTime.setTextColor(Color.BLACK);
            ((Item)holder).timerTime.setText(timers.get(myPosition).getTimeLeftFormatted());

            timers.get(myPosition).timerPlaying = false;
            timers.get(myPosition).timerPaused = false;
            timers.get(myPosition).timerIsDone = true;
        });

        ((Item)holder).invisibleTimeButton.setBackgroundColor(Color.TRANSPARENT); //make button invisible
        ((Item)holder).invisibleTimeButton.setOnClickListener(v -> openNameAndTimerDialog((Item)holder));
    }

    private void resetTimer(@NonNull Item holder) {
        int myPosition = holder.getAdapterPosition();

        timers.get(myPosition).mTimeLeftInMillis = timers.get(myPosition).mStartTimeInMillis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mStartTimeInMillis, true);
        } else {
            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mStartTimeInMillis);
        }
        ((Item)holder).progressBarTimeHorizontal.setBackgroundColor(Color.WHITE);
        timers.get(myPosition).mCountDownTimer = null;
    }

    private void pauseTimer(@NonNull Item holder, int position) {
        int myPosition = holder.getAdapterPosition();
        timers.get(myPosition).mCountDownTimer.cancel();
        timers.get(myPosition).timerPlaying = false;
        timers.get(myPosition).timerPaused = true;
    }

    private void startTimer(@NonNull Item holder, int position) {
        counter = 0;
        ticksToPass = 1000 / 100;

        if (timers.get(position).mCountDownTimer != null) {
            timers.get(position).mCountDownTimer.cancel();
        }
        timers.get(position).mCountDownTimer = new CountDownTimer(timers.get(position).mTimeLeftInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int myPosition = holder.getAdapterPosition();

                //to prevent new timers from continuing from other timer threads
                if (myPosition != -1) {
                    if (!timers.get(myPosition).timerPlaying && !timers.get(myPosition).timerPaused && !timers.get(myPosition).timerIsDone) {
                        resetTimer((Item)holder);
                    } else {
                        timers.get(myPosition).mTimeLeftInMillis = millisUntilFinished;
                        ((Item)holder).timerTime.setText(timers.get(myPosition).getTimeLeftFormatted());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mTimeLeftInMillis, true);
                        } else {
                            ((Item)holder).progressBarTimeHorizontal.setProgress((int) timers.get(myPosition).mTimeLeftInMillis);
                        }

                        counter++;
                        if (ticksToPass == counter) {
//                            holder.timerTime.setText(timers.get(position).getTimeLeftFormatted());
//                            saveData(); //saving data every second to prevent lag
                            counter = 0;
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                ((Item)holder).startButton.setVisibility(View.INVISIBLE);
                ((Item)holder).pauseButton.setVisibility(View.INVISIBLE);
                ((Item)holder).resetButton.setVisibility(View.VISIBLE);
                ((Item)holder).timerTime.setTextColor(Color.RED);
                ((Item)holder).progressBarTimeHorizontal.setBackgroundColor(Color.RED);
            }
        }.start();
    }

    public void openNameAndTimerDialog(@NonNull Item holder) {
        setNameAndTimerDialog setNameAndTimerDialog = new setNameAndTimerDialog(true, false, holder, timers);
        setNameAndTimerDialog.show( ((AppCompatActivity) context).getSupportFragmentManager(), "Set Name and Timer Here");
    }

    //this method does nothing but satisfy the compiler
    public void createNewTimerNameAndTime(String time, String name, boolean creatingNewTimer, boolean updateExistingTimer, Item holder, ArrayList<Timer> timers){
        long input = Long.parseLong(time);
        long hour = input / 10000;
        long minuteraw = (input - (hour * 10000)) ;
        long minuteone = minuteraw / 1000;
        long minutetwo = (minuteraw % 1000) / 100;
        long minute = (minuteone * 10) + minutetwo;
        long second = input - ((hour * 10000) + (minute * 100));
        long finalsecond = (hour * 3600) + (minute * 60) + second;

        if (time.length() == 0) {
            Toast.makeText(this.context, "Field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        //long millisInput = Long.parseLong(time) * 1000;
        long millisInput = finalsecond * 1000;
        if (millisInput == 0) {
            Toast.makeText(this.context, "Please enter a positive number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (updateExistingTimer) {
            timers.get(holder.getAdapterPosition()).mStartTimeInMillis = finalsecond;
            resetTimer(holder);
        }
    }

    private void saveData(String timerName, int countersToAdd, int timeInSecondsToAdd) {
        //putting array in json
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String timerNameJsonToGet = sharedPreferences.getString("timer name", null);
        Type timerNameType = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> timerNameArray = gson.fromJson(timerNameJsonToGet, timerNameType);

        //timesTimerRanCounter

        //timesTimerRanCounter is new name and count was the past one so it has not data while other two things have data
        String timesTimerRanCounterJsonToGet = sharedPreferences.getString("timesTimerRanCounter", null);
        Type timesTimerRanCounterType = new TypeToken<ArrayList<Integer>>() {}.getType();
        ArrayList<Integer> timesTimerRanCounterArray = gson.fromJson(timesTimerRanCounterJsonToGet, timesTimerRanCounterType);

        String timeInSecondsJsonToGet = sharedPreferences.getString("timeInSeconds", null);
        Type timeInSecondsType = new TypeToken<ArrayList<Integer>>() {}.getType();
        ArrayList<Integer> timeInSecondsArray = gson.fromJson(timeInSecondsJsonToGet, timeInSecondsType);

        boolean timerNameExist = false;

        if (timerNameArray != null && timesTimerRanCounterArray != null && timeInSecondsArray != null) {
            if (timesTimerRanCounterArray.size() > 0) {
                for(int i = 0; i < timesTimerRanCounterArray.size(); i++) {
                    if (timerNameArray.get(i).matches(timerName)) {
                        timerNameExist = true;

                        String timesTimerRanCounterJsonToPut = gson.toJson(timesTimerRanCounterArray.get(i) + countersToAdd);
                        editor.putString("timesTimerRanCounter", timesTimerRanCounterJsonToPut);

                        String timeInSecondsJsonToPut = gson.toJson(timeInSecondsArray.get(i) + timeInSecondsToAdd);
                        editor.putString("timeInSeconds", timeInSecondsJsonToPut);

                        editor.apply();
                        break;
                    }
                }
            }

        }

        if (!timerNameExist) {
            if (timerNameArray == null) {
                timerNameArray = new ArrayList<>();
            } else {
                timerNameArray.add(timerName);
            }
            String timerNameJsonToPut = gson.toJson(timerNameArray);
            editor.putString("timer name", timerNameJsonToPut);

            if (timesTimerRanCounterArray == null) {
                timesTimerRanCounterArray = new ArrayList<>();
            } else {
                timesTimerRanCounterArray.add(countersToAdd);
            }

            String timesTimerRanCounterJsonToPut = gson.toJson(timesTimerRanCounterArray);
            editor.putString("timesTimerRanCounter", timesTimerRanCounterJsonToPut);

            if (timeInSecondsArray == null) {
                timeInSecondsArray = new ArrayList<>();
            } else {
                timeInSecondsArray.add(timeInSecondsToAdd);
            }
            String timeInSecondsJsonToPut = gson.toJson(timeInSecondsArray);
            editor.putString("timeInSeconds", timeInSecondsJsonToPut);

            editor.apply();
        }
    }

    @Override
    public int getItemCount() {
        if (timers == null) {
            return 0;
        }
        return timers.size();
    }

    public static class Item extends RecyclerView.ViewHolder {
        TextView timerName;
        TextView timerTime;
        FloatingActionButton startButton;
        FloatingActionButton pauseButton;
        FloatingActionButton resetButton;
        Switch repeatSwitch;
        ProgressBar progressBarTimeHorizontal;
        Button invisibleTimeButton;
        Item(@NonNull View itemView) {
            super(itemView);
            timerName = itemView.findViewById(R.id.timerNameInMultiTimer);
            timerTime = itemView.findViewById(R.id.timerTimeInMultiTimer);
            startButton = itemView.findViewById(R.id.startButtonInMultiTimer);
            pauseButton = itemView.findViewById(R.id.stopButtonInMultiTimer);
            resetButton = itemView.findViewById(R.id.resetButtonInMultiTimer);
            repeatSwitch = itemView.findViewById(R.id.repeat_SwitchInMultiTimer);
            progressBarTimeHorizontal = itemView.findViewById(R.id.progressBarTimeHorizontal);
            invisibleTimeButton = itemView.findViewById(R.id.timerTimeInvisibleButtonInMultiTimer);
        }
    }
}
