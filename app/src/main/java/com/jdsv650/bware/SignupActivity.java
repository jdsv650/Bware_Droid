package com.jdsv650.bware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.jdsv650.bware.Constants.PREFS_NAME;

public class SignupActivity extends AppCompatActivity {

    EditText userName;
    EditText password;
    EditText passwordConfirm;
    OkHttpClient client = new OkHttpClient();
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

        userName = (EditText) findViewById(R.id.emailEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirmEditText);

    }

    public void signupPressed(View view)
    {
        Toast.makeText(this, "Attempting to register user...", Toast.LENGTH_SHORT).show();

        if (userName.getText().toString().length() == 0 ||
                password.getText().toString().length() == 0 ||
                passwordConfirm.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Email, password and confirm password field must not be blank", Toast.LENGTH_SHORT).show();
        }
        else if(!password.getText().toString().equals((passwordConfirm.getText().toString())))
        {
            Toast.makeText(this, "Password and confirm password must match", Toast.LENGTH_SHORT).show();
        }
        else
        {
            signupNewUser();
        }
    }


    void signupNewUser()
    {
        String urlAsString = Constants.baseUrlAsString + "/api/Account/Register";

        Log.w("username", userName.getText().toString());
        Log.w("password", password.getText().toString());
        Log.w("password", passwordConfirm.getText().toString());

        String reqBody = "email=" + userName.getText().toString()
                + "&username=" + userName.getText().toString()
                + "&password=" + password.getText().toString()
                + "&confirmpassword=" + passwordConfirm.getText().toString();

        final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
        String urlEncoded = Uri.encode(reqBody, ALLOWED_URI_CHARS);

        RequestBody body = RequestBody.create(MEDIA_TYPE, urlEncoded);

        RequestBody formBody = new FormBody.Builder()
                .add("username", userName.getText().toString())
                .add("email", userName.getText().toString())
                .add("password", password.getText().toString())
                .add("confirmpassword", passwordConfirm.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url(urlAsString)
                .addHeader("Content-Type", "application/json")
                .post(formBody)
                .build();

        OkHttpClient trustAllclient = Helper.trustAllSslClient(client);

        trustAllclient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignupActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

                    }
                });
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String mMessage = response.body().string();
                Log.w("success Response", mMessage);

                if (response.isSuccessful()){
                    try {
                        final JSONObject json = new JSONObject(mMessage);

                        Boolean isSuccess = json.getBoolean("isSuccess");
                        if (!isSuccess)
                        {
                            // message may include "Name xxxx@xxxxx.com is already taken"
                            final String message = json.optString("message","Register User Failed. Please try again");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else // registered user OK
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getBaseContext(), "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                }
                            });

                            getToken(); // Try to login
                        }


                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), "Register User Failed. Please try again", Toast.LENGTH_SHORT).show();
                    }

                } // end response success
                else   // unsuccessful response
                {
                    if (response.code() == 400) // received a response from server
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), "Please verify username, password and confirm password", Toast.LENGTH_SHORT).show();
                    }
                });
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    return;
                }

            }
        });
    }


    public void getToken()
    {
        String urlAsString = Constants.baseUrlAsString + "/token";

        Log.w("username", userName.getText().toString());
        Log.w("password", password.getText().toString());

        String reqBody = "grant_type=password&username=" + userName.getText().toString()
                + "&password=" + password.getText().toString() ;

        //grant_type=password&username=jds%40gmail.com&password=******";

        final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
        String urlEncoded = Uri.encode(reqBody, ALLOWED_URI_CHARS);

        RequestBody body = RequestBody.create(MEDIA_TYPE, urlEncoded);

        final Request request = new Request.Builder()
                .url(urlAsString)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        OkHttpClient trustAllclient = Helper.trustAllSslClient(client);

        trustAllclient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String mMessage = response.body().string();
                Log.w("success Response", mMessage);

                if (response.isSuccessful()){
                    try {
                        final JSONObject json = new JSONObject(mMessage);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {

                                    String accessToken = json.getString("access_token");
                                    String expires = json.getString(".expires");
                                    String username = json.getString("userName");

                                    // get shared prefs
                                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

                                    // save the fields to shared prefs
                                    preferences.edit().putString("access_token", accessToken).apply();
                                    preferences.edit().putString(".expires", expires).apply();
                                    preferences.edit().putString("userName", username).apply();

                                }
                                catch (Exception ex)
                                {
                                    Toast.makeText(getBaseContext(), "Error logging in please try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        // go to map
                        Intent intent = new Intent(getBaseContext(), BottomNavigationActivity.class);
                        startActivity(intent);

                    } catch (Exception e){
                        e.printStackTrace();
                    }

                } // end response success
                else   // unsuccessful response
                {
                    if (response.code() == 400) // received a response from server
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    return;
                }

            }
        });

    }



}
