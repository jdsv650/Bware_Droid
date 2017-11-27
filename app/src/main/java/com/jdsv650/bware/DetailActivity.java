package com.jdsv650.bware;

import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.jdsv650.bware.Constants.PREFS_NAME;

public class DetailActivity extends AppCompatActivity {

    Double lat = -99.0;
    Double lon = -99.0;

    private SharedPreferences preferences;
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    Bridge theBridge = new Bridge();

    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

        // get shared prefs
        preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        lat = getIntent().getExtras().getDouble("latitude");
        lon = getIntent().getExtras().getDouble("longitude");

        //Toast.makeText(this, "Lat = " + lat.toString() + " Lon = " + lon.toString(), Toast.LENGTH_SHORT).show();
        clearBridgeValues();
        getBridgeData(lat, lon);

    }


    private void getBridgeData(Double latitude, Double longitude)
    {
        String lat = latitude.toString();
        String lon = longitude.toString();

        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/GetByLocation?lat=" + lat
                + "&lon=" +lon;

        String token = preferences.getString("access_token","");  // is token stored

        if (token != "")
        {
            String urlEncoded = Uri.encode(urlAsString);
            RequestBody body = RequestBody.create(MEDIA_TYPE, urlEncoded);

            Request request = new Request.Builder()
                    .url(urlAsString)
                    .addHeader("Authorization", "Bearer " + token)
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
                            //final JSONArray jsonArray = new JSONArray(mMessage);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try
                                    {
                                        theBridge.country = json.optString("Country");
                                        theBridge.city = json.optString("Township");  // city
                                        theBridge.state = json.optString("State");
                                        theBridge.county =  json.optString("County");

                                        theBridge.locationDescription = json.optString("LocationDescription");
                                        theBridge.featureCarried = json.optString("FeatureCarried");
                                        theBridge.featureCrossed = json.optString("FeatureCrossed");
                                        theBridge.zip = json.optString("Zip");

                                        EditText countryET = (EditText) findViewById(R.id.countryEditText);
                                        EditText cityET = (EditText) findViewById(R.id.cityEditText);
                                        EditText stateET = (EditText) findViewById(R.id.stateEditText);
                                        EditText countyET = (EditText) findViewById(R.id.countyEditText);

                                        EditText locationET = (EditText) findViewById(R.id.descriptionEditText);
                                        EditText carriedET = (EditText) findViewById(R.id.carriedEditText);
                                        EditText crossedET = (EditText) findViewById(R.id.crossedEditText);
                                        EditText zipET = (EditText) findViewById(R.id.zipEditText);

                                        setEditTextAsTextView(countryET);
                                        if (theBridge.country != null && !theBridge.country.toUpperCase().equals("NULL")) {
                                            countryET.setText(theBridge.country);
                                        }

                                        setEditTextAsTextView(cityET);
                                        if (theBridge.city != null && !theBridge.city.toUpperCase().equals("NULL")) {
                                            cityET.setText(theBridge.city);
                                        }

                                        setEditTextAsTextView(stateET);
                                        if (theBridge.state != null && !theBridge.state.toUpperCase().equals("NULL")) {
                                            stateET.setText(theBridge.state);
                                        }

                                        setEditTextAsTextView(countyET);
                                        if (theBridge.county != null && !theBridge.county.toUpperCase().equals("NULL")) {
                                            countyET.setText(theBridge.county);
                                        }

                                        setEditTextAsTextView(locationET);
                                        if (theBridge.locationDescription != null && !theBridge.locationDescription.toUpperCase().equals("NULL")) {
                                            locationET.setText(theBridge.locationDescription);
                                        }

                                        setEditTextAsTextView(carriedET);
                                        if (theBridge.featureCarried != null && !theBridge.featureCarried.toUpperCase().equals("NULL")) {
                                            carriedET.setText(theBridge.featureCarried);
                                        }

                                        setEditTextAsTextView(crossedET);
                                        if (theBridge.featureCrossed != null && !theBridge.featureCrossed.toUpperCase().equals("NULL")) {
                                            crossedET.setText(theBridge.featureCrossed);
                                        }

                                        setEditTextAsTextView(zipET);
                                        if (theBridge.zip != null && !theBridge.zip.toUpperCase().equals("NULL")) {
                                            zipET.setText(theBridge.zip);
                                        }

                                        /*** Restricted ***/
                                        theBridge.weightStraight = json.optDouble("WeightStraight", -99.0);
                                        theBridge.weightStraight_TriAxle = json.optDouble("WeightStraight_TriAxle", -99.0);
                                        theBridge.weightCombo = json.optDouble("WeightCombination", -99.0);
                                        theBridge.weightDouble = json.optDouble("WeightDouble", -99.0);

                                        theBridge.height = json.optDouble("Height",-99.0);
                                        theBridge.otherPosting = json.optString("OtherPosting");
                                        theBridge.isRPosted = json.optBoolean( "isRposted");

                                        EditText straightET = (EditText) findViewById(R.id.tandemEditText);
                                        EditText triET = (EditText) findViewById(R.id.triaxleEditText);
                                        EditText comboET = (EditText) findViewById(R.id.combinationEditText);
                                        EditText doubleET = (EditText) findViewById(R.id.doubleEditText);

                                        EditText heightET = (EditText) findViewById(R.id.heightEditText);
                                        EditText otherET = (EditText) findViewById(R.id.otherEditText);
                                        Switch isRET = (Switch) findViewById(R.id.rSwitch);

                                        setEditTextAsTextView(straightET);
                                        if (theBridge.weightStraight != -99) {
                                            straightET.setText(theBridge.weightStraight.toString());
                                        }

                                        setEditTextAsTextView(triET);
                                        if (theBridge.weightStraight_TriAxle != -99) {
                                            triET.setText(theBridge.weightStraight_TriAxle.toString());
                                        }

                                        setEditTextAsTextView(comboET);
                                        if (theBridge.weightCombo != -99) {
                                            comboET.setText(theBridge.weightCombo.toString());
                                        }

                                        setEditTextAsTextView(doubleET);
                                        if (theBridge.weightDouble != -99) {
                                            doubleET.setText(theBridge.weightDouble.toString());
                                        }

                                        setEditTextAsTextView(heightET);
                                        if (theBridge.height != -99) { heightET.setText(theBridge.height.toString()); }

                                        setEditTextAsTextView(otherET);

                                        if (theBridge.otherPosting != null && !theBridge.otherPosting.toUpperCase().equals("NULL")) {
                                            otherET.setText(theBridge.otherPosting);
                                        }

                                        isRET.setChecked(theBridge.isRPosted);
                                        isRET.setEnabled(false);

                                    }
                                    catch (Exception ex)
                                    {
                                        Toast.makeText(getBaseContext(), "Error logging in please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    } // end response success
                    else   // unsuccessful response
                    {
                        if (response.code() == 400 || response.code() == 401) // received a response from server
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
        else  // no token found
        {
            finish(); // logout
        }

    }



    void clearBridgeValues()
    {
        theBridge.country = "";
        theBridge.county = "";
        theBridge.featureCarried = "";
        theBridge.featureCrossed = "";
        theBridge.locationDescription = "";
        theBridge.city = "";
        theBridge.state = "";
        theBridge.zip = "";
        theBridge.otherPosting = "";
    }


    void setEditTextAsTextView(EditText et)
    {
        et.setCursorVisible(false);
        et.setLongClickable(false);
        et.setClickable(false);
        et.setFocusable(false);
        et.setSelected(false);
        et.setKeyListener(null);
        et.setOnTouchListener(otl);
        //et.setBackgroundResource(android.R.color.transparent);
    }

    private View.OnTouchListener otl = new View.OnTouchListener() {
        public boolean onTouch (View v, MotionEvent event) {
            return true;
        }
    };

}
