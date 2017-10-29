package com.jdsv650.bware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get shared prefs
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        /*
          preferences.edit().putString("access_token", accessToken).apply();
                                    preferences.edit().putString(".expires", expires).apply();
                                    preferences.edit().putString("userName", username).apply();
         */

        String token = preferences.getString("access_token","");  // is token stored

        // NO call to setContentView just start activity based on if we have a stored token
        Intent intent;

        Log.i("TOKEN", token);

        if (token == "") { intent = new Intent(this, WelcomeActivity.class); }
        else { intent = new Intent(this, MapsActivity.class); }

        startActivity(intent);
        finish();

    }
}
