package com.armcomptech.akash.simpletimer4.TabbedView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

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
import com.armcomptech.akash.simpletimer4.billing.BillingClientSetup;
import com.armcomptech.akash.simpletimer4.buildTimer.buildTimer_Activity;
import com.armcomptech.akash.simpletimer4.multiTimer.MultiTimerActivity;
import com.armcomptech.akash.simpletimer4.singleTimer.timerWithService;
import com.armcomptech.akash.simpletimer4.statistics.StatisticsActivity;
import com.armcomptech.akash.simpletimer4.stopwatch.stopwatchWithService;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;
import static com.armcomptech.akash.simpletimer4.singleTimer.singleTimerFragment.clearFocusSingleTimer;
import static com.armcomptech.akash.simpletimer4.singleTimer.singleTimerFragment.isFocusedSingleTimer;
import static com.armcomptech.akash.simpletimer4.stopwatch.stopwatchFragment.clearFocusStopwatch;
import static com.armcomptech.akash.simpletimer4.stopwatch.stopwatchFragment.isFocusedStopwatchTimer;

public class TabbedActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    //TODO: Change FirebaseLogging to true when releasing
    public static Boolean isInProduction = false;

    public static Boolean alwaysShowAd = false;
    private static FirebaseAnalytics mFirebaseAnalytics;

    String activityToOpen;

    BillingClient billingClient;
    ConsumeResponseListener consumeResponseListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isInProduction) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App Opened");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        }


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (Objects.requireNonNull(sharedPreferences.getString("theme", "Follow System Theme"))){
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Follow System Default":
                int currentNightMode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Night mode is not active, we're using the light theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Night mode is active, we're using dark theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbled);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle("   Timer and Stopwatch");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_timer_white);
        }

        initializeBillingProcess();

        if (!isInProduction) {
            removeAds(); // this removes all ads for new users
        }

        if (alwaysShowAd) {
            alwaysShowAds();
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        boolean overrideActivityToOpen = getIntent().getBooleanExtra("overrideActivityToOpen", false);

        if (!overrideActivityToOpen) {

            activityToOpen = sharedPreferences.getString("firstOpenActivity", "Timer and Stopwatch");

            switch (Objects.requireNonNull(activityToOpen)) {
                case "Timer and Stopwatch":
                    //do nothing
                    break;
                case "Multi Timer":
                    Intent intent = new Intent(this , MultiTimerActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    break;
                case "Build Your Timer":
                    Intent intent2 = new Intent(this , buildTimer_Activity.class);
                    intent2.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent2);
                    break;
                case "Statistics":
                    startActivity(new Intent(this, StatisticsActivity.class));
                    break;
                default:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
            }
        }

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(isInProduction);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isInProduction);
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
                    int response = billingClient.launchBillingFlow(TabbedActivity.this, billingFlowParams)
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

    private void removeAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("removed_Ads", true);
        editor.apply();
    }

    private void alwaysShowAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("removed_Ads", false);
        editor.apply();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        menu.findItem(R.id.check_sound).setChecked(getSharedPreferences("shared preferences", MODE_PRIVATE).getBoolean("SOUND_CHECKED", true));
        menu.findItem(R.id.timer_and_stopwatch).setVisible(false);
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
            case R.id.check_sound:
                SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
                boolean previousValue = sharedPreferences.getBoolean("SOUND_CHECKED", true);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("SOUND_CHECKED", !previousValue);
                editor.apply();
                logFirebaseAnalyticsEvents("Sound Checked");
                break;

            case R.id.statistics_activity:
                logFirebaseAnalyticsEvents("Opened Statistics");
                startActivity(new Intent(this, StatisticsActivity.class));
                break;

            case R.id.multi_Timer_Mode:
                //destroy services
                stopService(new Intent(this, timerWithService.class));
                stopService(new Intent(this, stopwatchWithService.class));

                Intent intent = new Intent(this, MultiTimerActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case R.id.build_Timer_Mode:
                //destroy services
                stopService(new Intent(this, timerWithService.class));
                stopService(new Intent(this, stopwatchWithService.class));

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
                final View[] dialogLayout = new View[1];

                Activity activity = this;

                if (isFocusedSingleTimer() || isFocusedStopwatchTimer()) {
                    try {
                        clearFocusSingleTimer();
                        clearFocusStopwatch();

                        new Handler().postDelayed(() -> showDialog(dialogLayout, inflater, alert, activity), 1000);
                    } catch (ClassCastException classCastException) {
                        clearFocusSingleTimer();
                        clearFocusStopwatch();
                    }
                } else {
                    showDialog(dialogLayout, inflater, alert, activity);
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(View[] dialogLayout, LayoutInflater inflater, AlertDialog alert, Activity activity) {
        dialogLayout[0] = inflater.inflate(R.layout.feedback_layout, (ViewGroup) getCurrentFocus());

        Button cancelButton = dialogLayout[0].findViewById(R.id.cancel_feedback);
        Button sendButton = dialogLayout[0].findViewById(R.id.send_feedback);
        EditText editText = dialogLayout[0].findViewById(R.id.feedback_editText);

        alert.setView(dialogLayout[0]);

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
    }

    public boolean isRemovedAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("removed_Ads", false);
    }

    public void logFirebaseAnalyticsEvents(String eventName) {
        if (isInProduction) {
            eventName = eventName.replace(" ", "_");
            eventName = eventName.replace(":", "");

            Bundle bundle = new Bundle();
            bundle.putString("Event", eventName);
            mFirebaseAnalytics.logEvent(eventName, bundle);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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