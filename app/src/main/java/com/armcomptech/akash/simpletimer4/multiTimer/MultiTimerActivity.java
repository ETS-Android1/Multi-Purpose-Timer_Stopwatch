package com.armcomptech.akash.simpletimer4.multiTimer;

import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.Timer;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MultiTimerActivity extends AppCompatActivity implements setNameAndTimerDialog.setTimerDialogListener {

    RecyclerView recyclerView;
    ExtendedFloatingActionButton addTimerFab;
    private ArrayList<Timer> timers = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> holders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_timer);

        timers.add(new Timer(60 * 1000 , "one minute"));
        timers.add(new Timer(120 * 1000 , "two minute"));
        timers.add(new Timer(180 * 1000 , "three minute"));
        timers.add(new Timer(240 * 1000 , "four minute"));

        recyclerView = findViewById(R.id.multiTimerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new MultiTimerAdapter(this, timers, holders));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //TODO: there is some bug that needs fixing crashing when somethign is removed
                timers.get(viewHolder.getAdapterPosition()).clean();
                timers.remove(viewHolder.getAdapterPosition());
                holders.remove(viewHolder.getAdapterPosition());
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.deleteRed))
                        .addActionIcon(R.drawable.ic_baseline_delete_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        addTimerFab = findViewById(R.id.addTimerFloatingActionButton);
        addTimerFab.setOnClickListener(v -> {
            openNameAndTimerDialog();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addTimerFab.setTooltipText("Add Timer");
        }
    }

    public void openNameAndTimerDialog() {
        setNameAndTimerDialog setNameAndTimerDialog = new setNameAndTimerDialog(false, true, null, timers);
        setNameAndTimerDialog.show(getSupportFragmentManager(), "Set Name and Timer Here");
    }

    public void createNewTimerNameAndTime(String time, String name, boolean creatingNewTimer, boolean updateExistingTimer, MultiTimerAdapter.Item holder, ArrayList<Timer> timers){
        long input = Long.parseLong(time);
        long hour = input / 10000;
        long minuteraw = (input - (hour * 10000)) ;
        long minuteone = minuteraw / 1000;
        long minutetwo = (minuteraw % 1000) / 100;
        long minute = (minuteone * 10) + minutetwo;
        long second = input - ((hour * 10000) + (minute * 100));
        long finalsecond = (hour * 3600) + (minute * 60) + second;

        if (time.length() == 0) {
            Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        //long millisInput = Long.parseLong(time) * 1000;
        long millisInput = finalsecond * 1000;
        if (millisInput == 0) {
            Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (creatingNewTimer) {
            timers.add(new Timer(finalsecond * 1000, name));
            Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(recyclerView.getAdapter().getItemCount() + 1);
        }
        if (updateExistingTimer) {
            timers.get(holder.getAdapterPosition()).setmStartTimeInMillis(finalsecond * 1000);
            timers.get(holder.getAdapterPosition()).setmTimeLeftInMillis(finalsecond * 1000);
            timers.get(holder.getAdapterPosition()).setTimerPlaying(false);
            timers.get(holder.getAdapterPosition()).setTimerPaused(false);
            timers.get(holder.getAdapterPosition()).setTimerIsDone(false);
            if (timers.get(holder.getAdapterPosition()).getmCountDownTimer() != null) {
                timers.get(holder.getAdapterPosition()).getmCountDownTimer().cancel();
                timers.get(holder.getAdapterPosition()).setmCountDownTimer(null);
            }
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        }
    }
}