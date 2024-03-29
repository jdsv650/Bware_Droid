package com.jdsv650.bware;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import java.util.ArrayList;

public class BottomNavigationActivity extends AppCompatActivity {

    public interface UpdatedBridgeListListener {
        void onFinishUpdatingBridges(ArrayList<com.jdsv650.bware.Bridge> bridges);  // callback search
    }

    MapFragment mapFragment;
    SearchFragment searchFragment;
    SettingsFragment settingsFragment;
    GraphFragment graphFragment;

    private UpdatedBridgeListListener listener;
    Boolean isUpdateRequired = false;
    ArrayList<Bridge> bridges = new ArrayList<Bridge>();

    public void addBridge(Bridge bridge)
    {
        this.bridges.add(bridge);
    }
    public void clearAllBridges()
    {
        this.bridges.clear();
    }

    public Boolean getUpdateRequired() {
        return isUpdateRequired;
    }
    public void setUpdateRequired(Boolean isRequired) { isUpdateRequired = isRequired; }

    public MapFragment getMapFragment() {
        return mapFragment;
    }
    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_home:   // home - map pressed

                    while (getSupportFragmentManager().getBackStackEntryCount() > 0) {   // pop back to map
                        getSupportFragmentManager().popBackStackImmediate();
                    }

                    if (isUpdateRequired)  // set on successful search
                    {
                        UpdatedBridgeListListener mapF = (UpdatedBridgeListListener) getMapFragment();
                        mapF.onFinishUpdatingBridges(bridges);  // pass in the bridges to display
                    }
                    isUpdateRequired = false;

                    return true;
                case R.id.navigation_search:  // search pressed

                    searchFragment = new SearchFragment();
                    isUpdateRequired = false;
                    getSupportFragmentManager().beginTransaction().add(R.id.root_layout, searchFragment).addToBackStack(null).commit();

                    return true;
                case R.id.navigation_settings: // settings pressed

                    settingsFragment = new SettingsFragment();
                    getSupportFragmentManager().beginTransaction().add(R.id.root_layout, settingsFragment).addToBackStack(null).commit();
                    isUpdateRequired = false;

                    return true;
                case R.id.navigation_graph:

                    graphFragment = new GraphFragment();
                    getSupportFragmentManager().beginTransaction().add(R.id.root_layout, graphFragment).addToBackStack(null).commit();
                    isUpdateRequired = false;

                    return  true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("B*ware");

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            public void onBackStackChanged()
            {
                // back to map so reload markers
                /***
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    // this fragment was removed from back stack
                    //Toast.makeText(getBaseContext(), "ON BACK STACK CHANGED CALL", Toast.LENGTH_SHORT).show();
                }  ***/
            }
        });

        if (savedInstanceState != null) {   // avoid overlapping error from multiple fragments
            return;
        }

        // add the fragment
        mapFragment = new MapFragment();
        // root_layout added in activity_main.xml -- add map fragment to it
        getSupportFragmentManager().beginTransaction().add(R.id.root_layout, mapFragment).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    // remove all fragments
    private void clearAllFragments()
    {
        if (mapFragment != null)
        {
            getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
        }

        if (searchFragment != null)
        {
            getSupportFragmentManager().beginTransaction().remove(searchFragment).commit();
        }

        if (settingsFragment != null)
        {
            getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();
        }


    }

}
