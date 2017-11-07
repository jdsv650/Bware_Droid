package com.jdsv650.bware;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;


import static android.content.Context.MODE_PRIVATE;
import static com.jdsv650.bware.Constants.PREFS_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements LocationListener, OnMapReadyCallback {

    MapFragment mMapFragment;

    private GoogleMap gMap;
    MapView mMapView;
    View mView;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    LocationManager locManager;
    private static final Integer fiveMilesInMeters = 8047;
    private static final Integer sixtySeconds = 60000;
    private static final Integer zoom = 10;
    private SharedPreferences preferences;

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");


    Double geographicCenterUSLat = 39.833333;
    Double geographicCenterUSLon = -98.583333;
    Integer numMilesToSearch = 50;  // can be changed by preference

    OkHttpClient client;



    public MapFragment() {
        // Required empty public constructor

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(30, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(30, TimeUnit.SECONDS)
                .build();


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapsInitializer.initialize(getActivity().getApplicationContext());

        // get shared prefs
        preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) mView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        /***
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

            }
        });  ***/

        return mView;
    }


    @Override
    public void onLocationChanged(Location location) {

        if (gMap == null)
        {
            return;
        }
       // Toast.makeText(getActivity(), "Location: Lat = " + location.getLatitude() + "  Lon = " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        updateLocationUI(location.getLatitude(), location.getLongitude());
        getBridgeData(location, 50);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(getActivity(), "Getting updates ...", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(getActivity(), "Please enable location services", Toast.LENGTH_SHORT).show();

    }


    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

            Integer minute = 1000 * 60;

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0 , this);

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
            mLocationPermissionGranted = false;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION : {
                // request cancelled, result array is empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // OK
                    mLocationPermissionGranted = true;

                } else {

                    Toast.makeText(getActivity(),"", Toast.LENGTH_LONG).show();
                    mLocationPermissionGranted = false;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void updateLocationUI(double lat, double lon) {
        if (gMap == null) {
            return;
        }

        try {

            if (mLocationPermissionGranted == true) {
                gMap.setMyLocationEnabled(true);
                gMap.getUiSettings().setMyLocationButtonEnabled(true);
                gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Marker in ????" ));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom));

            } else {
                gMap.setMyLocationEnabled(false);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getBridgeData(Location location, Integer miles)
    {

        Double lat = location.getLatitude();
        Double lon = location.getLongitude();

        String urlAsString = "https://www.bwaremap.com" + "/api/Bridge/GetByMiles?lat=" + lat
                                + "&lon=" +lon + "&miles=" +miles;


        String token = preferences.getString("access_token","");  // is token stored

        if (token != "")
        {
            String urlEncoded = Uri.encode(urlAsString);
            RequestBody body = RequestBody.create(MEDIA_TYPE, urlEncoded);

            Request request = new Request.Builder()
                    .url(urlAsString)
                    .addHeader("Authorization", "Bearer " + token)
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
                           // final JSONObject json = new JSONObject(mMessage);
                            final JSONArray jsonArray = new JSONArray(mMessage);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try
                                    {
                                        drawMap(jsonArray);
                                    }
                                    catch (Exception ex)
                                    {
                                        Toast.makeText(getActivity(), "Error logging in please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            // go to map -- THIS IS THE ROOT OF ALL EVIL ----- why was it here to begin with ?????
                           // Intent intent = new Intent(getActivity(), BottomNavigationActivity.class);
                           // startActivity(intent);

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
                                    Toast.makeText(getActivity(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getActivity(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
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
            getActivity().finish(); // logout
        }

    }


    private void drawMap(JSONArray json)
    {
        gMap.clear();

        for (Integer i = 0; i < json.length(); i++) {
            try {
                final Double lat = json.getJSONObject(i).getDouble("Latitude");
                Log.i("JSON = ", lat.toString());

                final Double lon = json.getJSONObject(i).getDouble("Longitude");
                Log.i("JSON = ", lon.toString());

                if (lat < -90 || lat > 90) { continue; }
                if (lon < -180 || lon > 180) { continue; }

                /***
                 *  let height = b["Height"] as? Double
                 if height != nil
                 {
                 marker.icon = UIImage(named: "marker_height2_orange.png")
                 }
                 else
                 {
                 marker.icon = UIImage(named: "marker_weight2_orange.png")
                 }
                 */

                gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Marker in Lockport ?????"));


            }
            catch (Exception ex)
            {

            }

        }

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


    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (gMap == null)
        {
            gMap = googleMap;
        }

        // get location
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        getLocationPermission();

        // For showing a move to my location button
        //gMap.setMyLocationEnabled(true);

        // For dropping a marker at a point on the Map
        LatLng sydney = new LatLng(43.171395, -78.679584);
        gMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(zoom).build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        updateLocationUI(43.171395, -78.679584);

    }


}
