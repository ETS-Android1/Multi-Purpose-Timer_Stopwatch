package com.armcomptech.akash.simpletimer4.buildTimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import com.armcomptech.akash.simpletimer4.billing.BillingClientSetup;
import com.armcomptech.akash.simpletimer4.multiTimer.MultiTimerActivity;
import com.armcomptech.akash.simpletimer4.statistics.StatisticsActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.armcomptech.akash.simpletimer4.buildTimer.BuildGroupAdapter.clearFocusBuildTimer2;
import static com.armcomptech.akash.simpletimer4.buildTimer.BuildGroupAdapter.isFocusedBuildTimer2;

public class buildTimer_Activity extends AppCompatActivity implements setNameAndTimerDialogForBuildTimer.setTimerDialogListenerForBuildTimer, PurchasesUpdatedListener {

    RecyclerView recyclerView;
    ExtendedFloatingActionButton addGroupFab;
    ExtendedFloatingActionButton startTimerFab;
    private final ArrayList<RecyclerView.ViewHolder> holders = new ArrayList<>();

    Spinner saved_timers_spinner;
    static EditText save_timer_editText;
    Button save_timer_button;

    ArrayList<MasterInfo> masterList = new ArrayList<>();
    MasterInfo currentMaster;

    ArrayAdapter<String> saved_timers_spinner_adapter;
    final ArrayList<String> saved_timers_names = new ArrayList<>();
    private FirebaseAnalytics mFirebaseAnalytics;

    static AdView banner_adView;
    AdRequest banner_adRequest;

    BillingClient billingClient;
    ConsumeResponseListener consumeResponseListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setThemeForApp(sharedPreferences);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_timer);

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

        if (TabbedActivity.isInProduction) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        load_data();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle("   Build Your Timer");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_build_white);
        }

        initializeBillingProcess();

        //create timer
        BasicTimerInfo tempTimer = new BasicTimerInfo(60 * 1000 , "one minute");
        ArrayList<BasicTimerInfo> tempTimerArray = new ArrayList<>();
        tempTimerArray.add(tempTimer);

        //create group
        BasicGroupInfo tempGroup;
        if (currentMaster != null) {
            if (currentMaster.basicGroupInfoArrayList != null) {
                tempGroup = new BasicGroupInfo(tempTimerArray, 2, "Group: " + (currentMaster.basicGroupInfoArrayList.size() + 1));
            } else {
                tempGroup = new BasicGroupInfo(tempTimerArray, 2, "Group: 1");
            }
        } else {
            tempGroup = new BasicGroupInfo(tempTimerArray, 2, "Group: 1");
        }
        ArrayList<BasicGroupInfo> tempGroupArray = new ArrayList<>();
        tempGroupArray.add(tempGroup);

        //create master
        currentMaster = new MasterInfo(tempGroupArray, "");

        recyclerView = findViewById(R.id.buildTimerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new BuildGroupAdapter(this, currentMaster, holders));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        ViewGroup.LayoutParams params= recyclerView.getLayoutParams();
        params.height = Resources.getSystem().getDisplayMetrics().heightPixels - dpToPx(125);
        recyclerView.setLayoutParams(params);

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if (currentMaster.basicGroupInfoArrayList.size() > 0) {
                    startTimerFab.setVisibility(View.VISIBLE);
                    save_timer_button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (currentMaster.basicGroupInfoArrayList.size() == 0) {
                    startTimerFab.setVisibility(View.GONE);
                    save_timer_button.setVisibility(View.GONE);
                }
            }
        });

        addGroupFab = findViewById(R.id.addGroupFloatingActionButton);
        addGroupFab.setOnClickListener(v -> {
            //create timer
            BasicTimerInfo tempTimer2 = new BasicTimerInfo(60 * 1000 , "one minute");
            ArrayList<BasicTimerInfo> tempTimerArray2 = new ArrayList<>();
            tempTimerArray2.add(tempTimer2);

            //create group
            BasicGroupInfo tempGroup2 = new BasicGroupInfo(tempTimerArray2, 2, "Group: " + (currentMaster.basicGroupInfoArrayList.size() + 1));

            //add to master
            currentMaster.basicGroupInfoArrayList.add(tempGroup2);

            recyclerView.setAdapter(new BuildGroupAdapter(this, currentMaster, holders));
            recyclerView.smoothScrollToPosition(currentMaster.basicGroupInfoArrayList.size() - 1);
        });

        startTimerFab = findViewById(R.id.startTimerFloatingActionButton);
        startTimerFab.setOnClickListener(v -> {
            String masterName = String.valueOf(save_timer_editText.getText());

            if (masterName.isEmpty()) {
                masterName = "General";
            }

            save_group(masterName);
            notifyChange();

            Intent intent = new Intent(this, timerForBuilt_Activity.class); //add the activity where it is being sent
            intent.putExtra("masterName", currentMaster.masterName);

            ArrayList<String> timerNameArray = new ArrayList<>();
            ArrayList<String> groupNameArray = new ArrayList<>();
            ArrayList<Integer> timerTimeArray = new ArrayList<>();
            ArrayList<String> stringOfTimerArray = new ArrayList<>();

            for (int i = 0; i < currentMaster.basicGroupInfoArrayList.size(); i++) {
                for (int j = 0; j < currentMaster.basicGroupInfoArrayList.get(i).repeatSets; j++) {
                    for (int k = 0; k < currentMaster.basicGroupInfoArrayList.get(i).basicTimerInfoArrayList.size(); k++) {
                        String timerName = currentMaster.basicGroupInfoArrayList.get(i).basicTimerInfoArrayList.get(k).timerName;
                        String groupName = currentMaster.basicGroupInfoArrayList.get(i).groupName;
                        long start_time = currentMaster.basicGroupInfoArrayList.get(i).basicTimerInfoArrayList.get(k).mStartTimeInMillis;
                        timerNameArray.add(timerName);
                        groupNameArray.add(groupName);
                        timerTimeArray.add((int) start_time);
                        stringOfTimerArray.add(groupName + " - " + timerName + " - " + getTimeLeftFormatted(start_time));
                    }
                }
            }

            intent.putExtra("timerName", timerNameArray);
            intent.putExtra("groupName", groupNameArray);
            intent.putExtra("timerTime", timerTimeArray);
            intent.putExtra("stringOfTimer", stringOfTimerArray);
            startActivity(intent);
        });

        saved_timers_spinner = findViewById(R.id.saved_timers_spinner);
        if (masterList != null) {
            for (MasterInfo masterInfo: masterList) {
                saved_timers_names.add(masterInfo.masterName);
            }
        }
        saved_timers_spinner_adapter = new ArrayAdapter<>(this, R.layout.timername_autocomplete_textview, saved_timers_names);
        saved_timers_spinner.setAdapter(saved_timers_spinner_adapter);
        saved_timers_spinner.setPrompt("Past Timers");

        saved_timers_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                load_group(position);
                Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                notifyChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        save_timer_editText = findViewById(R.id.name_of_built_timer);
        save_timer_editText.setOnEditorActionListener((view, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                hideKeyboard(view);
                save_timer_editText.clearFocus();
                return true;
            }
            return false;
        });

        save_timer_button = findViewById(R.id.save_timer_button);
        save_timer_button.setOnClickListener(v -> {
            String masterName = String.valueOf(save_timer_editText.getText());
            save_group(masterName);
            hideKeyboard(save_timer_editText);
            saved_timers_spinner_adapter.notifyDataSetChanged();
            notifyChange();

            save_timer_editText.clearFocus();
        });
    }

    private void setThemeForApp(SharedPreferences sharedPreferences) {
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
    }

    public static boolean isFocusedBuildTimer1() {
        if (banner_adView == null) {
            return save_timer_editText.isFocused() || isFocusedBuildTimer2();
        } else {
            return banner_adView.hasFocus() || save_timer_editText.isFocused() || isFocusedBuildTimer2();
        }
    }

    public static void clearFocusBuildTimer1() {
        if (banner_adView != null) {
            banner_adView.clearFocus();
        }

        save_timer_editText.clearFocus();
        clearFocusBuildTimer2();
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
                    int response = billingClient.launchBillingFlow(buildTimer_Activity.this, billingFlowParams)
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

    public String getTimeLeftFormatted(long mStartTimeInMillis) {
        int hours = (int) (mStartTimeInMillis / 1000) / 3600;
        int minutes = (int) ((mStartTimeInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mStartTimeInMillis / 1000) % 60;
        int millis = (int) mStartTimeInMillis % 1000;

        String timeLeftFormatted;

        if (hours >= 10) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d:%02d", hours, minutes, seconds);
        } else if (hours >= 1) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes >= 1) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d.%03d", seconds, millis);
        }

        return timeLeftFormatted;
    }

    private void notifyChange() {
        recyclerView.setAdapter(new BuildGroupAdapter(this, currentMaster, holders));

        ArrayList<String> saved_timers_names2 = new ArrayList<>();
        if (masterList != null) {
            for (MasterInfo masterInfo: masterList) {
                saved_timers_names2.add(masterInfo.masterName);
            }
        }

        saved_timers_spinner_adapter.clear();
        saved_timers_spinner_adapter.addAll(saved_timers_names2);
    }

    private void load_group(int position) {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();

        String masterListOfTimersJson = sharedPreferences.getString("masterList", null);
        Type masterListOfTimerNameType = new TypeToken<ArrayList<MasterInfo>>(){}.getType();
        masterList = gson.fromJson(masterListOfTimersJson, masterListOfTimerNameType);

        if (masterList != null) {
            currentMaster = masterList.get(position);
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        save_timer_editText.setText(currentMaster.masterName);

        Toast.makeText(this, "Selected: " + currentMaster.masterName, Toast.LENGTH_SHORT).show();
//        String masterGroupSetCountsJson = sharedPreferences.getString("masterGroupSetCounts", null);
//        Type masterGroupSetCountsType = new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType();
//        masterGroupSetCounts = gson.fromJson(masterGroupSetCountsJson, masterGroupSetCountsType);
//        if (masterGroupSetCounts == null) {
//            masterGroupSetCounts = new ArrayList<>();
//        }
//        groupSetCounts = masterGroupSetCounts.get(position);
//
//        String masterGroupNamesJson = sharedPreferences.getString("masterGroupNames", null);
//        Type masterGroupNamesType = new TypeToken<ArrayList<ArrayList<String>>>(){}.getType();
//        masterGroupNames = gson.fromJson(masterGroupNamesJson, masterGroupNamesType);
//        if (masterGroupNames == null) {
//            masterGroupNames = new ArrayList<>();
//        }
//        groupNames = masterGroupNames.get(position);
//
//        String masterListNamesJson = sharedPreferences.getString("masterListNames", null);
//        Type masterListNamesType = new TypeToken<ArrayList<String>>(){}.getType();
//        saved_timers_names = gson.fromJson(masterListNamesJson, masterListNamesType);
//        if (saved_timers_names == null) {
//            saved_timers_names = new ArrayList<>();
//        }

    }

    private void load_data() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();

        String masterListOfTimersJson = sharedPreferences.getString("masterList", null);
        Type masterListOfTimerNameType = new TypeToken<ArrayList<MasterInfo>>(){}.getType();
        masterList = gson.fromJson(masterListOfTimersJson, masterListOfTimerNameType);
//        if (masterList == null) {
//            masterList = new ArrayList<>();
//        }
//
//        String masterGroupSetCountsJson = sharedPreferences.getString("masterGroupSetCounts", null);
//        Type masterGroupSetCountsType = new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType();
//        masterGroupSetCounts = gson.fromJson(masterGroupSetCountsJson, masterGroupSetCountsType);
//        if (masterGroupSetCounts == null) {
//            masterGroupSetCounts = new ArrayList<>();
//        }
//
//        String masterGroupNamesJson = sharedPreferences.getString("masterGroupNames", null);
//        Type masterGroupNamesType = new TypeToken<ArrayList<ArrayList<String>>>(){}.getType();
//        masterGroupNames = gson.fromJson(masterGroupNamesJson, masterGroupNamesType);
//        if (masterGroupNames == null) {
//            masterGroupNames = new ArrayList<>();
//        }
//
//        String masterListNamesJson = sharedPreferences.getString("masterListNames", null);
//        Type masterListNamesType = new TypeToken<ArrayList<String>>(){}.getType();
//        saved_timers_names = gson.fromJson(masterListNamesJson, masterListNamesType);
//        if (saved_timers_names == null) {
//            saved_timers_names = new ArrayList<>();
//        }
    }

    private void save_group(String masterName) {
        if (masterName.isEmpty()) {
            Toast.makeText(this, "Name required to save", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String masterListOfTimersJson = sharedPreferences.getString("masterList", null);
        Type masterListOfTimerNameType = new TypeToken<ArrayList<MasterInfo>>(){}.getType();
        masterList = gson.fromJson(masterListOfTimersJson, masterListOfTimerNameType);
//        if (masterList == null) {
//            masterList = new ArrayList<>();
//        }
//
//        String masterGroupSetCountsJson = sharedPreferences.getString("masterGroupSetCounts", null);
//        Type masterGroupSetCountsType = new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType();
//        masterGroupSetCounts = gson.fromJson(masterGroupSetCountsJson, masterGroupSetCountsType);
//        if (masterGroupSetCounts == null) {
//            masterGroupSetCounts = new ArrayList<>();
//        }
//
//        String masterGroupNamesJson = sharedPreferences.getString("masterGroupNames", null);
//        Type masterGroupNamesType = new TypeToken<ArrayList<ArrayList<String>>>(){}.getType();
//        masterGroupNames = gson.fromJson(masterGroupNamesJson, masterGroupNamesType);
//        if (masterGroupNames == null) {
//            masterGroupNames = new ArrayList<>();
//        }
//
//        String masterListNamesJson = sharedPreferences.getString("masterListNames", null);
//        Type masterListNamesType = new TypeToken<ArrayList<String>>(){}.getType();
//        saved_timers_names = gson.fromJson(masterListNamesJson, masterListNamesType);
//        if (saved_timers_names == null) {
//            saved_timers_names = new ArrayList<>();
//        }

        if (masterExists(masterName)) {
            Toast.makeText(this, "Name already exist. Overwriting...", Toast.LENGTH_LONG).show();
        }

        currentMaster.masterName = masterName;
        if (masterList == null) {
            masterList = new ArrayList<>();
        }
        masterList.add(currentMaster);

//        masterGroupSetCounts.add(groupSetCounts);
//        masterGroupNames.add(groupNames);
//        saved_timers_names.add(groupOfTimerName);

        String masterListJson = gson.toJson(masterList);
        editor.putString("masterList", masterListJson);

//        String masterGroupSetCountJson = gson.toJson(masterGroupSetCounts);
//        editor.putString("masterGroupSetCounts", masterGroupSetCountJson);
//
//        String masterGroupNameJson = gson.toJson(masterGroupNames);
//        editor.putString("masterGroupNames", masterGroupNameJson);
//
//        String masterListTimerNamesJson = gson.toJson(saved_timers_names);
//        editor.putString("masterListNames", masterListTimerNamesJson);
        editor.apply();

        Toast.makeText(this, "Saved: " + masterName, Toast.LENGTH_SHORT).show();
    }

    private boolean masterExists(String groupOfTimerName) {
        if (masterList == null) {
            return false;
        }
        for (int i = 0; i < masterList.size(); i++) {
            if (masterList.get(i).masterName.equals(groupOfTimerName)) {
                masterList.remove(i);

//                groupSetCounts.remove(i);
//                groups.remove(i);
//                groupNames.remove(i);
//                saved_timers_names.remove(i);
//
//                for (int j = 0; j < groupNames.size(); j++) {
//                    if (!groupNames.get(j).contains("Group: ")) {
//                        groupNames.set(j, "Group: " + (j+1));
//                    }
//                }
                return true;
            }
        }
        return false;
    }

    public void hideKeyboard(TextView view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void createNewTimerNameAndTimeForBuildTimer (
            String time,
            int hours,
            int minutes,
            int seconds,
            String name,
            boolean creatingNewTimer,
            boolean updateExistingTimer,
            int adapterPosition,
            ArrayList<BasicTimerInfo> timers) {

        long millisInput;
        long finalSecond;

        if (!time.equals("null")) {
            long input = Long.parseLong(time);
            long hour = input / 10000;
            long minuteRaw = (input - (hour * 10000)) ;
            long minuteOne = minuteRaw / 1000;
            long minuteTwo = (minuteRaw % 1000) / 100;
            long minute = (minuteOne * 10) + minuteTwo;
            long second = input - ((hour * 10000) + (minute * 100));
            finalSecond = (hour * 3600) + (minute * 60) + second;

            if (time.length() == 0) {
                Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

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
            timers.add(new BasicTimerInfo(finalSecond * 1000, name));
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        }
        if (updateExistingTimer) {
            timers.get(adapterPosition).timerName = name;
            timers.get(adapterPosition).mStartTimeInMillis  = finalSecond * 1000;
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        }
    }

    public int dpToPx(int dp) {
        float density = getApplicationContext().getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        menu.findItem(R.id.check_sound).setVisible(false);
        menu.findItem(R.id.build_Timer_Mode).setVisible(false);
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

    public boolean isRemovedAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("removed_Ads", false);
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

            case R.id.multi_Timer_Mode:
                Intent intent3 = new Intent(this, MultiTimerActivity.class);
                intent3.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent3);
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
                Activity activity = this;

                LayoutInflater inflater = getLayoutInflater();
                final View[] dialogLayout = new View[1];

                if (isFocusedBuildTimer1()) {
                    clearFocusBuildTimer1();

                    new Handler().postDelayed(() -> sendFeedbackDialog(dialogLayout, inflater, alert, activity), 1000);
                } else {
                    sendFeedbackDialog(dialogLayout, inflater, alert, activity);
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendFeedbackDialog(View[] dialogLayout, LayoutInflater inflater, AlertDialog alert, Activity activity) {
        try {
            dialogLayout[0] = inflater.inflate(R.layout.feedback_layout, (ViewGroup) getCurrentFocus());
        } catch (ClassCastException classCastException) {
            clearFocusBuildTimer1();
            return;
        }

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

    private void removeAds() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("removed_Ads", true);
        editor.apply();
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