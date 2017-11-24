package com.jdsv650.bware;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by james on 11/24/17.
 */

public class AboutDialog extends DialogFragment  {

    private Button okButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container);
        getDialog().setTitle("About");
        return view;
    }



}
