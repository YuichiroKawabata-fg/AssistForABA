package com.kawabata.abaprojects.assistforaba;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;
import com.kawabata.abaprojects.assistforaba.listcomponent.Util;
import com.kawabata.abaprojects.assistforaba.utill.DatabaseHelper;
import com.kawabata.abaprojects.assistforaba.utill.ImageController;

import java.io.IOException;
import java.io.InputStream;

public class AlarmActivity extends AppCompatActivity {
    private DatabaseHelper helper = null;
    private int alarmID;
    private ImageView imageViewForTime;
    private ImageView imageViewForCard;
    private TextView textViewForTime;
    private TextView textViewForCard;
    private Uri registratedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        imageViewForTime = (ImageView)findViewById(R.id.imageView_alarm_time);
        imageViewForCard = (ImageView)findViewById(R.id.imageView_alarm_card);
        textViewForTime= (TextView)findViewById(R.id.textView_alarm_time);
        textViewForCard= (TextView)findViewById(R.id.textView_alarm_action);
        // ヘルパーの準備
        helper = DatabaseHelper.getInstance(this);

        Intent intent = getIntent();

        // データを取得
        alarmID = intent.getIntExtra(getString(R.string.alarm_id),-1);
        Log.d("AlarmActivity","alarmID=" +String.valueOf(alarmID));
        ListItem item = Util.getAlarmsByID(alarmID, helper);
        textViewForCard.setText(item.getAlarmName());
        registratedUri = Uri.parse(item.getUri());


        //時間の表示
        Bitmap image = getBitmapFromAsset("1200.png");
        imageViewForTime.setImageBitmap(image);

        //絵カードの表示
        ImageController imageController = new ImageController(this);
        imageViewForCard.setImageBitmap(imageController.getBitmap(this.registratedUri));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(R.string.app_name+ alarmID);

    }


    private Bitmap getBitmapFromAsset(String strName)
    {
        AssetManager assetManager = getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }
}