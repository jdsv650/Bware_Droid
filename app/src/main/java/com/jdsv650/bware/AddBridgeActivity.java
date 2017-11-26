package com.jdsv650.bware;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
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

    EditText countryET = (EditText) findViewById(R.id.add_countryEditText);
    EditText cityET = (EditText) findViewById(R.id.add_cityEditText);
    EditText stateET = (EditText) findViewById(R.id.add_stateEditText);
    EditText countyET = (EditText) findViewById(R.id.add_countyEditText);
    EditText locationET = (EditText) findViewById(R.id.add_descriptionEditText);
    EditText carriedET = (EditText) findViewById(R.id.add_carriedEditText);
    EditText crossedET = (EditText) findViewById(R.id.add_crossedEditText);
    EditText zipET = (EditText) findViewById(R.id.add_zipEditText);
    EditText straightET = (EditText) findViewById(R.id.add_tandemEditText);
    EditText triET = (EditText) findViewById(R.id.add_triaxleEditText);
    EditText comboET = (EditText) findViewById(R.id.add_combinationEditText);
    EditText doubleET = (EditText) findViewById(R.id.add_doubleEditText);
    EditText heightET = (EditText) findViewById(R.id.add_heightEditText);
    EditText otherET = (EditText) findViewById(R.id.add_otherEditText);
    Switch isRSwitch = (Switch) findViewById(R.id.add_rSwitch);


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

        Button button = (Button) findViewById(R.id.add_submit_button);
        button.setOnClickListener(this);

        lat = getIntent().getExtras().getDouble("latitude");
        lon = getIntent().getExtras().getDouble("longitude");

        clearBridgeValues();
        reverseLookup();

    }


    private void addBridgeData(Bridge bridge) {
        String lat = bridge.latitude.toString();
        String lon = bridge.longitude.toString();

        String urlAsString = Constants.baseUrlAsString + "/Api/Bridge/Create";
        String token = preferences.getString("access_token", "");  // is token stored
        String userName = preferences.getString("userName", "unknown"); // get username

        if (token != "") {

            DateFormat df = DateFormat.getTimeInstance();
            df.setTimeZone(TimeZone.getTimeZone("gmt"));
            String gmtTime = df.format(new Date());

            Toast.makeText(this, gmtTime, Toast.LENGTH_SHORT).show();
            // From ios app we want the following format - "2014-07-23 18:01:41 +0000" in UTC

            // String urlEncoded = Uri.encode(urlAsString);
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
                    .add("WeightStraight", bridge.weightStraight.toString())
                    .add("WeightStraight_TriAxle", bridge.weightStraight_TriAxle.toString())
                    .add("WeightDouble", bridge.weightDouble.toString())
                    .add("WeightCombination", bridge.weightCombo.toString())
                    .add("Height", bridge.height.toString())
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


    private View.OnTouchListener otl = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add_submit_button:

                //Toast.makeText(this, "Adding Bridge", Toast.LENGTH_SHORT).show();
                Bridge bridge = new Bridge();

                if (lat == -99 || lon == -99)
                {
                    Toast.makeText(this, "Error Creating Bridge, Please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                bridge.latitude = lat;
                bridge.longitude = lon;

                // At least one of the following must be set otherwise don't save
                if (straightET.getText().toString() == "" && triET.getText().toString() == ""
                        && doubleET.getText().toString() == "" && comboET.getText().toString() == ""
                        && heightET.getText().toString() == "" && otherET.getText().toString() == ""
                        && isRSwitch.isChecked() == false)
                {
                    Toast.makeText(this, "Error Creating Bridge , Please supply weight, height, other posting or set R posted switch", Toast.LENGTH_SHORT).show();
                    return;
                }

                bridge.weightStraight = Double.parseDouble(straightET.getText().toString());
                bridge.weightStraight_TriAxle = Double.parseDouble(triET.getText().toString());
                bridge.weightCombo =  Double.parseDouble(comboET.getText().toString());
                bridge.weightDouble = Double.parseDouble(doubleET.getText().toString());
                bridge.height = Double.parseDouble(heightET.getText().toString());
                bridge.isRPosted = isRSwitch.isChecked();
                bridge.otherPosting = otherET.getText().toString();

                if (bridge.weightStraight < 0 || bridge.weightStraight > 100 ||
                    bridge.weightStraight_TriAxle < 0 || bridge.weightStraight_TriAxle > 100 ||
                    bridge.weightDouble < 0 || bridge.weightDouble > 100    ||
                    bridge.weightCombo < 0 || bridge.weightCombo > 100 ||
                    bridge.height < 0 || bridge.height > 22)
                {
                    Toast.makeText(this, "Error Creating Bridge , Please supply reasonable values for weight or height", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(countryET.getText().toString() == "" || stateET.getText().toString() == "" ||
                        countyET.getText().toString() == "")
                {
                    Toast.makeText(this, "Error Creating Bridge , Please supply country, state and county", Toast.LENGTH_SHORT).show();
                    return;
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

    void reverseLookup()
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

