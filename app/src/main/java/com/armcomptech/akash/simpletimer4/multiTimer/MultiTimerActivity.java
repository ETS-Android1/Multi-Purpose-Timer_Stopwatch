package com.armcomptech.akash.simpletimer4.multiTimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.armcomptech.akash.simpletimer4.EmailLogic.SendMailTask;
import com.armcomptech.akash.simpletimer4.R;
import com.armcomptech.akash.simpletimer4.Settings.SettingsActivity;
import com.armcomptech.akash.simpletimer4.TabbedView.TabbedActivity;
import com.armcomptech.akash.simpletimer4.Timer;
import com.armcomptech.akash.simpletimer4.billing.BillingClientSetup;
import com.armcomptech.akash.simpletimer4.buildTimer.buildTimer_Activity;
import com.armcomptech.akash.simpletimer4.statistics.StatisticsActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.App.MULTI_TIMER_ID;

public class MultiTimerActivity extends AppCompatActivity implements setNameAndTimerDialog.setTimerDialogListener, PurchasesUpdatedListener {

    RecyclerView recyclerView;
    ExtendedFloatingActionButton addTimerFab;
    private final ArrayList<Timer> timers = new ArrayList<>();
    private final ArrayList<RecyclerView.ViewHolder> holders = new ArrayList<>();
    final static int GROUP_NOTIFICATION_ID = 1000;
    private FirebaseAnalytics mFirebaseAnalytics;

    private AdView banner_adView;
    AdRequest banner_adRequest;

    BillingClient billingClient;
    ConsumeResponseListener consumeResponseListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_timer);

        if (!isRemovedAds()) {
            banner_adView = findViewById(R.id.banner_ad);
            banner_adRequest = new AdRequest.Builder().build();
            banner_adView.loadAd(banner_adRequest);
            banner_adView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    banner_adView.setVisibility(View.VISIBLE);
                    logFirebaseAnalyticsEvents("Loaded banner ad");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    banner_adView.setVisibility(View.GONE);
                    logFirebaseAnalyticsEvents("Failed to load banner ad");
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                }
            });
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle("   Multi Timer");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_video_library_white);

        initializeBillingProcess();

        Timer tempTimer = new Timer(60 * 1000 , "one minute");
        timers.add(tempTimer);

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
                timers.get(viewHolder.getBindingAdapterPosition()).clean();
                timers.remove(viewHolder.getBindingAdapterPosition());
                holders.remove(viewHolder.getBindingAdapterPosition());
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRemoved(viewHolder.getBindingAdapterPosition());
                NotificationManagerCompat.from(getApplicationContext()).cancel(viewHolder.getBindingAdapterPosition() + 2); //cancel notification
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

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
        addTimerFab.setOnClickListener(v -> openNameAndTimerDialog());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addTimerFab.setTooltipText("Add Timer");
        }
    }

    public void initializeBillingProcess() {
        consumeResponseListener = (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                Log.d("Billing", "Consume OK");
            }
        };

        billingClient = BillingClientSetup.getInstance(this, this);
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "Success to connect billing");

                    // Query
                    List<Purchase> purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                            .getPurchasesList();
                    if (purchases != null) {
                        handleItemAlreadyPurchase(purchases);
                    } else {
                        Log.d("Billing", "Purchases is null");
                    }
                } else {
                    Log.d("Billing", "Error code: " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d("Billing", "Disconnected from billing service");
            }
        });
    }

    private void handleItemAlreadyPurchase(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            handlePurchase(purchase);
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals("remove_ads")) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.consumeAsync(consumeParams, consumeResponseListener);

            removeAds();
            Log.d("Billing", "Removed Ads");
            Toast.makeText(this, "Removed Ads", Toast.LENGTH_SHORT).show();
        }
    }

    private void purchaseProduct(String sku) {
        if (billingClient.isReady()) {
            // all the products to be purchased
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(Collections.singletonList("remove_ads"))
                    .setType(BillingClient.SkuType.INAPP)
                    .build();
            billingClient.querySkuDetailsAsync(params, (billingResult, skuDetailsList) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    SkuDetails tempSkuDetails = null;

                    assert skuDetailsList != null;
                    for (SkuDetails skuDetail : skuDetailsList) {
                        if (skuDetail.getSku().matches(sku)) {
                            tempSkuDetails = skuDetail;
                            break;
                        }
                    }

                    assert tempSkuDetails != null;
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(tempSkuDetails)
                            .build();
                    int response = billingClient.launchBillingFlow(MultiTimerActivity.this, billingFlowParams)
                            .getResponseCode();

                    switch (response) {
                        case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                            Log.d("Billing", "BILLING_UNAVAILABLE");
                            break;
                        case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                            Log.d("Billing", "DEVELOPER_ERROR");
                            break;
                        case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                            Log.d("Billing", "FEATURE_NOT_SUPPORTED");
                            break;
                        case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                            Log.d("Billing", "ITEM_ALREADY_OWNED");
                            break;
                        case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                            Log.d("Billing", "SERVICE_DISCONNECTED");
                            break;
                        case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                            Log.d("Billing", "SERVICE_TIMEOUT");
                            break;
                        case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                            Log.d("Billing", "ITEM_UNAVAILABLE");
                            break;
                        case BillingClient.BillingResponseCode.ERROR:
                            Log.d("Billing", "ERROR");
                            break;
                        case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                            Log.d("Billing", "ITEM_NOT_OWNED");
                            break;
                        case BillingClient.BillingResponseCode.OK:
                            Log.d("Billing", "OK");
                            break;
                        case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                            Log.d("Billing", "SERVICE_UNAVAILABLE");
                            break;
                        case BillingClient.BillingResponseCode.USER_CANCELED:
                            Log.d("Billing", "USER_CANCELED");
                            break;
                        default:
                            break;
                    }
                } else {
                    Log.d("Billing", "Error code: " + billingResult.getResponseCode());
                }
            });
        }
    }

    public void openNameAndTimerDialog() {
        setNameAndTimerDialog setNameAndTimerDialog = new setNameAndTimerDialog(false, true, null, timers);
        setNameAndTimerDialog.show(getSupportFragmentManager(), "Set Name and Timer Here");
    }

    public void createNewTimerNameAndTime(String time, int hours, int minutes, int seconds, String name, boolean creatingNewTimer, boolean updateExistingTimer, MultiTimerAdapter.Item holder, ArrayList<Timer> timers){
        long millisInput;
        long finalSecond;

        if (time.length() == 0) {
            Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!time.equals("null")) {
            long input = Long.parseLong(time);
            long hour = input / 10000;
            long minuteRaw = (input - (hour * 10000)) ;
            long minuteOne = minuteRaw / 1000;
            long minuteTwo = (minuteRaw % 1000) / 100;
            long minute = (minuteOne * 10) + minuteTwo;
            long second = input - ((hour * 10000) + (minute * 100));
            finalSecond = (hour * 3600) + (minute * 60) + second;

            millisInput = finalSecond * 1000;
            if (millisInput == 0) {
                Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (hours == 0 && minutes == 0 && seconds == 0){
                Toast.makeText(this, "Time can't be zero", Toast.LENGTH_SHORT).show();
                return;
            } else {
                millisInput = (hours * 3600000) + (minutes * 60000) + (seconds * 1000);
                finalSecond = millisInput/1000;
            }
        }

        if (creatingNewTimer) {
            timers.add(new Timer(finalSecond * 1000, name));
            Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(recyclerView.getAdapter().getItemCount() + 1);
        }
        if (updateExistingTimer) {
            timers.get(holder.getBindingAdapterPosition()).setStartTimeInMillis(finalSecond * 1000);
            timers.get(holder.getBindingAdapterPosition()).setTimeLeftInMillis(finalSecond * 1000);
            timers.get(holder.getBindingAdapterPosition()).setTimerPlaying(false);
            timers.get(holder.getBindingAdapterPosition()).setTimerPaused(false);
            timers.get(holder.getBindingAdapterPosition()).setTimerIsDone(false);
            if (timers.get(holder.getBindingAdapterPosition()).getCountDownTimer() != null) {
                timers.get(holder.getBindingAdapterPosition()).getCountDownTimer().cancel();
                timers.get(holder.getBindingAdapterPosition()).setCountDownTimer(null);
            }
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        menu.findItem(R.id.check_sound).setVisible(false);
        menu.findItem(R.id.multi_Timer_Mode).setVisible(false);
        menu.findItem(R.id.privacy_policy).setVisible(false);

        if (!isRemovedAds()) {
            menu.add(0, R.id.remove_Ads, 4, menuIconWithText(getResources().getDrawable(R.drawable.ic_baseline_remove_circle_outline_black), "Remove Ads"));
        }
        return true;
    }

    private CharSequence menuIconWithText(Drawable r, String title) {

        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        item.setChecked(!item.isChecked());

        switch (id) {
            case R.id.statistics_activity:
                logFirebaseAnalyticsEvents("Opened Statistics");
                startActivity(new Intent(this, StatisticsActivity.class));
                break;

            case R.id.timer_and_stopwatch:
                Intent intent = new Intent(this, TabbedActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("overrideActivityToOpen", true);
                startActivity(intent);
                break;

            case R.id.build_Timer_Mode:

                Intent intent2 = new Intent(this, buildTimer_Activity.class);
                intent2.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                break;

            case R.id.setting_activity:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.remove_Ads:
                purchaseProduct("remove_ads");
                break;

            case R.id.send_feedback:
                logFirebaseAnalyticsEvents("Opened Feedback");

                AlertDialog alert = new AlertDialog.Builder(this).create();

                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.feedback_layout, (ViewGroup) getCurrentFocus());

                Button cancelButton = dialoglayout.findViewById(R.id.cancel_feedback);
                Button sendButton = dialoglayout.findViewById(R.id.send_feedback);
                EditText editText = dialoglayout.findViewById(R.id.feedback_editText);

                alert.setView(dialoglayout);

                Activity activity = this;

                sendButton.setOnClickListener(view_ -> {
                    String feedback = String.valueOf(editText.getText());
                    String subject = "Feedback for Timer Application";
                    List<String> toEmail = Collections.singletonList(getString(R.string.toEmail));
                    new SendMailTask(activity).execute(getString(R.string.fromEmail), getString(R.string.fromPassword), toEmail, subject, feedback, new ArrayList<File>());
                    Toast.makeText(getApplicationContext(), "Feedback sent successfully", Toast.LENGTH_SHORT).show();
                    logFirebaseAnalyticsEvents("Sent Feedback");
                    alert.dismiss();
                });

                cancelButton.setOnClickListener(view_ -> {
                    Toast.makeText(getApplicationContext(), "Feedback was not sent", Toast.LENGTH_SHORT).show();
                    logFirebaseAnalyticsEvents("Cancelled Feedback");
                    alert.dismiss();
                });

                alert.show();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isRemovedAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("removed_Ads", false);
    }

    private void removeAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("removed_Ads", true);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyAllTimerAndNotification();
        NotificationManagerCompat.from(this).cancel(GROUP_NOTIFICATION_ID); //cancel group notification
    }

    private void destroyAllTimerAndNotification() {
        int count = 0;
        for (Timer timer: timers) {
            timer.setShowNotification(false);
            NotificationManagerCompat.from(this).cancel(count + 2); //cancel notification
            if (timer.getCountDownTimer() != null) {
                timer.getCountDownTimer().cancel();
            }
            count++;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Timer timer: timers) {
            timer.setShowNotification(true);
        }
        showGroupNotification();
    }

    private void showGroupNotification() {
        int count = 0;
        for (Timer timer: timers) {
            if (timer.getStartTimeInMillis() != timer.getTimeLeftInMillis()) {
                if (!timer.getTimerPaused()) {
                    count += 1;
                }
            }
        }
        if (count < 2) {
            return;
        }

        Intent notificationIntent = new Intent(MultiTimerActivity.class.getName());
        notificationIntent.setComponent(new ComponentName("com.armcomptech.akash.simpletimer4", "com.armcomptech.akash.simpletimer4.multiTimer.MultiTimerActivity"));

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification summaryNotification =
                new NotificationCompat.Builder(this, MULTI_TIMER_ID)
                        .setSmallIcon(R.drawable.ic_video_library_black)
                        .setGroup("multiTimer")
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .setFullScreenIntent(pendingIntent, true)
                        .setContentIntent(pendingIntent)
                        .build();
        NotificationManagerCompat.from(this).notify(GROUP_NOTIFICATION_ID, summaryNotification);
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Timer timer: timers) {
            timer.setShowNotification(false);
        }
        NotificationManagerCompat.from(this).cancel(GROUP_NOTIFICATION_ID); //cancel group notification
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (Timer timer: timers) {
            timer.setShowNotification(true);
        }
        showGroupNotification();
    }

    public void logFirebaseAnalyticsEvents(String eventName) {
        if (TabbedActivity.isInProduction) {
            eventName = eventName.replace(" ", "_");
            eventName = eventName.replace(":", "");

            Bundle bundle = new Bundle();
            bundle.putString("Event", eventName);
            mFirebaseAnalytics.logEvent(eventName, bundle);
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
    }
}