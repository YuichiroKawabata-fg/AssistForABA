package com.kawabata.abaprojects.assistforaba;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;
import com.kawabata.abaprojects.assistforaba.listcomponent.Util;
import com.kawabata.abaprojects.assistforaba.utill.DatabaseHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private String imageFileName;
    private File imageFile;
    private Uri uri;
    private Uri registratedUri;

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

        textView = findViewById(R.id.textView_imagePath);
        imageView = findViewById(R.id.imageView_Card);

        // アラーム名を取得
        editAlarmName = findViewById(R.id.editAlarmText);

        // ヘルパーの準備
        helper = DatabaseHelper.getInstance(InputAlarmActivity.this);

        //画像登録ボタンを定義
        Button button = findViewById(R.id.button_intoImage);


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

        //TODO 編集時のアクションを追加
        if(reqCode == SecondFragment.EDIT_REQ_CODE){
            // 編集モード
            // 削除ボタンを追加する
            Menu menu = toolbar.getMenu();
            menu.add(0,MENU_DELETE_ID,2,R.string.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            //画像登録ボタンのテキストを変更
            button.setText("絵カード変更");
            // 編集前のデータを取得
            alarmID = intent.getIntExtra(getString(R.string.alarm_id),-1);
            ListItem item = Util.getAlarmsByID(alarmID, helper);
            editAlarmName.setText(item.getAlarmName());
            registratedUri = Uri.parse(item.getImageFileName());
            //登録画像を表示
                if (registratedUri !=null) {
                    try {
                        setImageCard(registratedUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            //           imageFileName = "TestSampeImage.jpg";
            //            imageFile = new File(folder,imageFileName);
           // File folder = new File("/storage/emulated/0/Pictures");
            //imageFileName = item.getImageFileName();


            //         imageFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),imageFileName);
            //Log.d("exist imageFileName",imageFileName);
           // Log.d("exist imageFile",imageFile.getPath());

          //  if (imageFileName != null){
          //      setImageCard();
          //  }

            /*
            File file = new File("/storage/emulated/0/Pictures/TestSampeImage.jpg");
            Uri uri_ = Uri.fromFile(file);
            try (InputStream stream = this.getContentResolver().openInputStream(uri_)) {
                Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(stream));
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */





            if (currentApiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                timePicker.setHour(Integer.parseInt(item.getHour()));
                timePicker.setMinute(Integer.parseInt(item.getMinitsu()));
            } else {
                timePicker.setCurrentHour(Integer.parseInt(item.getHour()));
                timePicker.setCurrentMinute(Integer.parseInt(item.getMinitsu()));
            }



        }else {
            // 新規
            // 何もしない
        }


        final int alarmIDForMenu = alarmID;
        int alarmIDForDelete = alarmID;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id ==  R.id.action_save) {

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

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);

                    // データ登録 or 更新
                    // TODO DB登録後にエラーが発生した場合の考慮が必要
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
                            if(uri != null){
                                cv.put("imageFileName",registrationMediaStrage(uri).toString());
                                if(registratedUri != null) {
                                    deleteImage(
                                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                                    "/"+ getString(R.string.app_name) + "/" +
                                            Util.getFileNameFromUri(getApplicationContext(),registratedUri));

                                }
                            }
                            String[] params = {String.valueOf(requestCode)};
                            db.update("alarms",cv,"alarmid = ?",params);
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
                            if(uri != null){
                                cv.put("imageFileName",registrationMediaStrage(uri).toString());
                            }
                            requestCode = (int)db.insert("alarms",null,cv);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }



                    // 参考 https://qiita.com/hiroaki-dev/items/e3149e0be5bfa52d6a51
                    // TODO アラームの設定


                }else if(id == MENU_DELETE_ID){

                    // 編集
                    // TODO アラーム削除処理
                    if(registratedUri != null) {
                        deleteImage(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                        "/"+ getString(R.string.app_name) + "/" +
                                        Util.getFileNameFromUri(getApplicationContext(),registratedUri));

                    }

                    // データ削除処理
                    try(SQLiteDatabase db = helper.getWritableDatabase()){
                        String[] params = {String.valueOf(alarmIDForDelete)};
                        db.delete("alarms","alarmid = ?",params);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //TODO 削除メッセージ
                    //Toast.makeText(InputAlarmActivity.this,R.string.alarm_delete_msg,Toast.LENGTH_SHORT).show();
                }

                retnIntent = new Intent();
                setResult(RESULT_OK, retnIntent);
                finish();
                return true;
            }
        });


        button.setOnClickListener( v -> {
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


    }
    private void setImageCard(Uri uri) throws IOException {
      ParcelFileDescriptor pfDescriptor = null;
        pfDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        if (pfDescriptor != null) {
            FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
            Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            pfDescriptor.close();
            imageView.setImageBitmap(bmp);
        }
    }

    private void setImageCard(){
        if(isExternalStorageReadable()){
            try(InputStream inputStream0 =
                        new FileInputStream(imageFile) ) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream0);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
                Uri resultUri = resultData.getData();
                this.uri = resultUri;
                // Uriを表示
                textView.setText(
                        String.format(Locale.US, "Uri:　%s", ((Uri) resultUri).toString()));
                //DB登録用URI
                imageFileName = System.currentTimeMillis() + Util.getFileNameFromUri(this,resultUri);
                imageFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),imageFileName);
                Log.d("new imageFileName",imageFileName);
                Log.d("new imageFile",imageFile.getPath());
                try {
                    setImageCard(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                registrationImage(uri);
//                setImageCard();

                /*
                //登録画像を表示
                try {
                    setImageCard(uri);
                } catch (IOException e) {
                    // 何もしない
                } catch (NullPointerException e){
                    // 何もしない
                }

                 */
            }
        }
    }

    //読み込んだ画像をストレージにコピーする
    private void registrationImage(Uri uri){
        File folder = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"alarm");
        imageFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                System.currentTimeMillis() + Util.getFileNameFromUri(this,uri));

        if(isExternalStorageWritable()){
            try(// uriから画像ファイルを取り出し
                InputStream inputStream =
                        this.getContentResolver().openInputStream(uri);
                // 外部ストレージに画像を保存
                FileOutputStream output =
                        new FileOutputStream(imageFile);
            ) {

                // バッファーを使って画像を書き出す
                int DEFAULT_BUFFER_SIZE = 10240 * 4;
                byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                int len;

                while((len=inputStream.read(buf))!=-1){
                    output.write(buf,0, len);
                }
                output.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void deleteImage(final String path) {
        final Uri externalStorageUri = getExternalStorageUri(Build.VERSION.SDK_INT);

        final String selection = "_data = ?";

        final String[] selectionArgs = {
                path
        };
        getApplicationContext().getContentResolver().delete(externalStorageUri, selection, selectionArgs);
    }
    private Uri registrationMediaStrage(Uri uri){

        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isExternalStorageWritable()) {

            ContentValues values = new ContentValues();

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/"+ getString(R.string.app_name) + "/" + System.currentTimeMillis() + Util.getFileNameFromUri(this,uri);
            String fileName = new File(path).getName();
            // コンテンツ クエリの列名
            // ファイル名
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            // マイムの設定
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            //　パスの設定
            values.put(MediaStore.Images.Media.DATA, path);
            // 書込み時にメディア ファイルに排他的にアクセスする
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            if (Build.VERSION.SDK_INT >= 29) {
                final String relativeDir = getRelativeDir(path);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, relativeDir);
                values.put(MediaStore.Images.Media.IS_PENDING, true);
            }


            ContentResolver resolver = getApplicationContext().getContentResolver();
            Uri collection = getExternalStorageUri(Build.VERSION.SDK_INT);
            Uri item = resolver.insert(collection, values);

            //画像を表示 BMPに変換して保存
            try (OutputStream outstream = getContentResolver().openOutputStream(item)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            values.clear();
            //　排他的にアクセスの解除
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(item, values, null, null);

            return item;
        }

        return null;
    }

    public String getRelativeDir(final String path) {
        final String target = Environment.getExternalStoragePublicDirectory("").getPath() + "/";
        final String relativePath = path.replace(target, "");
        final File file = new File(relativePath);
        final String relativeDir = file.getParent();

        return relativeDir;
    }

    public Uri getExternalStorageUri(int sdk_int) {
        if (sdk_int < 29) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

}