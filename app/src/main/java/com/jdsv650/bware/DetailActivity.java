package com.jdsv650.bware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
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

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    Double lat = -99.0;
    Double lon = -99.0;
    Integer bridgeId = -99;

    ImageView noBridge1;
    ImageView noBridge2;
    ImageView noBridge3;
    ImageView editBridge1;
    ImageView editBridge2;
    ImageView editBridge3;

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

        noBridge1 = (ImageView) findViewById(R.id.noBridgeImage1);
        noBridge2 = (ImageView) findViewById(R.id.noBridgeImage2);
        noBridge3 = (ImageView) findViewById(R.id.noBridgeImage3);
        editBridge1 = (ImageView) findViewById(R.id.editBridgeImage1);
        editBridge2 = (ImageView) findViewById(R.id.editBridgeImage2);
        editBridge3 = (ImageView) findViewById(R.id.editBridgeImage3);

        noBridge1.setVisibility(ImageView.GONE);
        noBridge2.setVisibility(ImageView.GONE);
        noBridge3.setVisibility(ImageView.GONE);
        editBridge1.setVisibility(ImageView.GONE);
        editBridge2.setVisibility(ImageView.GONE);
        editBridge3.setVisibility(ImageView.GONE);

        Button noB = (Button) findViewById(R.id.noBridgeButton);
        noB.setOnClickListener(this);

        Button wrongB = (Button) findViewById(R.id.wrongInfoButton);
        wrongB.setOnClickListener(this);

        Button editB = (Button) findViewById(R.id.editButton);
        editB.setOnClickListener(this);

        Button removeB = (Button) findViewById(R.id.removeButton);
        removeB.setOnClickListener(this);

        // get shared prefs
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        lat = getIntent().getExtras().getDouble("latitude");
        lon = getIntent().getExtras().getDouble("longitude");

        clearBridgeValues();
        getBridgeData(lat, lon);
    }


    // call api - bridge/getByLocation
    private void getBridgeData(Double latitude, Double longitude) {
        String lat = latitude.toString();
        String lon = longitude.toString();

        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/GetByLocation?lat=" + lat
                + "&lon=" + lon;

        String token = preferences.getString("access_token", "");  // get token

        if (token != "")  // token stored
        {
            String urlEncoded = Uri.encode(urlAsString);

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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

                        }
                    });
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()) {
                        try {
                            final JSONObject json = new JSONObject(mMessage);
                            //final JSONArray jsonArray = new JSONArray(mMessage);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        if (!json.isNull("User1Reason")) {
                                            Boolean reason1 = json.getBoolean("User1Reason");

                                            if (!reason1) {
                                                noBridge1.setVisibility(View.VISIBLE);
                                            } else {
                                                editBridge1.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        if (!json.isNull("User2Reason")) {
                                            Boolean reason2 = json.getBoolean("User2Reason");

                                            if (!reason2) {
                                                noBridge2.setVisibility(View.VISIBLE);
                                            } else {
                                                editBridge2.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        if (!json.isNull("User3Reason")) {
                                            Boolean reason3 = json.getBoolean("User3Reason");

                                            if (!reason3) {
                                                noBridge3.setVisibility(View.VISIBLE);
                                            } else {
                                                editBridge3.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        bridgeId = json.optInt("BridgeId", -99);

                                        theBridge.country = json.optString("Country");
                                        theBridge.city = json.optString("Township");  // city
                                        theBridge.state = json.optString("State");
                                        theBridge.county = json.optString("County");

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

                                        theBridge.height = json.optDouble("Height", -99.0);
                                        theBridge.otherPosting = json.optString("OtherPosting");
                                        theBridge.isRPosted = json.optBoolean("isRposted");

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
                                        if (theBridge.height != -99) {
                                            heightET.setText(theBridge.height.toString());
                                        }

                                        setEditTextAsTextView(otherET);

                                        if (theBridge.otherPosting != null && !theBridge.otherPosting.toUpperCase().equals("NULL")) {
                                            otherET.setText(theBridge.otherPosting);
                                        }

                                        isRET.setChecked(theBridge.isRPosted);
                                        isRET.setEnabled(false);

                                    } catch (Exception ex) {
                                        Toast.makeText(getBaseContext(), "Error retrieving info, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } // end response success
                    else   // unsuccessful response
                    {
                        if (response.code() == 400 || response.code() == 401) // received a response from server
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        return;
                    }

                }
            });

        } else  // no token found
        {
            finish(); // logout
        }

    }


    void clearBridgeValues() {
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


    void setEditTextAsTextView(EditText et) {
        et.setCursorVisible(false);
        et.setLongClickable(false);
        et.setClickable(false);
        et.setFocusable(false);
        et.setSelected(false);
        et.setKeyListener(null);
        et.setOnTouchListener(otl);
    }

    private View.OnTouchListener otl = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.noBridgeButton:  // no bridge pressed

                noBridgePressed();
                break;

            case R.id.wrongInfoButton:  // wrong info pressed

                wrongInfoPressed();
                break;

            case R.id.editButton:

                if (isOkToEditOrRemove())
                {
                    editBridge();
                }
                else
                {
                    Toast.makeText(this, "Edit Bridge Failed, Bridge must have at least 3 down votes to be edited", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.removeButton:

                if (isOkToEditOrRemove())
                {
                    removeBridge();
                }
                else
                {
                    Toast.makeText(this, "Remove Bridge Failed, Bridge must have at least 3 down votes to be removed", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private Boolean isOkToEditOrRemove()
    {
        Integer count = 0;

        if (noBridge1.getVisibility() == View.VISIBLE) { count += 1; }
        if (noBridge2.getVisibility() == View.VISIBLE) { count += 1; }
        if (noBridge3.getVisibility() == View.VISIBLE) { count += 1; }
        if (editBridge1.getVisibility() == View.VISIBLE) { count += 1; }
        if (editBridge2.getVisibility() == View.VISIBLE) { count += 1; }
        if (editBridge3.getVisibility() == View.VISIBLE) { count += 1; }

        if (count >= 3)
        {
            return  true;
        }

        return false;
    }

    void removeBridge() {
        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/RemoveByLocation";

        String token = preferences.getString("access_token", "");  // get token

        if (token != "")  // token stored
        {
            String urlEncoded = Uri.encode(urlAsString);

            if (lat == -99 || lon == -99) {
                return;
            }

            urlAsString += "?lat=" + lat + "&lon=" + lon;

            RequestBody formBody = new FormBody.Builder().build();

            Request request = new Request.Builder()
                    .url(urlAsString)
                    .addHeader("Authorization", "Bearer " + token)
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
                            Toast.makeText(DetailActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

                        }
                    });
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()) {
                        try {
                            final JSONObject json = new JSONObject(mMessage);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        if (!json.isNull("isSuccess")) {
                                            Boolean success = json.getBoolean("isSuccess");

                                            if (success != true) {
                                                String message = json.optString("message", "Please Try Again");
                                                Toast.makeText(DetailActivity.this, "Remove Failed, " + message, Toast.LENGTH_SHORT).show();
                                            } else // success
                                            {
                                                Toast.makeText(DetailActivity.this, "Remove Successful, Bridge marked as inactive", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        } else {
                                            String message = json.optString("message", "Please Try Again");
                                            Toast.makeText(DetailActivity.this, "Remove Failed, " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception ex) {
                                        Toast.makeText(getBaseContext(), "Error removing bridge, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } // end response success
                    else   // unsuccessful response
                    {
                        if (response.code() == 400 || response.code() == 401) // received a response from server
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        return;
                    }

                }
            });

        } else  // no token found
        {
            finish(); // logout
        }

    }


    void editBridge() {

        theBridge.latitude = lat;
        theBridge.longitude = lon;

        Intent i = new Intent(this, EditBridgeActivity.class);
        i.putExtra("bridge", theBridge);
        startActivity(i);
    }

    void noBridgePressed() {
        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/DownVoteBridge";

        String token = preferences.getString("access_token", "");  // get token
        String userName = preferences.getString("userName", "");   // get user name

        if (token != "")  // token stored
        {
            if (lat == -99 || lon == -99 || bridgeId == -99) {
                return;
            }

            String urlEncoded = Uri.encode(urlAsString);

            urlAsString += "/?bridgeId=" + bridgeId + "&userName=" + userName + "&isEdit=false";

            RequestBody formBody = new FormBody.Builder().build();

            Request request = new Request.Builder()
                    .url(urlAsString)
                    .addHeader("Authorization", "Bearer " + token)
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
                            Toast.makeText(DetailActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

                        }
                    });
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()) {
                        try {
                            final JSONObject json = new JSONObject(mMessage);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        if (!json.isNull("isSuccess")) {
                                            Boolean success = json.getBoolean("isSuccess");

                                            if (success != true) {
                                                String message = json.optString("message", "Please Try Again");
                                                Toast.makeText(DetailActivity.this, "Down Vote Failed, " + message, Toast.LENGTH_SHORT).show();
                                            } else // success
                                            {
                                                addThumbsUp(false);
                                            }
                                        } else {
                                            String message = json.optString("message", "Please Try Again");
                                            Toast.makeText(DetailActivity.this, "Down Vote Failed, " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception ex) {
                                        Toast.makeText(getBaseContext(), "Error down voting bridge, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } // end response success
                    else   // unsuccessful response
                    {
                        if (response.code() == 400 || response.code() == 401) // received a response from server
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        return;
                    }

                }
            });

        } else  // no token found
        {
            finish(); // logout
        }

    }

    void wrongInfoPressed()
    {
        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/DownVoteBridge";

        String token = preferences.getString("access_token", "");  // get token
        String userName = preferences.getString("userName", "");   // get user name

        if (token != "")  // token stored
        {
            if (lat == -99 || lon == -99 || bridgeId == -99) {
                return;
            }

            String urlEncoded = Uri.encode(urlAsString);

            urlAsString += "/?bridgeId=" + bridgeId + "&userName=" + userName + "&isEdit=true"; // isEdit=true

            RequestBody formBody = new FormBody.Builder().build();

            Request request = new Request.Builder()
                    .url(urlAsString)
                    .addHeader("Authorization", "Bearer " + token)
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
                            Toast.makeText(DetailActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

                        }
                    });
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()) {
                        try {
                            final JSONObject json = new JSONObject(mMessage);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        if (!json.isNull("isSuccess")) {
                                            Boolean success = json.getBoolean("isSuccess");

                                            if (success != true) {
                                                String message = json.optString("message", "Please Try Again");
                                                Toast.makeText(DetailActivity.this, "Down Vote Failed, " + message, Toast.LENGTH_SHORT).show();
                                            } else // success
                                            {
                                                addThumbsUp(true);
                                            }
                                        } else {
                                            String message = json.optString("message", "Please Try Again");
                                            Toast.makeText(DetailActivity.this, "Down Vote Failed, " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception ex) {
                                        Toast.makeText(getBaseContext(), "Error down voting bridge, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } // end response success
                    else   // unsuccessful response
                    {
                        if (response.code() == 400 || response.code() == 401) // received a response from server
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        return;
                    }

                }
            });

        } else  // no token found
        {
            finish(); // logout
        }

    }


    void addThumbsUp(Boolean isEdit) {

        if (isEdit == false) {
            if (noBridge1.getVisibility() == View.GONE && noBridge2.getVisibility() == View.GONE && noBridge3.getVisibility() == View.GONE) {
                noBridge1.setVisibility(View.VISIBLE);
            } else if (noBridge1.getVisibility() == View.VISIBLE && noBridge2.getVisibility() == View.GONE && noBridge3.getVisibility() == View.GONE) {
                noBridge2.setVisibility(View.VISIBLE);
            } else if (noBridge1.getVisibility() == View.VISIBLE && noBridge2.getVisibility() == View.VISIBLE && noBridge3.getVisibility() == View.GONE) {
                noBridge3.setVisibility(View.VISIBLE);
            }
        } else {
            if (editBridge1.getVisibility() == View.GONE && editBridge2.getVisibility() == View.GONE && editBridge3.getVisibility() == View.GONE) {
                editBridge1.setVisibility(View.VISIBLE);
            } else if (editBridge1.getVisibility() == View.VISIBLE && editBridge2.getVisibility() == View.GONE && editBridge3.getVisibility() == View.GONE) {
                editBridge2.setVisibility(View.VISIBLE);
            } else if (editBridge1.getVisibility() == View.VISIBLE && editBridge2.getVisibility() == View.VISIBLE && editBridge3.getVisibility() == View.GONE) {
                editBridge3.setVisibility(View.VISIBLE);
            }
        }
    }

}
