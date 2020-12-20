package com.armcomptech.akash.simpletimer4.Settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.armcomptech.akash.simpletimer4.EmailLogic.SendMailTask;
import com.armcomptech.akash.simpletimer4.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.armcomptech.akash.simpletimer4.TabbedView.TabbedActivity.logFirebaseAnalyticsEvents;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        setTitle("   Settings");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_settings_white);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        menu.findItem(R.id.check_sound).setVisible(false);
        menu.findItem(R.id.timer_and_stopwatch).setVisible(false);
        menu.findItem(R.id.multi_Timer_Mode).setVisible(false);
        menu.findItem(R.id.setting_activity).setVisible(false);
        menu.findItem(R.id.statistics_activity).setVisible(false);
//        menu.add(0, R.id.privacy_policy, 4, menuIconWithText(getResources().getDrawable(R.drawable.ic_lock_black), "Privacy Policy"));

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

        switch (id) {
            case R.id.privacy_policy:
                logFirebaseAnalyticsEvents("Opened Privacy Policy");
                Intent myWebLink = new Intent(Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://timerpolicy.blogspot.com/2019/06/privacy-policy-armcomptech-built.html"));
                startActivity(myWebLink);
                break;

            case R.id.send_feedback:
                final EditText edittext = new EditText(this);
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Send Feedback");
                alert.setMessage("How can this app be improved?");

                alert.setView(edittext);

                alert.setPositiveButton("Send", (dialog, whichButton) -> {
                    String feedback = String.valueOf(edittext.getText());
                    String subject = "Feedback for Timer Application";
                    List<String> toEmail = Collections.singletonList(getString(R.string.toEmail));
                    new SendMailTask(this).execute(getString(R.string.fromEmail), getString(R.string.fromPassword), toEmail, subject, feedback, new ArrayList<File>());
                    Toast.makeText(getApplicationContext(), "Feedback sent successfully", Toast.LENGTH_SHORT).show();
                });

                alert.setNegativeButton("Cancel", (dialog, whichButton) ->
                        Toast.makeText(getApplicationContext(), "Feedback was not sent", Toast.LENGTH_SHORT).show());

                alert.show();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}