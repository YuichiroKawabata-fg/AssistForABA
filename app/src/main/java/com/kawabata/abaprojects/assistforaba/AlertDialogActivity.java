package com.kawabata.abaprojects.assistforaba;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

public class AlertDialogActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlarmDialogFragment fragment = new AlarmDialogFragment();
        fragment.show(getFragmentManager(), "alert_dialog");
    }
}