package com.jdsv650.bware;


import android.os.Bundle;
// import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.DialogFragment;
import android.widget.Button;
import android.widget.EditText;


public class ResetPasswordFragment extends DialogFragment implements View.OnClickListener {

    private EditText emailEditText;


    public interface ResetPasswordDialogListener {
        void onFinishResetPasswordDialog(String email);
    }

    public ResetPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reset_password, container);
        emailEditText = (EditText) view.findViewById(R.id.resetPassword_editText);

        ((Button) view.findViewById(R.id.cancelButton)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.saveButton)).setOnClickListener(this);

        getDialog().setTitle("Reset Password");

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancelButton:  // cancel so just dismiss

                dismiss();
                break;
            case R.id.saveButton:   // save todo

                Log.i("Email to reset = ", emailEditText.getText().toString());

                ResetPasswordDialogListener activity = (ResetPasswordDialogListener) getActivity();

                // pass back email to any listener
                activity.onFinishResetPasswordDialog(emailEditText.getText().toString());

                dismiss();
                break;
        }

    }
}
