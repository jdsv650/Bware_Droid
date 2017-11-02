package com.jdsv650.bware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.jdsv650.bware.Constants.PREFS_NAME;

public class LoginActivity extends AppCompatActivity {


    EditText emailText;
    EditText passwordText;
    final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        OkHttpClient trustAllclient = trustAllSslClient(client);

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

                                // Toast.makeText(getBaseContext(), mMessage, Toast.LENGTH_LONG).show();
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


    /** SSL bypass **/
    private static final TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };
    private static final SSLContext trustAllSslContext;
    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

    public static OkHttpClient trustAllSslClient(OkHttpClient client) {

        OkHttpClient.Builder builder = client.newBuilder();
        builder.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager)trustAllCerts[0]);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        return builder.build();
    }

}
