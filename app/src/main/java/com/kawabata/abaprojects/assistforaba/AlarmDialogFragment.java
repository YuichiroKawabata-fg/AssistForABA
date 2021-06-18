package com.kawabata.abaprojects.assistforaba;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;
import com.kawabata.abaprojects.assistforaba.listcomponent.Util;
import com.kawabata.abaprojects.assistforaba.utill.DatabaseHelper;
import com.kawabata.abaprojects.assistforaba.utill.ImageController;

//絵カードの通知をポップアップで実施
public class AlarmDialogFragment extends DialogFragment {
    private DatabaseHelper helper = null;
    private int alarmID;
    private Uri registratedUri;
    private ImageView imageViewForCard;
    private TextView textViewForCard;
    private View layout;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

// ヘルパーの準備
        helper = DatabaseHelper.getInstance(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        layout = inflater.inflate(R.layout.fragment_alarm_dialog, null);
        textViewForCard = (TextView)layout.findViewById(R.id.alarm_dialog_action_name);
        imageViewForCard = (ImageView)layout.findViewById(R.id.alarm_dialog_action_card);

        // データを取得
        alarmID = getArguments().getInt(getString((R.string.alarm_id)), -1);
        Log.d("AlarmDialogFragment","alarmID=" + String.valueOf(alarmID));
        ListItem item = Util.getAlarmsByID(alarmID, helper);
        textViewForCard.setText(item.getAlarmName());
        registratedUri = Uri.parse(item.getUri());

        //絵カードの表示
        ImageController imageController = new ImageController(getActivity());
        imageViewForCard.setImageBitmap(imageController.getBitmap(this.registratedUri));


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout)
        // Add action buttons
           .setPositiveButton(R.string.open_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getActivity(), AlarmActivity.class);
                intent.putExtra("alarm_id",alarmID);
                startActivity(intent);
            }
        })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().finish();
    }
}