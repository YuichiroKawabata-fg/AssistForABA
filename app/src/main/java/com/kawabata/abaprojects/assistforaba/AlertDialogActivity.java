package com.kawabata.abaprojects.assistforaba;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.kawabata.abaprojects.assistforaba.utill.DatabaseHelper;

public class AlertDialogActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        // データを取得
        int alarmID = intent.getIntExtra(getString(R.string.alarm_id),-1);
        Log.d("AlertDialogActivity","alarmID=" + String.valueOf(alarmID));


        AlarmDialogFragment fragment = new AlarmDialogFragment();
        // 渡す値をセット
        Bundle args = new Bundle();
        args.putInt(getString(R.string.alarm_id), alarmID);
        fragment.setArguments(args);

        fragment.show(getFragmentManager(), "alert_dialog");
    }
}