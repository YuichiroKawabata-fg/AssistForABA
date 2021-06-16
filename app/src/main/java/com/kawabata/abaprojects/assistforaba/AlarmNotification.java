package com.kawabata.abaprojects.assistforaba;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AlarmNotification extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmBroadcastReceiver","onReceive() pid=" + android.os.Process.myPid());

        int requestCode = intent.getIntExtra("RequestCode",0);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "default";
        // app name
        String title = context.getString(R.string.app_name);

        long currentTime = System.currentTimeMillis();
        SimpleDateFormat dataFormat =
                new SimpleDateFormat("HH:mm:ss", Locale.JAPAN);
        String cTime = dataFormat.format(currentTime);

        // メッセージ　+ 11:22:331
        String message = "時間になりました。 "+cTime ;

        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Notification　Channel 設定
        NotificationChannel channel = new NotificationChannel(
                channelId, title , NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(message);
        channel.enableVibration(true);
        channel.canShowBadge();
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        // the channel appears on the lockscreen
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setSound(defaultSoundUri, null);
        channel.setShowBadge(true);

        if(notificationManager != null){
            notificationManager.createNotificationChannel(channel);

            //Notificationをタップしたとき、MainActivityへ遷移する
            Intent notifyIntent  = new Intent(context, AlarmActivity.class);
            // データを取得
            int alarmID = intent.getIntExtra("alarm_id",-1);
            String alarmName = intent.getStringExtra("alarm_name");

            notifyIntent.putExtra("alarm_id",alarmID);//ここでAlarmIDを渡す
            notifyIntent .setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP  // 起動中のアプリがあってもこちらを優先する
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED  // 起動中のアプリがあってもこちらを優先する
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS  // 「最近利用したアプリ」に表示させない
            );
            PendingIntent contentIntent =
                    PendingIntent.getActivity(
                            context,
                            alarmID,
                            notifyIntent ,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                            .setContentTitle("アクションの時間になりました。絵カードを見せてあげましょう")
                            .setContentText(alarmName)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(alarmName));

            mBuilder.setContentIntent(contentIntent);

            notificationManager.notify(R.string.app_name + alarmID, mBuilder.build());


/*
            Notification notification = new Notification.Builder(context, channelId)
                    .setContentTitle(title)
                    // android標準アイコンから
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();

            // 通知
            notificationManager.notify(R.string.app_name, notification);
*/


        }
    }
}