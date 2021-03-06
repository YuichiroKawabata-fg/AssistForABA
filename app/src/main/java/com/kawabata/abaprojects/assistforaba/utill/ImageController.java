package com.kawabata.abaprojects.assistforaba.utill;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.kawabata.abaprojects.assistforaba.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageController {

    private Context context;
    public ImageController(Context context){
        this.context=context;
    }
    /**
     * URIからファイル名を取得.
     *
     * @param uri uri
     * @return file name
     */
    public String getFileNameFromUri(Uri uri) {
        // is null
        if (null == uri) {
            return null;
        }

        // get scheme
        String scheme = uri.getScheme();

        // get file name
        String fileName = null;
        switch (scheme) {
            case "content":
                String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                Cursor cursor = this.context.getContentResolver()
                        .query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(
                                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                    }
                    cursor.close();
                }
                break;

            case "file":
                fileName = new File(uri.getPath()).getName();
                break;

            default:
                break;
        }
        return fileName;
    }

    public void deleteImage(final String path) {
        final Uri externalStorageUri = getExternalStorageUri(Build.VERSION.SDK_INT);

        final String selection = "_data = ?";

        final String[] selectionArgs = {
                path
        };
        this.context.getContentResolver().delete(externalStorageUri, selection, selectionArgs);
    }

    public Uri registrationMediaStrage(Uri uri){

        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(this.context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isExternalStorageWritable()) {

            ContentValues values = new ContentValues();
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/"+ this.context.getString(R.string.app_name) + "/" + System.currentTimeMillis() + getFileNameFromUri(uri);
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),this.context.getString(R.string.app_name));
            String fileName = new File(path).getName();
            // コンテンツ クエリの列名
            // ファイル名
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            // マイムの設定
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            //　パスの設定
            values.put(MediaStore.Images.Media.DATA, path);

            if (Build.VERSION.SDK_INT >= 29) {
                final String relativeDir = getRelativeDir(path);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, relativeDir);
                values.put(MediaStore.Images.Media.IS_PENDING, true);
            }else{
                if (!folder.exists()) {
                    /* subDirectoryが存在しない場合は作成する。*/
                    /* 親ディレクトリが存在しない場合は親ディレクトリも作成する。 */
                    folder.mkdirs();
                }
            }


            ContentResolver resolver = this.context.getApplicationContext().getContentResolver();
            Uri collection = getExternalStorageUri(Build.VERSION.SDK_INT);
            //Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri item = resolver.insert(collection, values);

            //画像を表示 BMPに変換して保存
            try (OutputStream outstream = this.context.getContentResolver().openOutputStream(item)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            values.clear();
            if (Build.VERSION.SDK_INT >= 29) {
                //　排他的にアクセスの解除
                values.put(MediaStore.Images.Media.IS_PENDING, false);
                resolver.update(item, values, null, null);
            }
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

    public Bitmap getBitmap(Uri uri){
        ParcelFileDescriptor pfDescriptor = null;
        try {
            pfDescriptor = this.context.getContentResolver().openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (pfDescriptor != null) {
            FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
            Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            try {
                pfDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }
        return null;
    }

    public Bitmap getBitmapFromAsset(String strName)
    {
        AssetManager assetManager = context.getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
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

    public Bitmap rotateBitmap(Bitmap inImage,int angle){
        // 回転マトリックス作成（90度回転）
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        // 回転したビットマップを作成
        Bitmap bmp = Bitmap.createBitmap(inImage, 0, 0, inImage.getWidth(), inImage.getHeight(), mat, true);
        return bmp;
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}
