package com.jdsv650.bware;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    public void loginPressed(View view)
    {
        Intent i = new Intent(this, LoginActivity.class);
        this.startActivity(i);
    }

    public void signupPressed(View view)
    {
        Intent i = new Intent(this, SignupActivity.class);
        this.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }
}
