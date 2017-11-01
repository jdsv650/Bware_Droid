package com.jdsv650.bware;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class BottomNavigationActivity extends AppCompatActivity {

    MapFragment mapFragment;
    SearchFragment searchFragment;
    SettingsFragment settingsFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            clearAllFragments();


            switch (item.getItemId()) {
                case R.id.navigation_home:


                        // add the fragment
                        mapFragment = new MapFragment();
                        // root_layout added in activity_main.xml -- add map fragment to it
                        getSupportFragmentManager().beginTransaction().add(R.id.root_layout, mapFragment).commit();


                    return true;
                case R.id.navigation_search:

                    searchFragment = new SearchFragment();
                    getSupportFragmentManager().beginTransaction().add(R.id.root_layout, searchFragment).commit();

                    return true;
                case R.id.navigation_settings:

                    settingsFragment = new SettingsFragment();
                    getSupportFragmentManager().beginTransaction().add(R.id.root_layout, settingsFragment).commit();

                    return true;
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
