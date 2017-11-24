package com.jdsv650.bware;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import static android.content.Context.MODE_PRIVATE;
import static com.jdsv650.bware.Constants.PREFS_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences preferences;
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    OkHttpClient client;
    Bridge[] bridges = new Bridge[1000];

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        Button button = (Button) view.findViewById(R.id.searchSubmitPressed);
        button.setOnClickListener(this);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

        // get shared prefs
        preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.searchSubmitPressed:

                performSearch();
                break;
        }

    }

    private void performSearch()
    {
        // required
        EditText countryET = (EditText) getView().findViewById(R.id.searchCountryET);
        EditText stateET = (EditText) getView().findViewById(R.id.searchStateET);
        EditText countyET = (EditText) getView().findViewById(R.id.searchCountyET);

        // optional
        EditText cityET = (EditText) getView().findViewById(R.id.searchCityET);

        String country = countryET.getText().toString();
        String state = stateET.getText().toString();
        String county = countyET.getText().toString();
        String city = cityET.getText().toString();

        if (country.isEmpty() || state.isEmpty() || county.isEmpty())
        {
            Toast.makeText(getActivity(), "Required fields must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // ok make api call

        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/GetByInfo";

        String token = preferences.getString("access_token","");  // is token stored

        if (token != "")
        {
            /*
             * {"isSuccess":true,"message":"Success","data":null,"multipleData":[{"BridgeId":1,"BIN":"3329720","Latitude":43.308770030182,"Longitude":-78.715318945470116,"FeatureCarried":"WILSON-BURT ROAD","FeatureCrossed":"EIGHTEEN MILE CRK","LocationDescription":"1.8 MI S JCT SH18 & SH78","State":"NY","County":"NIAGARA","Township":"NEWFANE TOWN","Zip":"","Country":"US","Height":null,"WeightStraight":20.0,"WeightStraight_TriAxle":null,"WeightCombination":null,"WeightDouble":null,"isRposted":false,"OtherPosting":"","DateCreated":"2015-08-20T19:31:00","DateModified":"2015-08-20T19:31:00","UserCreated":"jdsv650@yahoo.com","UserModified":"jdsv650@yahoo.com","N
             */

            String parameters = "/?country=" + country.toUpperCase() + "&state=" + state.toUpperCase()
                                + "&county=" + county.toUpperCase() + "&town=" + city.toUpperCase();

            urlAsString += parameters;
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
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()){
                        try {
                            final JSONObject json = new JSONObject(mMessage);
                            final JSONArray jsonArray = json.getJSONArray("multipleData");

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try
                                    {
                                        Toast.makeText(getActivity(), "OK PARSE ME.....", Toast.LENGTH_SHORT).show();


                                        //theBridge.country = json.optString("Country");

                                        BottomNavigationView navigation = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
                                        navigation.setSelectedItemId(R.id.navigation_home);

                                    }
                                    catch (Exception ex)
                                    {
                                        Toast.makeText(getContext(), "Error retrieving bridge info - try again", Toast.LENGTH_SHORT).show();
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getContext(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getContext(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "User Not Found", Toast.LENGTH_SHORT).show();
            getActivity().finish(); // logout
        }



    }

}
