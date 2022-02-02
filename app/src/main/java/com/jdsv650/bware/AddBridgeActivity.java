package com.jdsv650.bware;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

public class AddBridgeActivity extends AppCompatActivity implements View.OnClickListener {

    Double lat = -99.0;
    Double lon = -99.0;

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
    Bridge theBridge = new Bridge();

    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bridge);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

        // get shared prefs
        preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        countryET = (EditText) findViewById(R.id.add_countryEditText);
        cityET = (EditText) findViewById(R.id.add_cityEditText);
        stateET = (EditText) findViewById(R.id.add_stateEditText);
        countyET = (EditText) findViewById(R.id.add_countyEditText);
        locationET = (EditText) findViewById(R.id.add_descriptionEditText);
        carriedET = (EditText) findViewById(R.id.add_carriedEditText);
        crossedET = (EditText) findViewById(R.id.add_crossedEditText);
        zipET = (EditText) findViewById(R.id.add_zipEditText);
        straightET = (EditText) findViewById(R.id.add_tandemEditText);
        triET = (EditText) findViewById(R.id.add_triaxleEditText);
        comboET = (EditText) findViewById(R.id.add_combinationEditText);
        doubleET = (EditText) findViewById(R.id.add_doubleEditText);
        heightET = (EditText) findViewById(R.id.add_heightEditText);
        otherET = (EditText) findViewById(R.id.add_otherEditText);
        isRSwitch = (Switch) findViewById(R.id.add_rSwitch);


        Button button = (Button) findViewById(R.id.add_submit_button);
        button.setOnClickListener(this);

        lat = getIntent().getExtras().getDouble("latitude");
        lon = getIntent().getExtras().getDouble("longitude");

        clearBridgeValues();
        reverseLookup();
    }


    // make api call to bridge/create
    private void addBridgeData(Bridge bridge) {
        String lat = bridge.latitude.toString();
        String lon = bridge.longitude.toString();

        String urlAsString = Constants.baseUrlAsString + "/Api/Bridge/Create";
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
                    .add("DateCreated", gmtTime)
                    .add("DateModified", gmtTime)
                    .add("UserCreated", userName)
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
                            Toast.makeText(AddBridgeActivity.this, "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

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
                            else // registered user OK
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(getBaseContext(), "Bridge Created", Toast.LENGTH_SHORT).show();
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


    private View.OnTouchListener otl = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    Double weightStraight;
    Double weightTri;
    Double weightCombo;
    Double weightDouble;
    Double height;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add_submit_button:   // submit pressed so build a bridge object to pass

                Bridge bridge = new Bridge();

                if (lat == -99 || lon == -99)
                {
                    Toast.makeText(this, "Error Creating Bridge, Please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                bridge.latitude = lat;
                bridge.longitude = lon;

                // At least one of the following must be set otherwise don't save
                if (straightET.getText().toString().equals("") && triET.getText().toString().equals("")
                        && doubleET.getText().toString().equals("") && comboET.getText().toString().equals("")
                        && heightET.getText().toString().equals("") && otherET.getText().toString().equals("")
                        && isRSwitch.isChecked() == false)
                {
                    Toast.makeText(this, "Error Creating Bridge , Please supply weight, height, other posting or set R posted switch", Toast.LENGTH_SHORT).show();
                    return;
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
                    Toast.makeText(this, "Error Creating Bridge , Please supply valid values for weight or height", Toast.LENGTH_SHORT).show();
                    return;  // test for malformed input
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
                        Toast.makeText(this, "Error Creating Bridge , Please supply a reasonable value for weight straight", Toast.LENGTH_SHORT).show();
                        return;  // test for input within range
                    }
                }

                if (bridge.weightStraight_TriAxle != null)
                {
                    if (bridge.weightStraight_TriAxle < 0 || bridge.weightStraight_TriAxle > 100)
                    {
                        Toast.makeText(this, "Error Creating Bridge , Please supply a reasonable value for weight straight triaxle", Toast.LENGTH_SHORT).show();
                        return;  // test for input within range
                    }
                }

                if (bridge.weightDouble != null)
                {
                    if (bridge.weightDouble < 0 || bridge.weightDouble > 100)
                    {
                        Toast.makeText(this, "Error Creating Bridge , Please supply a reasonable value for weight double", Toast.LENGTH_SHORT).show();
                        return;  // test for input within range
                    }
                }

                if (bridge.weightCombo != null)
                {
                    if (bridge.weightCombo < 0 || bridge.weightCombo > 100)
                    {
                        Toast.makeText(this, "Error Creating Bridge , Please supply a reasonable value for weight combo", Toast.LENGTH_SHORT).show();
                        return;  // test for input within range
                    }
                }

                if (bridge.height != null)
                {
                    if (bridge.height < 0 || bridge.height > 100)
                    {
                        Toast.makeText(this, "Error Creating Bridge , Please supply a reasonable value for height", Toast.LENGTH_SHORT).show();
                        return;  // test for input within range
                    }
                }

                if(countryET.getText().toString().equals("") || stateET.getText().toString().equals("") ||
                        countyET.getText().toString().equals("") )
                {
                    Toast.makeText(this, "Error Creating Bridge , Please supply country, state and county", Toast.LENGTH_SHORT).show();
                    return;  // check for country state and county - required
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

                addBridgeData(bridge);

                break;
        }

    }

    void reverseLookup()   // reverse geocode to pre-populate some of the text boxes
    {
        EditText cityET = (EditText) findViewById(R.id.add_cityEditText);
        EditText stateET = (EditText) findViewById(R.id.add_stateEditText);
        EditText descET = (EditText) findViewById(R.id.add_descriptionEditText);
        EditText zipET = (EditText) findViewById(R.id.add_zipEditText);
        EditText countryET = (EditText) findViewById(R.id.add_countryEditText);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try
        {
            List<Address> addressList = geocoder.getFromLocation(lat, lon, 1);

            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                // sending back first address line and locality
                String city = address.getLocality();
                String state = getStateByName(address.getAdminArea());  // convert New York -> NY
                String desc = address.getThoroughfare();
                String zip = address.getPostalCode();
                String country = getCountryByName(address.getCountryName()); // convert United States -> US

                cityET.setText(city);
                stateET.setText(state);
                descET.setText(desc);
                zipET.setText(zip);
                countryET.setText(country);

                /* String result = "City = " + city + " State = " + state + " desc = " + desc
                        + " zip = " + zip + " country = " + country;
                Toast.makeText(this, "REVERSE GEOCODE = " + result, Toast.LENGTH_LONG).show(); */
            }
        }
        catch (Exception ex)
        {
            // Couldn't reverse geocode; just ignore - let user fill in all fields
        }
    }

    String getStateByName(String name)
    {
        switch (name.toUpperCase())
        {
            case "ALABAMA":
                return "AL";
            case "ALASKA":
                return "AK";
            case "ARIZONA":
                return "AZ";
            case "ARKANSAS":
                return "AR";
            case "CALIFORNIA":
                return "CA";
            case "COLORADO":
                return "CO";
            case "CONNECTICUT":
                return "CT";
            case "DELAWARE":
                return "DE";
            case "DISTRICT OF COLUMBIA":
                return "DC";
            case "FLORIDA":
                return "FL";
            case "GEORGIA":
                return "GA";
            case "HAWAII":
                return "HI";
            case "IDAHO":
                return "ID";
            case "ILLINOIS":
                return "IL";
            case "INDIANA":
                return "IN";
            case "IOWA":
                return "IA";
            case "KANSAS":
                return "KS";
            case "KENTUCKY":
                return "KY";
            case "LOUISIANA":
                return "LA";
            case "MAINE":
                return "ME";
            case "MARYLAND":
                return "MD";
            case "MASSACHUSETTS":
                return "MA";
            case "MICHIGAN":
                return "MI";
            case "MINNESOTA":
                return "MN";
            case "MISSISSIPPI":
                return "MS";
            case "MISSOURI":
                return "MO";
            case "MONTANA":
                return "MT";
            case "NEBRASKA":
                return "NE";
            case "NEVADA":
                return "NV";
            case "NEW HAMPSHIRE":
                return "NH";
            case "NEW JERSEY":
                return "NJ";
            case "NEW MEXICO":
                return "NM";
            case "NEW YORK":
                return "NY";
            case "NORTH CAROLINA":
                return "NC";
            case "NORTH DAKOTA":
                return "ND";
            case "OHIO":
                return "OH";
            case "OKLAHOMA":
                return "OK";
            case "OREGON":
                return "OR";
            case "PENNSYLVANIA":
                return "PA";
            case "RHODE ISLAND":
                return "RI";
            case "SOUTH CAROLINA":
                return "SC";
            case "SOUTH DAKOTA":
                return "SD";
            case "TENNESSEE":
                return "TN";
            case "TEXAS":
                return "TX";
            case "UTAH":
                return "UT";
            case "VERMONT":
                return "VT";
            case "VIRGINIA":
                return "VA";
            case "WASHINGTON":
                return "WA";
            case "WEST VIRGINIA":
                return "WV";
            case "WISCONSIN":
                return "WI";
            case "WYOMING":
                return "WY";
            default:
                return "";
        }
    }

    String getCountryByName(String name)
    {
        switch (name.toUpperCase())
        {
            case "UNITED STATES":
                return "US";
            case "CANADA":
                return "CA";
            case "MEXICO":
                return "MX";
            default:
                return "";
        }

    }

}

