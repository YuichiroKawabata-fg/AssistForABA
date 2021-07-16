package com.kawabata.abaprojects.assistforaba;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;
import com.kawabata.abaprojects.assistforaba.listcomponent.Util;
import com.kawabata.abaprojects.assistforaba.utill.DatabaseHelper;
import com.kawabata.abaprojects.assistforaba.utill.ImageController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

public class InputAlarmActivity extends AppCompatActivity {

    private AlarmManager alarmMgr = null;
    private PendingIntent alarmIntent = null;
    private TimePicker timePicker = null;
    private DatabaseHelper helper = null;
    private EditText editAlarmName = null;
    private int reqCode = -1;
    Intent retnIntent = null;
    int currentApiVersion = Build.VERSION.SDK_INT;
    private static int MENU_DELETE_ID = 2;
    private static final int RESULT_PICK_IMAGEFILE = 1001;
    private TextView textView;
    private ImageView imageView;
    private String strUri;
    private File imageFile;
    private Uri uri;
    private Uri registratedUri;
    private ImageController imageController;

    private CheckBox checkSunday = null;
    private CheckBox checkMonday = null;
    private CheckBox checkTuesday = null;
    private CheckBox checkWednesday = null;
    private CheckBox checkThursday = null;
    private CheckBox checkFriday = null;
    private CheckBox checkSaturday = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_alarm);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        // タイムピッカーを取得
        timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        // イメージビューを取得
        imageView = findViewById(R.id.imageView_Card);

        //曜日チェックボックスるを取得
        checkSunday = findViewById(R.id.checkBox_sunday);
        checkMonday = findViewById(R.id.checkBox_monday);
        checkTuesday = findViewById(R.id.checkBox_tuesday);
        checkWednesday = findViewById(R.id.checkBox_wednesday);
        checkThursday = findViewById(R.id.checkBox_thursday);
        checkFriday = findViewById(R.id.checkBox_friday);
        checkSaturday = findViewById(R.id.checkBox_saturday);

        // アラーム名を取得
        editAlarmName = findViewById(R.id.editAlarmText);

        // ヘルパーの準備
        helper = DatabaseHelper.getInstance(InputAlarmActivity.this);
        
        //画像のコントローラの登録
        imageController = new ImageController(this);

        //画像登録ボタンを定義
        Button buttonIntoImage = findViewById(R.id.button_intoImage);

        //画像選択ボタンの挙動をセット
        //ここでファイルエクスプローラをコールする
        buttonIntoImage.setOnClickListener( v -> {
            // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
            Intent intent_exp = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            intent_exp.addCategory(Intent.CATEGORY_OPENABLE);

            // Filter to show only images, using the image MIME data type.
            // it would be "*/*".
            intent_exp.setType("*/*");

            startActivityForResult(intent_exp, RESULT_PICK_IMAGEFILE);
        });


        // キャンセルボタンの設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarInput);
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
            }
        });


        // 保存、削除ボタンの設定
        toolbar.inflateMenu(R.menu.edit_menu);

        // 新規 or 編集を取得
        Intent intent = getIntent();
        //この部分で編集する必要がある科を判断する
        reqCode = intent.getIntExtra(getString(R.string.request_code),-1);
        int alarmID = 0;

        // 編集モード
        if(reqCode == SecondFragment.EDIT_REQ_CODE){
            // 削除ボタンを追加する
            toolbar.getMenu().findItem(R.id.action_delete).setVisible(true);

            // 編集前のデータを取得
            alarmID = intent.getIntExtra(getString(R.string.alarm_id),-1);
            ListItem item = Util.getAlarmsByID(alarmID, helper);
            editAlarmName.setText(item.getAlarmName());
            checkSunday.setChecked(item.getSunday());
            checkMonday.setChecked(item.getMonday());
            checkTuesday.setChecked(item.getTuesday());
            checkWednesday.setChecked(item.getWednesday());
            checkThursday.setChecked(item.getThursday());
            checkFriday.setChecked(item.getFriday());
            checkSaturday.setChecked(item.getSaturday());

            if (currentApiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                timePicker.setHour(Integer.parseInt(item.getHour()));
                timePicker.setMinute(Integer.parseInt(item.getMinitsu()));
            } else {
                timePicker.setCurrentHour(Integer.parseInt(item.getHour()));
                timePicker.setCurrentMinute(Integer.parseInt(item.getMinitsu()));
            }

            //登録されている画像を表示
            if (item.getUri() !=null) {
                //画像登録ボタンのテキストを変更
                registratedUri = Uri.parse(item.getUri());
                buttonIntoImage.setText("絵カード変更");
                imageView.setImageBitmap(imageController.getBitmap(registratedUri));
            }
        }else {
            // 新規
            // 何もしない
        }


        final int alarmIDForMenu = alarmID;
        int alarmIDForDelete = alarmID;
        
        //ツールバーの挙動設定
        int finalAlarmID = alarmID;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                //保存ボタン
                if (id ==  R.id.action_save || id == R.id.action_demo) {
                    // アラーム設定処理
                    // 設定時刻を取得
                    int hour;
                    int minute;
                    if (currentApiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        hour = timePicker.getHour();
                        minute = timePicker.getMinute();
                    } else {
                        hour = timePicker.getCurrentHour();
                        minute = timePicker.getCurrentMinute();
                    }

                    // データ登録 or 更新
                    int requestCode = -1;

                    // アラーム名の設定
                    String alarmName = editAlarmName.getText().toString();
                    if(alarmName.equals("")){
                        alarmName = "無題";
                    }

                    // 時刻登録の準備
                    String alarmTime = String.format("%02d", hour) + ":"
                            + String.format("%02d", minute);

                    if(reqCode == SecondFragment.EDIT_REQ_CODE){
                        // 編集
                        // データ更新処理
                        requestCode = alarmIDForMenu;
                        try(SQLiteDatabase db = helper.getWritableDatabase()){
                            ContentValues cv = new ContentValues();
                            cv.put("name",alarmName);
                            cv.put("alarttime", alarmTime);

                            if(uri != null){ //絵カードがすでに登録された場合
                                strUri = imageController.registrationMediaStrage(uri).toString();
                                if(registratedUri != null) { //既存の絵カードから書き換えられた場合は、既存の絵カードデータを削除
                                    imageController.deleteImage(
                                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                                    "/"+ getString(R.string.app_name) + "/" +
                                                    imageController.getFileNameFromUri(registratedUri));

                                }
                            }else{
                                if(registratedUri != null) {//絵カードが新たに登録された場合
                                    strUri = registratedUri.toString();
                                }
                            }
                            cv.put("uri",strUri);
                            cv.put("sunday",checkSunday.isChecked());
                            cv.put("monday",checkMonday.isChecked());
                            cv.put("tuesday",checkTuesday.isChecked());
                            cv.put("wednesday",checkWednesday.isChecked());
                            cv.put("thursday",checkThursday.isChecked());
                            cv.put("friday",checkFriday.isChecked());
                            cv.put("saturday",checkSaturday.isChecked());

                            String[] params = {String.valueOf(requestCode)};
                            db.update("alarms",cv,"alarmid = ?",params);
                            //reservationAlarm(finalAlarmID,alarmName,hour,minute,"");
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }else {
                        // 新規
                        // データ登録
                        try(SQLiteDatabase db = helper.getWritableDatabase()){
                            ContentValues cv = new ContentValues();
                            cv.put("name",alarmName);
                            cv.put("alarttime", alarmTime);
                            cv.put("sunday",checkSunday.isChecked());
                            cv.put("monday",checkMonday.isChecked());
                            cv.put("tuesday",checkTuesday.isChecked());
                            cv.put("wednesday",checkWednesday.isChecked());
                            cv.put("thursday",checkThursday.isChecked());
                            cv.put("friday",checkFriday.isChecked());
                            cv.put("saturday",checkSaturday.isChecked());

                            if(uri != null){
                                cv.put("uri",imageController.registrationMediaStrage(uri).toString());
                            }
                            // TODO アラームの設定
                            reservationAlarm((int)db.insert("alarms",null,cv),alarmName,hour,minute,"");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    retnIntent = new Intent();
                    setResult(RESULT_OK, retnIntent);
                    finish();

                }else if (id ==  R.id.action_delete) {

                    new AlertDialog.Builder(InputAlarmActivity.this)
                            .setTitle("確認")
                            .setMessage("本当に削除しますか？")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //画像削除
                                    if(registratedUri != null) {
                                        imageController.deleteImage(
                                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                                        "/"+ getString(R.string.app_name) + "/" +
                                                        imageController.getFileNameFromUri(registratedUri));

                                    }
                                    // データ削除処理
                                    try(SQLiteDatabase db = helper.getWritableDatabase()){
                                        String[] params = {String.valueOf(alarmIDForDelete)};
                                        db.delete("alarms","alarmid = ?",params);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    //削除メッセージ
                                    Toast.makeText(InputAlarmActivity.this,R.string.action_delete_message,Toast.LENGTH_SHORT).show();

                                    retnIntent = new Intent();
                                    setResult(RESULT_OK, retnIntent);
                                    finish();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                return true;
            }
        });

        //画像回転ボタンの挙動を定義
        Button cancelButton = findViewById(R.id.button_rotate_image);
        cancelButton.setOnClickListener( v -> {
            Bitmap bitmap;
            Bitmap rotatedBitmap;
            if(uri != null){
                bitmap = imageController.getBitmap(uri);
                rotatedBitmap = imageController.rotateBitmap(bitmap,90);
                imageView.setImageBitmap(rotatedBitmap);
                uri = imageController.getImageUri(rotatedBitmap);
            }else if(registratedUri != null) {
                bitmap = imageController.getBitmap(registratedUri);
                rotatedBitmap = imageController.rotateBitmap(bitmap,90);
                imageView.setImageBitmap(rotatedBitmap);
                registratedUri = imageController.getImageUri(rotatedBitmap);
            }
        });

    }

    //エクスプローラーで選択された画像を取得
    //選択された画像のURIをセットし、ImageViewに表示する
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData.getData() != null) {
                this.uri = resultData.getData();
                imageView.setImageBitmap(imageController.getBitmap(this.uri));
            }
        }
    }

    private void reservationAlarm(int alarmID,String alarmName, int hour, int minute, String strUri){
        AlarmManager am;
        PendingIntent pending;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //TODO 実際にセットするように戻す
        //calendar.set(Calendar.HOUR_OF_DAY, hour);
        //calendar.set(Calendar.MINUTE, minute);

        //まずはサンプル！ 5秒後にアラームが設定されるように決め打ち
        calendar.add(Calendar.SECOND, 5);
        //ブロードキャストレシーバーのIntentコールをおこなう設定（アラーム通知クラスをコールする）
        Intent intent = new Intent(getApplicationContext(), AlarmNotification.class);
        intent.putExtra("alarm_id",alarmID);
        intent.putExtra("alarm_name",alarmName);
        intent.putExtra("uri",strUri);
        //intent.putExtra("ListItem", item);

        Log.d("InputAlarmActivity","alarmID=" + String.valueOf(alarmID));

        pending = PendingIntent.getBroadcast(
                getApplicationContext(),alarmID, intent, 0);

        // アラームをセットする
        am = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pending);
        }

    }

    private void cancelAlarm(int alarmID){

    }


}