package com.jdsv650.bware;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    public static final String PREFS_NAME = "PREFS";


    TextView userNameEditText;

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

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);

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
}
