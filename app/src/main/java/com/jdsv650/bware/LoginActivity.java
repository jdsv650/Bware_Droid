package com.jdsv650.bware;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class LoginActivity extends AppCompatActivity implements ResetPasswordFragment.ResetPasswordDialogListener {


    EditText emailText;
    EditText passwordText;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button forgot = (Button) findViewById(R.id.forgotButton);
        forgot.setVisibility(View.INVISIBLE);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

    }

    public void loginPressed(View view)
    {
        emailText = (EditText) findViewById(R.id.emailEditText);
        passwordText = (EditText) findViewById(R.id.passwordEditText);


        if (emailText.getText().toString().length() == 0 ||
                passwordText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Email and password field must not be blank", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // supplied credentials so try for token
           getToken();
        }

    }

    public void forgotPressed(View view)
    {
        resetPassword();
    }

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");


    public void getToken()
    {
        String urlAsString = Constants.baseUrlAsString + "/token";

        Log.w("username", emailText.getText().toString());
        Log.w("password", passwordText.getText().toString());

        String reqBody = "grant_type=password&username=" + emailText.getText().toString()
                        + "&password=" + passwordText.getText().toString() ;

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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {

                                    /***
                                    String err = json.getString("error");
                                    if (err == "invalid_grant")
                                    {
                                        Toast.makeText(getBaseContext(), "Please verify your email and password", Toast.LENGTH_SHORT).show();
                                        return;  // don't try to get token info it failed so just exit
                                    }  ****/

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


    public void resetPassword()
    {
        showResetPasswordDialog();

    }


    private void showResetPasswordDialog() {

        FragmentManager fm = getFragmentManager();

        ResetPasswordFragment resetDialog = new ResetPasswordFragment();
        resetDialog.show(fm, "Reset Password");
    }

    // callback from resetpassword dialog with email
    @Override
    public void onFinishResetPasswordDialog(String email) {

        String urlAsString = Constants.baseUrlAsString + "/api/Account/ForgotPassword";

        Log.w("email", email);

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
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
                        Toast.makeText(LoginActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

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
                            final String message = json.optString("message","Reset Password Failed. Please try again");


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                                    Log.i("TOAST", "isSuccess FALSE TOAST");
                                }
                            });

                        }
                        else // reset password call OK
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(LoginActivity.this, "Check your email and follow the link provided to reset your B*ware password\"", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Reset Password Failed. Please try again", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(LoginActivity.this, "Please verify email", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast.makeText(LoginActivity.this, "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    return;
                }

            }
        });

    }
}
