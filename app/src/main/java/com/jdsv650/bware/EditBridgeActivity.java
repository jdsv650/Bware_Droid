package com.jdsv650.bware;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class EditBridgeActivity extends AppCompatActivity {

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
    Switch isRET;

    Bridge bridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bridge);

        Intent i = getIntent();
        bridge = (Bridge) i.getExtras().getSerializable("bridge");

        countryET = (EditText) findViewById(R.id.edit_countryEditText);
        cityET = (EditText) findViewById(R.id.edit_cityEditText);
        stateET = (EditText) findViewById(R.id.edit_stateEditText);
        countyET = (EditText) findViewById(R.id.edit_countyEditText);
        locationET = (EditText) findViewById(R.id.edit_descriptionEditText);
        carriedET = (EditText) findViewById(R.id.edit_carriedEditText);
        crossedET = (EditText) findViewById(R.id.edit_crossedEditText);
        zipET = (EditText) findViewById(R.id.edit_zipEditText);
        straightET = (EditText) findViewById(R.id.edit_tandemEditText);
        triET = (EditText) findViewById(R.id.edit_triaxleEditText);
        comboET = (EditText) findViewById(R.id.edit_combinationEditText);
        doubleET = (EditText) findViewById(R.id.edit_doubleEditText);
        heightET = (EditText) findViewById(R.id.edit_heightEditText);
        otherET = (EditText) findViewById(R.id.edit_otherEditText);
        isRET = (Switch) findViewById(R.id.edit_rSwitch);

        populateTextFields();

    }

    void populateTextFields()
    {
        if (bridge == null || bridge.latitude == -99.0 || bridge.longitude == -99.0) { return; }

        countryET.setText(bridge.country);
        cityET.setText(bridge.city);
        stateET.setText(bridge.state);
        countyET.setText(bridge.county);
        locationET.setText(bridge.locationDescription);
        carriedET.setText(bridge.featureCarried);
        crossedET.setText(bridge.featureCrossed);
        zipET.setText(bridge.zip);

        if (bridge.weightStraight == -99)
        {
            straightET.setText("");
            bridge.weightStraight = null;
        }
        else {
            straightET.setText(bridge.weightStraight.toString());
        }

        if (bridge.weightStraight_TriAxle == -99)
        {
            triET.setText("");
            bridge.weightStraight_TriAxle = null;
        }
        else {
            triET.setText(bridge.weightStraight_TriAxle.toString());
        }

        if (bridge.weightCombo == -99)
        {
            comboET.setText("");
            bridge.weightCombo = null;
        }
        else {
            comboET.setText(bridge.weightCombo.toString());
        }

        if (bridge.weightDouble == -99)
        {
            doubleET.setText("");
            bridge.weightDouble = null;
        }
        else {
            doubleET.setText(bridge.weightDouble.toString());
        }

        if (bridge.height == -99)
        {
            heightET.setText("");
            bridge.height = null;
        }
        else {
            heightET.setText(bridge.height.toString());
        }

        otherET.setText(bridge.otherPosting);
        isRET.setChecked(bridge.isRPosted);

    }
}
