package com.jdsv650.bware;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
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

            String urlEncoded = Uri.encode(urlAsString);

            RequestBody body = RequestBody.create(MEDIA_TYPE, urlEncoded);

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
                    .build();

            /*************
             *
             *
             *
             *
             * add ........................................
             *
             */

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

                    if (response.isSuccessful()) {
                        try {
                            final JSONObject json = new JSONObject(mMessage);
                            //final JSONArray jsonArray = new JSONArray(mMessage);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        theBridge.country = json.optString("Country");
                                        theBridge.city = json.optString("Township");  // city
                                        theBridge.state = json.optString("State");
                                        theBridge.county = json.optString("County");

                                        theBridge.locationDescription = json.optString("LocationDescription");
                                        theBridge.featureCarried = json.optString("FeatureCarried");
                                        theBridge.featureCrossed = json.optString("FeatureCrossed");
                                        theBridge.zip = json.optString("Zip");


                                        setEditTextAsTextView(countryET);
                                        countryET.setText(theBridge.country);

                                        setEditTextAsTextView(cityET);
                                        cityET.setText(theBridge.city);

                                        setEditTextAsTextView(stateET);
                                        stateET.setText(theBridge.state);

                                        setEditTextAsTextView(countyET);
                                        countyET.setText(theBridge.county);

                                        setEditTextAsTextView(locationET);
                                        locationET.setText(theBridge.locationDescription);

                                        setEditTextAsTextView(carriedET);
                                        carriedET.setText(theBridge.featureCarried);

                                        setEditTextAsTextView(crossedET);
                                        crossedET.setText(theBridge.featureCrossed);

                                        setEditTextAsTextView(zipET);
                                        zipET.setText(theBridge.zip);

                                        /*** Restricted ***/

                                        theBridge.weightStraight = json.optDouble("WeightStraight", -99.0);
                                        theBridge.weightStraight_TriAxle = json.optDouble("WeightStraight_TriAxle", -99.0);
                                        theBridge.weightCombo = json.optDouble("WeightCombination", -99.0);
                                        theBridge.weightDouble = json.optDouble("WeightDouble", -99.0);

                                        theBridge.height = json.optDouble("Height", -99.0);
                                        theBridge.otherPosting = json.optString("OtherPosting");
                                        theBridge.isRPosted = json.optBoolean("isRposted");



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

                                        otherET.setText(theBridge.otherPosting);
                                        isRSwitch.setChecked(theBridge.isRPosted);
                                        isRSwitch.setEnabled(false);


                                        /***
                                         * {"BridgeId":4,"BIN":"3329700","Latitude":43.231433040485619,"Longitude":-78.811738031051618,"FeatureCarried":"WILLOW ROAD","FeatureCrossed":"E B TWELVEMILE CK","LocationDescription":".1 MI SE OF SOUTH WILSON","State":"NY","County":"NIAGARA","Township":"WILSON TOWN","Zip":"","Country":"US","Height":null,"WeightStraight":15.0,"WeightStraight_TriAxle":null,"WeightCombination":null,"WeightDouble":null,"isRposted":false,"OtherPosting":"","DateCreated":"2015-08-20T19:31:00","DateModified":"2015-10-01T00:11:58","UserCreated":"jdsv650@yahoo.com","UserModified":"jdsv650@yahoo.com","NumberOfVotes":0,"User1Verified":null,"User2Verified":null,"User3Verified":null,"User1Reason":null,"User2Reason":null,"User3Reason":null,"isLocked":false,"isActive":true}
                                         */

                                        /**
                                         *  if let reason1 = data["User1Reason"] as? Bool
                                         {
                                         if !reason1 { self.thumb1.hidden = false }
                                         else { self.thumb1Edit.hidden = false }
                                         }

                                         if let reason2 = data["User2Reason"] as? Bool
                                         {
                                         if !reason2 { self.thumb2.hidden = false }
                                         else { self.thumb2Edit.hidden = false }
                                         }

                                         if let reason3 = data["User3Reason"] as? Bool
                                         {
                                         if !reason3 { self.thumb3.hidden = false }
                                         else { self.thumb3Edit.hidden = false }
                                         }

                                         if let bridgeId = data["BridgeId"] as? Int
                                         {
                                         self.bridgeId = bridgeId
                                         }

                                         if let weightStraight = data["WeightStraight"] as? Double
                                         {
                                         self.weightStraightTF.text = weightStraight.toString()
                                         self.theBridge.weightStraight = weightStraight
                                         }

                                         if let weightTri = data["WeightStraight_TriAxle"] as? Double
                                         {
                                         self.weightTriAxle.text = weightTri.toString()
                                         self.theBridge.weightStraight_TriAxle = weightTri
                                         }

                                         if let weightCombo = data["WeightCombination"] as? Double
                                         {
                                         self.weightComboTF.text = weightCombo.toString()
                                         self.theBridge.weightCombo = weightCombo
                                         }

                                         if let weightDouble = data["WeightDouble"] as? Double
                                         {
                                         self.weightDoubleTF.text = weightDouble.toString()
                                         self.theBridge.weightDouble = weightDouble
                                         }

                                         if let height = data["Height"] as? Double
                                         {
                                         self.heightTF.text = height.toString()
                                         self.theBridge.height = height
                                         }

                                         if let isR = data["isRposted"] as? Bool
                                         {
                                         self.isRSwitch.on = isR
                                         self.theBridge.isRPosted = isR
                                         }

                                         if let desc = data["LocationDescription"] as? String
                                         {
                                         self.descriptionTF.text = desc
                                         self.theBridge.locationDescription = desc
                                         }

                                         if let city = data["Township"] as? String
                                         {
                                         self.cityTF.text = city
                                         self.theBridge.city = city
                                         }

                                         if let state = data["State"] as? String
                                         {
                                         self.stateTF.text = state
                                         self.theBridge.state = state
                                         }

                                         if let zip = data["Zip"] as? String
                                         {
                                         self.zipTF.text = zip
                                         self.theBridge.zip = zip
                                         }

                                         if let country = data["Country"] as? String
                                         {
                                         self.CountryTF.text = country
                                         self.theBridge.country = country
                                         }

                                         if let other = data["OtherPosting"] as? String
                                         {
                                         self.otherPostingTF.text = other
                                         self.theBridge.otherPosting = other
                                         }

                                         if let carried = data["FeatureCarried"] as? String
                                         {
                                         self.carriedTF.text = carried
                                         self.theBridge.featureCarried = carried
                                         }

                                         if let crossed = data["FeatureCrossed"] as? String
                                         {
                                         self.crossedTF.text = crossed
                                         self.theBridge.featureCrossed = crossed
                                         }

                                         if let county = data["County"] as? String
                                         {
                                         self.countyTF.text = county
                                         self.theBridge.county = county
                                         }
                                         */

                                    } catch (Exception ex) {
                                        Toast.makeText(getBaseContext(), "Error logging in please try again", Toast.LENGTH_SHORT).show();
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
        //et.setBackgroundResource(android.R.color.transparent);
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

