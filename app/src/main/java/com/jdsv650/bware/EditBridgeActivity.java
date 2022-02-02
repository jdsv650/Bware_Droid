package com.jdsv650.bware;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
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

public class EditBridgeActivity extends AppCompatActivity implements View.OnClickListener {

    EditText countryET;
    EditText cityET;
    EditText stateET;
    EditText countyET;
    EditText locationET;
    EditText carriedET;
    EditText crossedET;
    EditText zipET;
    EditText straightET;
    EditText triET;
    EditText comboET;
    EditText doubleET;
    EditText heightET;
    EditText otherET;
    Switch isRSwitch;

    private SharedPreferences preferences;
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    OkHttpClient client;
    Bridge bridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bridge);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

        // get shared prefs
        preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        Intent i = getIntent();
        bridge = (Bridge) i.getExtras().getSerializable("bridge");

        countryET = (EditText) findViewById(R.id.edit_countryEditText);
        cityET = (EditText) findViewById(R.id.edit_cityEditText);
        stateET = (EditText) findViewById(R.id.edit_stateEditText);
        countyET = (EditText) findViewById(R.id.edit_countyEditText);
        locationET = (EditText) findViewById(R.id.edit_descriptionEditText);
        carriedET = (EditText) findViewById(R.id.edit_carriedEditText);
        crossedET = (EditText) findViewById(R.id.edit_crossedEditText);
        zipET = (EditText) findViewById(R.id.edit_zipEditText);
        straightET = (EditText) findViewById(R.id.edit_tandemEditText);
        triET = (EditText) findViewById(R.id.edit_triaxleEditText);
        comboET = (EditText) findViewById(R.id.edit_combinationEditText);
        doubleET = (EditText) findViewById(R.id.edit_doubleEditText);
        heightET = (EditText) findViewById(R.id.edit_heightEditText);
        otherET = (EditText) findViewById(R.id.edit_otherEditText);
        isRSwitch = (Switch) findViewById(R.id.edit_rSwitch);

        Button saveB = (Button) findViewById(R.id.edit_saveButton);
        saveB.setOnClickListener(this);

        populateTextFields();

    }

    void populateTextFields()
    {
        if (bridge == null || bridge.latitude == -99.0 || bridge.longitude == -99.0) { return; }

        if (bridge.country == null || bridge.country.toUpperCase().equals("NULL")) { countryET.setText(""); }
        else { countryET.setText(bridge.country); }

        if (bridge.city == null || bridge.city.toUpperCase().equals("NULL")) { cityET.setText(""); }
        else { cityET.setText(bridge.city); }

        if (bridge.state == null || bridge.state.toUpperCase().equals("NULL")) { stateET.setText(""); }
        else { stateET.setText(bridge.state);  }

        if (bridge.county == null || bridge.county.toUpperCase().equals("NULL")) { countyET.setText(""); }
        else { countyET.setText(bridge.county); }

        if (bridge.locationDescription == null || bridge.locationDescription.toUpperCase().equals("NULL")) { locationET.setText(""); }
        else { locationET.setText(bridge.locationDescription); }

        if (bridge.featureCarried == null || bridge.featureCarried.toUpperCase().equals("NULL")) { carriedET.setText(""); }
        else { carriedET.setText(bridge.featureCarried);  }

        if (bridge.featureCrossed == null || bridge.featureCrossed.toUpperCase().equals("NULL")) { crossedET.setText(""); }
        else { crossedET.setText(bridge.featureCrossed);  }

        if (bridge.zip== null || bridge.zip.toUpperCase().equals("NULL")) { zipET.setText(""); }
        else {  zipET.setText(bridge.zip);  }

        if (bridge.weightStraight == -99)
        {
            straightET.setText("");
            bridge.weightStraight = null;
        }
        else {
            straightET.setText(bridge.weightStraight.toString());
        }

        if (bridge.weightStraight_TriAxle == -99)
        {
            triET.setText("");
            bridge.weightStraight_TriAxle = null;
        }
        else {
            triET.setText(bridge.weightStraight_TriAxle.toString());
        }

        if (bridge.weightCombo == -99)
        {
            comboET.setText("");
            bridge.weightCombo = null;
        }
        else {
            comboET.setText(bridge.weightCombo.toString());
        }

        if (bridge.weightDouble == -99)
        {
            doubleET.setText("");
            bridge.weightDouble = null;
        }
        else {
            doubleET.setText(bridge.weightDouble.toString());
        }

        if (bridge.height == -99)
        {
            heightET.setText("");
            bridge.height = null;
        }
        else {
            heightET.setText(bridge.height.toString());
        }

        if (bridge.otherPosting == null || bridge.otherPosting.toUpperCase().equals("NULL")) { otherET.setText(""); }
        else { otherET.setText(bridge.otherPosting); }

        isRSwitch.setChecked(bridge.isRPosted);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.edit_saveButton:

                if (rewriteBridgeFromTextFields())
                {
                    saveBridgeData();
                }
                break;
        }
    }

    Double weightStraight;
    Double weightTri;
    Double weightCombo;
    Double weightDouble;
    Double height;

    private Boolean rewriteBridgeFromTextFields()
    {

        // At least one of the following must be set otherwise don't save
        if (straightET.getText().toString().equals("") && triET.getText().toString().equals("")
                && doubleET.getText().toString().equals("") && comboET.getText().toString().equals("")
                && heightET.getText().toString().equals("") && otherET.getText().toString().equals("")
                && isRSwitch.isChecked() == false)
        {
            Toast.makeText(this, "Error Creating Bridge , Please supply weight, height, other posting or set R posted switch", Toast.LENGTH_SHORT).show();
            return false;
        }

        try { weightStraight = Double.parseDouble(straightET.getText().toString()); }
        catch (NumberFormatException ex) { weightStraight = null; }

        try { weightTri = Double.parseDouble(triET.getText().toString()); }
        catch (NumberFormatException ex) { weightTri = null; }

        try { weightCombo = Double.parseDouble(comboET.getText().toString()); }
        catch (NumberFormatException ex) { weightCombo = null; }

        try { weightDouble = Double.parseDouble(doubleET.getText().toString()); }
        catch (NumberFormatException ex) { weightDouble = null; }

        try { height = Double.parseDouble(heightET.getText().toString()); }
        catch (NumberFormatException ex) { height = null; }

        if (weightStraight == null && weightTri == null && weightCombo == null
                && weightDouble == null && height == null && isRSwitch.isChecked() == false)
        {
            Toast.makeText(this, "Error Editing Bridge , Please supply valid values for weight or height", Toast.LENGTH_SHORT).show();
            return false;
        }

        bridge.weightStraight = weightStraight;
        bridge.weightStraight_TriAxle = weightTri;
        bridge.weightCombo =  weightCombo;
        bridge.weightDouble = weightDouble;
        bridge.height = height;
        bridge.isRPosted = isRSwitch.isChecked();
        bridge.otherPosting = otherET.getText().toString();

        if (bridge.weightStraight != null)
        {
            if (bridge.weightStraight < 0 || bridge.weightStraight > 100)
            {
                Toast.makeText(this, "Error Editing Bridge , Please supply a reasonable value for weight straight", Toast.LENGTH_SHORT).show();
                return false;  // test for input within range
            }
        }

        if (bridge.weightStraight_TriAxle != null)
        {
            if (bridge.weightStraight_TriAxle < 0 || bridge.weightStraight_TriAxle > 100)
            {
                Toast.makeText(this, "Error Editing Bridge , Please supply a reasonable value for weight straight triaxle", Toast.LENGTH_SHORT).show();
                return false;  // test for input within range
            }
        }

        if (bridge.weightDouble != null)
        {
            if (bridge.weightDouble < 0 || bridge.weightDouble > 100)
            {
                Toast.makeText(this, "Error Editing Bridge , Please supply a reasonable value for weight double", Toast.LENGTH_SHORT).show();
                return false;  // test for input within range
            }
        }

        if (bridge.weightCombo != null)
        {
            if (bridge.weightCombo < 0 || bridge.weightCombo > 100)
            {
                Toast.makeText(this, "Error Editing Bridge , Please supply a reasonable value for weight combo", Toast.LENGTH_SHORT).show();
                return false;  // test for input within range
            }
        }

        if (bridge.height != null)
        {
            if (bridge.height < 0 || bridge.height > 100)
            {
                Toast.makeText(this, "Error Editing Bridge , Please supply a reasonable value for height", Toast.LENGTH_SHORT).show();
                return false;  // test for input within range
            }
        }

        if(countryET.getText().toString().equals("") || stateET.getText().toString().equals("") ||
                countyET.getText().toString().equals("") )
        {
            Toast.makeText(this, "Error Editing Bridge , Please supply country, state and county", Toast.LENGTH_SHORT).show();
            return false;  // check for country state and county - required
        }

        // location info 8 fields
        // these 3 required
        bridge.country = countryET.getText().toString();
        bridge.city = cityET.getText().toString();
        bridge.state = stateET.getText().toString();
        // 5 optional
        bridge.county = countyET.getText().toString();
        bridge.locationDescription = locationET.getText().toString();
        bridge.featureCarried = carriedET.getText().toString();
        bridge.featureCrossed = crossedET.getText().toString();
        bridge.zip = zipET.getText().toString();

        bridge.isLocked = false;
        bridge.numVotes = 0;

       return true;
    }

    private void saveBridgeData()
    {
        String lat = bridge.latitude.toString();
        String lon = bridge.longitude.toString();

        String urlAsString = Constants.baseUrlAsString + "/Api/Bridge/Update";
        String token = preferences.getString("access_token", "");  // get stored token
        String userName = preferences.getString("userName", "unknown"); // get username

        if (token != "") {  // stored bearer token

            DateFormat df = DateFormat.getTimeInstance();
            df.setTimeZone(TimeZone.getTimeZone("gmt"));
            String gmtTime = df.format(new Date());
            //Toast.makeText(this, gmtTime, Toast.LENGTH_SHORT).show();
            // From ios app we want the following format - "2014-07-23 18:01:41 +0000" in UTC

            String isR = bridge.isRPosted ? "true" : "false";

            RequestBody formBody = new FormBody.Builder()
                    .add("BridgeId", "100")
                    .add("Latitude", lat)
                    .add("Longitude", lon)
                    .add("DateModified", gmtTime)
                    .add("UserModified", userName)
                    .add("NumberOfVotes", "0")
                    .add("isLocked", "true")
                    .add("FeatureCarried", bridge.featureCarried)
                    .add("FeatureCrossed", bridge.featureCrossed)
                    .add("LocationDescription", bridge.locationDescription)
                    .add("State", bridge.state)
                    .add("County", bridge.county)
                    .add("Township", bridge.city)
                    .add("Zip", bridge.zip)
                    .add("Country", bridge.country)
                    .add("WeightStraight", straightET.getText().toString())
                    .add("WeightStraight_TriAxle", triET.getText().toString())
                    .add("WeightDouble", doubleET.getText().toString())
                    .add("WeightCombination", comboET.getText().toString())
                    .add("Height", heightET.getText().toString())
                    .add("OtherPosting", bridge.otherPosting)
                    .add("isRposted", isR)
                    .build();

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
                            Toast.makeText(EditBridgeActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

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

                            Boolean isSuccess = json.getBoolean("isSuccess");
                            if (!isSuccess)
                            {
                                // message may include "Name xxxx@xxxxx.com is already taken"
                                final String message = json.optString("message","Error Saving Bridge. Please try again");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                            else // ok success
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(getBaseContext(), "Bridge Info Saved", Toast.LENGTH_SHORT).show();
                                        try {
                                            wait(1000);
                                        }
                                        catch (Exception ex) {  }
                                        finally {

                                            finish();
                                        }

                                    }
                                });

                            }

                        } catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(), "Error Saving Bridge. Please try again", Toast.LENGTH_SHORT).show();
                        }

                    } // end response success
                    else   // unsuccessful response
                    {
                        if (response.code() == 400 || response.code() == 401) // received a response from server
                        {
                            finish();  // not authorized
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Error Saving Bridge. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                }
            });

        } else  // no token found
        {
            finish();
        }


    }
}
