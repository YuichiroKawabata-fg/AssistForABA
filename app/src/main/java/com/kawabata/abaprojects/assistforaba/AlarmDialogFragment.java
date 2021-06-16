package com.kawabata.abaprojects.assistforaba;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

//絵カードの通知をポップアップで実施
public class AlarmDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder.setTitle("通知").setMessage("").create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().finish();
    }
}