package com.example.covid_19;

import static com.example.covid_19.ui.home.Home.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.covid_19.ui.home.Home;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.messaging.FirebaseMessaging;

public class Setting extends AppCompatActivity {
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        final SwitchMaterial btnNotifications = findViewById(R.id.PushNofSwitch);
        final SwitchMaterial btnToggleDark = findViewById(R.id.darkSwitch);
        //final SwitchMaterial btnAutoUpdate = findViewById(R.id.updateSwitch);
        final LinearLayout settings = findViewById(R.id.settingsBackground);

        // Saving state of our app
        // using SharedPreferences
        editor = Home.sharedPreferences.edit();

        //AppCompatDelegate.setDefaultNightMode(NightMode);
        //2 is night mode, 1 is light mode
        btnToggleDark.setChecked(Home.NightMode == 2);
        if(Home.NightMode == 2){
            btnToggleDark.setChecked(true);
            settings.setBackgroundResource(R.drawable.background_night);
        }else if(Home.NightMode == 1){
            btnToggleDark.setChecked(false);
            settings.setBackgroundResource(R.drawable.background);
        }

        //1 is off, 2 is on
        if(Home.PushNotification == 2){
            btnNotifications.setChecked(true);
        }else if(Home.PushNotification == 1){
            btnNotifications.setChecked(false);
        }
        btnNotifications.setOnClickListener(view -> {
            //System.out.println(NightMode + ", " + btnToggleDark.isChecked());
            if (!btnNotifications.isChecked()) {
                // if dark mode is on it
                // will turn it off
                Home.PushNotification = 1;
                FirebaseMessaging.getInstance().unsubscribeFromTopic("news")
                        .addOnCompleteListener(task -> {
                            String msg = "Unsubscribed";
                            if (!task.isSuccessful()) {
                                msg = "Failed";
                            }
                            Log.d(TAG, msg);
                            Toast.makeText(Setting.this,msg, Toast.LENGTH_SHORT).show();
                        });
            }
            else if(btnNotifications.isChecked()) {
                // if dark mode is off
                // it will turn it on
                Home.PushNotification = 2;
                FirebaseMessaging.getInstance().subscribeToTopic("news")
                        .addOnCompleteListener(task -> {
                            String msg = "Subscribed";
                            if (!task.isSuccessful()) {
                                msg = "Failed";
                            }
                            Log.d(TAG, msg);
                            Toast.makeText(Setting.this,msg, Toast.LENGTH_SHORT).show();
                        });
            }
            editor.putInt("NotificationInt", Home.PushNotification);
            editor.apply();
        });
        //System.out.println(NightMode + ", " + btnToggleDark.isChecked());
        btnToggleDark.setOnClickListener(view -> {
            // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage(R.string.confirmation_msg)
                    .setTitle(R.string.confirmation_title);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                // User clicked OK button
                //System.out.println(NightMode + ", " + btnToggleDark.isChecked());
                if (!btnToggleDark.isChecked()) {
                    // if dark mode is on it
                    // will turn it off
                    Home.NightMode = 1;
                    Home.restart = 1;
                    //System.out.println("button pressed: set to day mode");
                }
                else if(btnToggleDark.isChecked()) {
                    // if dark mode is off
                    // it will turn it on
                    Home.NightMode = 2;
                    Home.restart = 1;
                    //System.out.println("button pressed: set to night mode");
                }
                //AppCompatDelegate.setDefaultNightMode(NightMode);
                editor.putInt("NightModeInt", Home.NightMode);
                editor.apply();
                Intent intent = new Intent(this, Home.class);
                this.startActivity(intent);
                this.finishAffinity();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
                // User cancelled the dialog
                if(Home.NightMode == 2){
                    btnToggleDark.setChecked(true);
                }else if(Home.NightMode == 1){
                    btnToggleDark.setChecked(false);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public void backBtn(View view){
        finish();
    }
}