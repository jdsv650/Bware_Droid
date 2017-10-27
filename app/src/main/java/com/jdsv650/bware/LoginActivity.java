package com.jdsv650.bware;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    public void loginPressed(View view)
    {
        EditText emailText = (EditText) findViewById(R.id.emailEditText);
        EditText passwordText = (EditText) findViewById(R.id.passwordEditText);
        if (emailText.getText().toString().length() == 0 ||
                passwordText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Email and password field must not be blank", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // have creds so try for token
            getToken();

        }

    }

    void getToken()
    {

    }


}
