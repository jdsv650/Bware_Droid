package com.jdsv650.bware;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final String PREFS_NAME = "PREFS";

    AboutDialog dialog;
    TextView userNameEditText;
    TextView milesTV;
    SeekBar milesBar;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button button = (Button) view.findViewById(R.id.logoutButton);
        button.setOnClickListener(this);

        Button saveButton = (Button) view.findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(this);

        ImageButton aboutButton = (ImageButton) view.findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);

        milesBar = (SeekBar) view.findViewById(R.id.seekBar);
        milesBar.setOnSeekBarChangeListener(this);

        milesTV = (TextView) view.findViewById(R.id.miles_textView);

        // get shared prefs
        SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        Integer distance = preferences.getInt("distance", 50);  // is distance stored
        milesTV.setText("Find bridges within " + distance + " miles");
        milesBar.setProgress(distance);

        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        TextView userNameEditText = (TextView) getView().findViewById(R.id.usernameID);
        userNameEditText.setTextSize(18);

        // get shared prefs
        SharedPreferences preferences = getContext().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        String name = preferences.getString("userName","");  // is token stored

        if (name != "" )
        {
            userNameEditText.setText(name);
        }
        else
        {
            userNameEditText.setText("");
        }
    }

    @Override
    public void onClick(View v) {

        // get shared prefs
        SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        switch (v.getId()) {
            case R.id.logoutButton:

                Toast.makeText(getActivity(), "Logout Pressed", Toast.LENGTH_SHORT).show();

                preferences.edit().remove(".expires").commit();
                preferences.edit().remove("access_token").commit();
                preferences.edit().remove("userName").commit();

                getActivity().finish();

            break;
            case R.id.about_button:

                showAboutDialog();
                break;
            case R.id.saveSettingsButton:

                // get shared prefs
                preferences.edit().putInt("distance", milesBar.getProgress()+5).commit();
                Toast.makeText(getActivity(), "Saving Settings...refresh map to see updates", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    // displays the add todo dialog
    private void showAboutDialog() {

        FragmentManager fm = getFragmentManager();

        AboutDialog aboutDialog = new AboutDialog();
        dialog = aboutDialog;

        //aboutDialog.show(fm, "About");

        AlertDialog.Builder alertDialogBuilder = null;
        alertDialogBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_about, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle("About");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        String display = "Find bridges within " + (progress+5) + " miles";
        milesTV.setText(display);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
