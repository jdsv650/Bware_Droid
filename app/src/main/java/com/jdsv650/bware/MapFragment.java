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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
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

public class MapFragment extends Fragment implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener, BottomNavigationActivity.UpdatedBridgeListListener, View.OnClickListener {

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
    Boolean displayDensity = false; // can be changed by preference

    OkHttpClient client;


    public MapFragment() {
        // Required empty public constructor

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapsInitializer.initialize(getActivity().getApplicationContext());

        // get shared prefs
        preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        displayDensity = preferences.getBoolean("displayDensity", false);
    }

    @Override
    public void onResume() {
        super.onResume();

        /***
        if (((BottomNavigationActivity) getActivity()).getSearch() == true)
        {
            Toast.makeText(getActivity(), "SEARCH IS TRUE", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getActivity(), "SEARCH IS FALSE", Toast.LENGTH_SHORT).show();

        } ****/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) mView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        ImageButton imgButton = (ImageButton) mView.findViewById(R.id.imageButton);
        imgButton.setOnClickListener(this);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

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

        // use saved distance to get list of bridges
        Integer distance = preferences.getInt("distance", 50);
        getBridgeData(location, distance);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

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

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, sixtySeconds * 3, fiveMilesInMeters, this);
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, sixtySeconds * 3, fiveMilesInMeters , this);

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
        Double lat;
        Double lon;

        if (location == null) {

            lat = geographicCenterUSLat;
            lon = geographicCenterUSLon;
        }
        else
        {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/GetByMiles?lat=" + lat
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

            OkHttpClient trustAllclient = Helper.trustAllSslClient(client);

            trustAllclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String mMessage = e.getMessage().toString();
                    Log.w("failure Response", mMessage);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()){
                        try {
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
                                        Toast.makeText(getActivity(), "Error... please try again", Toast.LENGTH_SHORT).show();
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

        preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        displayDensity = preferences.getBoolean("displayDensity", false);

        if (displayDensity == true) {
            gMap.setTrafficEnabled(true);
        }
        else
        {
            gMap.setTrafficEnabled(false);
        }

        for (Integer i = 0; i < json.length(); i++) {
            try {
                final Double lat = json.getJSONObject(i).getDouble("Latitude");
                Log.i("JSON = ", lat.toString());

                final Double lon = json.getJSONObject(i).getDouble("Longitude");
                Log.i("JSON = ", lon.toString());

                if (lat < -90 || lat > 90) { continue; }
                if (lon < -180 || lon > 180) { continue; }

                Double height;
                height = json.getJSONObject(i).optDouble("Height", -99.0);
                BitmapDescriptor bmpDesc;

                if (height == -99.0) // No height restriction found
                {
                    bmpDesc = BitmapDescriptorFactory.fromResource(R.drawable.marker_bridge_orange);
                }
                else
                {
                    bmpDesc = BitmapDescriptorFactory.fromResource(R.drawable.marker_height_orange);
                }

                gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).icon(bmpDesc));

            }
            catch (Exception ex)
            {

            }

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (gMap == null)
        {
            gMap = googleMap;
        }

        gMap.setOnMarkerClickListener(this);
        gMap.setOnMapLongClickListener(this);

        // get location
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        getLocationPermission();

        // For dropping a marker at a point on the Map
        LatLng sydney = new LatLng(geographicCenterUSLat, geographicCenterUSLon);

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(zoom).build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        updateLocationUI(geographicCenterUSLat, geographicCenterUSLon);

    }


    @Override
    public void onMapLongClick(LatLng latLng) {

        Intent i = new Intent(getActivity(), AddBridgeActivity.class);
        i.putExtra("latitude", latLng.latitude);
        i.putExtra("longitude", latLng.longitude);

        startActivity(i);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Intent i = new Intent(getActivity(), DetailActivity.class);
        i.putExtra("latitude", marker.getPosition().latitude);
        i.putExtra("longitude", marker.getPosition().longitude);

        startActivity(i);

        return true;
    }

    @Override
    public void onFinishUpdatingBridges(ArrayList<Bridge> bridges) {

        drawMapFromBridgeList(bridges);

    }


    private void drawMapFromBridgeList(ArrayList<Bridge> bridges)
    {
        gMap.clear();

        for (Integer i = 0; i < bridges.size(); i++) {
            try {
                final Double lat = bridges.get(i).latitude;
                Log.i("LAT = ", lat.toString());

                final Double lon = bridges.get(i).longitude;
                Log.i("LON = ", lon.toString());

                if (lat < -90 || lat > 90) { continue; }
                if (lon < -180 || lon > 180) { continue; }

                if(i == 0) {  // set camera zoom off of one of the bridges
                    LatLng cameraCenter = new LatLng(lat, lon);
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(cameraCenter).zoom(zoom).build();
                    gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                Double height = bridges.get(i).height;
                BitmapDescriptor bmpDesc;

                if (height == -99.0) // No height restriction found
                {
                    bmpDesc = BitmapDescriptorFactory.fromResource(R.drawable.marker_bridge_orange);
                }
                else
                {
                    bmpDesc = BitmapDescriptorFactory.fromResource(R.drawable.marker_height_orange);
                }

                gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).icon(bmpDesc));

            }
            catch (Exception ex)
            {

            }

        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageButton:

                Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
                if (locManager != null)
                {
                    if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        locManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                    }

                    Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    Integer distance = preferences.getInt("distance", 50);
                    getBridgeData(loc, distance);

                }
                else
                {
                    Toast.makeText(getActivity(), "Please allow location services ...", Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }
}
