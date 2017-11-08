package com.jdsv650.bware;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    Double lat = -99.0;
    Double lon = -99.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        lat = getIntent().getExtras().getDouble("latitude");
        lon = getIntent().getExtras().getDouble("longitude");

        //Toast.makeText(this, "Lat = " + lat.toString() + " Lon = " + lon.toString(), Toast.LENGTH_SHORT).show();



    }





}
