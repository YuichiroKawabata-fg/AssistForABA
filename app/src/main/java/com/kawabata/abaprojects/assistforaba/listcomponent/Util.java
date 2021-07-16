package com.kawabata.abaprojects.assistforaba.listcomponent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.kawabata.abaprojects.assistforaba.AlarmNotification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class Util {

    // アラームのデータを取得
    public static ListItem getAlarmsByID(int alarmID, SQLiteOpenHelper helper){

        ListItem item = null;
        try(SQLiteDatabase db = helper.getReadableDatabase()) {

            String[] cols ={"alarmid","name","alarttime","uri","sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
            String[] params = {String.valueOf(alarmID)};

            Cursor cs = db.query("alarms",cols,"alarmid = ?",params,
                    null,null,"alarmid",null);
            cs.moveToFirst();
            item = new ListItem();
            item.setAlarmID(cs.getInt(0));
            item.setAlarmName(cs.getString(1));
            item.setTime(cs.getString(2));
            item.setUri(cs.getString(3));
            item.setSunday(stringToBoolean(cs.getString(4)));
            item.setMonday(stringToBoolean(cs.getString(5)));
            item.setTuesday(stringToBoolean(cs.getString(6)));
            item.setWednesday(stringToBoolean(cs.getString(7)));
            item.setThursday(stringToBoolean(cs.getString(8)));
            item.setFriday(stringToBoolean(cs.getString(9)));
            item.setSaturday(stringToBoolean(cs.getString(10)));


        }
        return item;
    }
    public static ArrayList<ListItem> getAlarmList(SQLiteOpenHelper helper){

        ArrayList<ListItem> data = new ArrayList<>();
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            String[] cols = {"alarmid", "name", "alarttime", "uri", "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
            Cursor cs = db.query("alarms",cols,null,null,
                    null,null,"alarmid",null);

            boolean eol = cs.moveToFirst();
            while (eol) {
                ListItem item = new ListItem();
                item.setAlarmID(cs.getInt(0));
                item.setAlarmName(cs.getString(1));
                item.setTime(cs.getString(2));
                item.setUri(cs.getString(3));
                item.setSunday(stringToBoolean(cs.getString(4)));
                item.setMonday(stringToBoolean(cs.getString(5)));
                item.setTuesday(stringToBoolean(cs.getString(6)));
                item.setWednesday(stringToBoolean(cs.getString(7)));
                item.setThursday(stringToBoolean(cs.getString(8)));
                item.setFriday(stringToBoolean(cs.getString(9)));
                item.setSaturday(stringToBoolean(cs.getString(10)));
                data.add(item);
                eol = cs.moveToNext();
            }
        }
        return data;
    }


    private static Boolean stringToBoolean(String str){
        if(str != null){
            if(str.equals("1")) return true;
        }
        return false;
    }

    // アラームをセット
    public static void setAlarm(Context context, ListItem item){

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(item.getHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(item.getMinitsu()));
        calendar.set(Calendar.SECOND, 0);

        // 現在時刻を取得
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());

        // 比較
        int diff = calendar.compareTo(nowCalendar);

        // 日付を設定
        if(diff <= 0){
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        }

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmNotification.class);
        intent.setData(Uri.parse(String.valueOf(item.getAlarmID())));
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, item.getAlarmID(), intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmMgr.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null), alarmIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
    }


}