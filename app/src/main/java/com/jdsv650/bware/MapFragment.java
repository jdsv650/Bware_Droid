package com.jdsv650.bware;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import java.util.Map;

import okhttp3.OkHttpClient;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements LocationListener {

    MapFragment mMapFragment;

    private GoogleMap gMap;
    MapView mMapView;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    LocationManager locManager;
    private static final Double fiveMilesInMeters = 8046.72;
    private static final Integer thirtySeconds = 30000;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
       // SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
               //.findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);

       // OkHttpClient client = new OkHttpClient();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);


        mMapView.onCreate(savedInstanceState);

       mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                gMap = mMap;

                // get location
                locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                getLocationPermission();

                // For showing a move to my location button
               // googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng sydney = new LatLng(43.171395, -78.679584);
                gMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                updateLocationUI(43.171395, -78.679584);
            }
        });


        return rootView;
    }


    @Override
    public void onLocationChanged(Location location) {

       // Toast.makeText(getActivity(), "Location: Lat = " + location.getLatitude() + "  Lon = " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        updateLocationUI(location.getLatitude(), location.getLongitude());

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

    /***
    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        // get location
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        getLocationPermission();

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(43, -112);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.getUiSettings().setZoomControlsEnabled(true);

        updateLocationUI();

    }
***/
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
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, thirtySeconds, 0, this);
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, thirtySeconds, 0, this);

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
                gMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Marker in Lockport"));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 14)); // 14094

            } else {
                gMap.setMyLocationEnabled(false);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }




}
