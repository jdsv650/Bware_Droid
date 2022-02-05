package com.jdsv650.bware;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
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
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.jdsv650.bware.Constants.PREFS_NAME;

import static android.content.Context.MODE_PRIVATE;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private SharedPreferences preferences;
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    OkHttpClient client;

    AboutDialog dialog;
    DeleteAccountDialog deleteDialog;
    TextView userNameEditText;
    TextView milesTV;
    SeekBar milesBar;
    Switch densitySwitch;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

        // get shared prefs
        preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        View v = getActivity().getCurrentFocus();


        if (v != null)
        {
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        Button button = (Button) view.findViewById(R.id.logoutButton);
        button.setOnClickListener(this);

        Button saveButton = (Button) view.findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(this);

        ImageButton aboutButton = (ImageButton) view.findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);

        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_account_button);
        deleteButton.setOnClickListener(this);

        densitySwitch = (Switch) view.findViewById(R.id.switchTrafficDensity);
        densitySwitch.setOnClickListener(this);

        milesBar = (SeekBar) view.findViewById(R.id.seekBar);
        milesBar.setOnSeekBarChangeListener(this);

        milesTV = (TextView) view.findViewById(R.id.miles_textView);

        Integer distance = preferences.getInt("distance", 50);  // is distance stored
        Boolean isDisplayDensityOn = preferences.getBoolean("displayDensity", false);

        // set the display density switch base on stored val
        densitySwitch.setChecked(isDisplayDensityOn);

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

        switch (v.getId()) {
            case R.id.logoutButton:
                logoutUser();
                break;

            case R.id.about_button:
                showAboutDialog();
                break;

            case R.id.delete_account_button:
                showDeleteAccountDialog();
                break;

            case R.id.saveSettingsButton:

                // get shared prefs
                preferences.edit().putInt("distance", milesBar.getProgress()+5).commit();
                preferences.edit().putBoolean("displayDensity", densitySwitch.isChecked()).commit();
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

    // displays delete account dialog
    private void showDeleteAccountDialog() {

        FragmentManager fm = getFragmentManager();

        DeleteAccountDialog deleteUserDialog = new DeleteAccountDialog();
        deleteDialog = deleteUserDialog;

        AlertDialog.Builder alertDialogBuilder = null;
        alertDialogBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_delete_account, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle("Delete User");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                deleteAccount();
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }


    private void deleteAccount()
    {
        // make delete api call
        String urlAsString = Constants.baseUrlAsString + "/api/Account/DeleteUser";

        String token = preferences.getString("access_token","");  // is token stored

        if (token != "")
        {
            /*  {"isSuccess":true,"message":"Success" ... */

            String name = preferences.getString("userName","");  // try to get username
            String parameters = "/?user=";

            if (name != "")
            {
                parameters += name;
            }
            else // display error and exit
            {
                Toast.makeText(getActivity(), "Request failed, User Name Unknown", Toast.LENGTH_SHORT).show();
                return;
            }

            urlAsString += parameters;
            String urlEncoded = Uri.encode(urlAsString);

            Request request = new Request.Builder()
                    .url(urlAsString)
                    .addHeader("Authorization", "Bearer " + token)
                    .delete()
                    .build();

            OkHttpClient trustAllclient = Helper.trustAllSslClient(client);

            trustAllclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    final String mMessage = e.getMessage().toString();
                    Log.w("failure Response", mMessage);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()){
                        try {
                            final JSONObject json = new JSONObject(mMessage);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try
                                    {
                                        BottomNavigationActivity navActivity = ((BottomNavigationActivity) getActivity());

                                        Boolean success = json.getBoolean("isSuccess");
                                        Log.i("JSON = ", success.toString());

                                        if (success)
                                        {
                                            Toast.makeText(getActivity(), "Account Deleted Successfully - " + name, Toast.LENGTH_SHORT).show();
                                            BottomNavigationView navigation = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
                                            logoutUser();
                                        }
                                        else // check error message exists
                                        {
                                            String theMessage = json.getString("message");

                                            if (theMessage == null || theMessage == "")
                                            {
                                                Toast.makeText(getActivity(), "Error Deleting User. Please Try again.", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(getActivity(), "Error Deleting User - " + theMessage, Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    }
                                    catch (Exception ex)
                                    {
                                        Toast.makeText(getActivity(), "Error deleting user. Try again.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getActivity(), "Unauthorized Error: verify username and password", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "User Not Found", Toast.LENGTH_SHORT).show();
            getActivity().finish(); // logout
        }

    }


    private void logoutUser()
    {
        // Toast.makeText(getActivity(), "Logout", Toast.LENGTH_SHORT).show();

        preferences.edit().remove(".expires").commit();
        preferences.edit().remove("access_token").commit();
        preferences.edit().remove("userName").commit();

        Intent i = new Intent(getActivity(), WelcomeActivity.class);
        this.startActivity(i);
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
