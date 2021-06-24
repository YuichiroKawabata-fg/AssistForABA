package com.kawabata.abaprojects.assistforaba;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;
import com.kawabata.abaprojects.assistforaba.listcomponent.Util;
import com.kawabata.abaprojects.assistforaba.utill.DatabaseHelper;
import com.kawabata.abaprojects.assistforaba.utill.ImageController;

import java.io.IOException;
import java.io.InputStream;

public class AlarmActivity extends Activity {
    private DatabaseHelper helper = null;
    private int alarmID;
    private ImageView imageViewForTime;
    private ImageView imageViewForCard;
    private TextView textViewForTime;
    private TextView textViewForCard;
    private String clockNName;


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
        textViewForTime.setText(item.getTime());

        //画像ファイル名を２４時表記から１２時間表記に変更
        if (Integer.valueOf(item.getHour()) == 0){
            clockNName = "12"+item.getMinitsu();
        }else if  (Integer.valueOf(item.getHour()) > 21){
            clockNName = String.valueOf(Integer.valueOf(item.getHour())-12)+item.getMinitsu();
        }else if( (Integer.valueOf(item.getHour()) > 12)){
            clockNName = "0"+String.valueOf(Integer.valueOf(item.getHour())-12)+item.getMinitsu();
        }else{
            clockNName = item.getHour()+item.getMinitsu();
        }
        ImageController imageController = new ImageController(this);

        //時間の表示
        Bitmap image = imageController.getBitmapFromAsset("clock/"+clockNName+ ".gif");
        imageViewForTime.setImageBitmap(image);

        //絵カードの表示
        if (item.getUri()!= null) {
            imageViewForCard.setImageBitmap(imageController.getBitmap(Uri.parse(item.getUri())));
        }else{
            imageViewForCard.setImageBitmap(imageController.getBitmapFromAsset("no_image_square.jpg"));
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(R.string.app_name+ alarmID);

        //Closeボタンを定義
        Button cancelButton = findViewById(R.id.button_close_alarm);
        cancelButton.setOnClickListener( v -> {
            finish();
        });

    }

}