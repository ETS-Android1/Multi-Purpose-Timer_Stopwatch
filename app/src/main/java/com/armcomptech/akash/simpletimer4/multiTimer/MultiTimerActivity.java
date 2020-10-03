package com.armcomptech.akash.simpletimer4.multiTimer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.armcomptech.akash.simpletimer4.SettingsActivity;
import com.armcomptech.akash.simpletimer4.Timer;
import com.armcomptech.akash.simpletimer4.singleTimer.SingleTimerActivity;
import com.armcomptech.akash.simpletimer4.statistics.StatisticsActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.armcomptech.akash.simpletimer4.singleTimer.SingleTimerActivity.logFirebaseAnalyticsEvents;

public class MultiTimerActivity extends AppCompatActivity implements setNameAndTimerDialog.setTimerDialogListener {

    RecyclerView recyclerView;
    ExtendedFloatingActionButton addTimerFab;
    private ArrayList<Timer> timers = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> holders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_timer);
        setTitle("Multi Timer");

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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        menu.findItem(R.id.check_heartbeat).setVisible(false);
        menu.findItem(R.id.check_sound).setVisible(false);
        menu.add(0, R.id.single_Timer_Mode, 1, menuIconWithText(getResources().getDrawable(R.drawable.ic_timer), "Single Timer Mode"));
        menu.add(0, R.id.privacy_policy, 3, menuIconWithText(getResources().getDrawable(R.drawable.ic_lock_black), "Privacy Policy"));
        menu.add(0, R.id.statistics_activity, 2, menuIconWithText(getResources().getDrawable(R.drawable.ic_data_usage_black), "Statistics"));
        menu.add(0, R.id.setting_activity, 3, menuIconWithText(getResources().getDrawable(R.drawable.ic_settings_black), "Settings"));

        return true;
    }

    private CharSequence menuIconWithText(Drawable r, String title) {

        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }

        switch (id) {
            case R.id.statistics_activity:
                logFirebaseAnalyticsEvents("Opened Statistics");
                startActivity(new Intent(this, StatisticsActivity.class));
                break;

            case R.id.privacy_policy:
                logFirebaseAnalyticsEvents("Opened Privacy Policy");
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://timerpolicy.blogspot.com/2019/06/privacy-policy-armcomptech-built.html"));
                startActivity(myWebLink);
                break;

            case R.id.single_Timer_Mode:
                Intent intent = new Intent(this, SingleTimerActivity.class);
                intent.putExtra("overrideActivityToOpen", true);
                startActivity(intent);
                break;

            case R.id.setting_activity:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}