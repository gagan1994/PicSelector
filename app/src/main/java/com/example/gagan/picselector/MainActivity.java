package com.example.gagan.picselector;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_ID = 1;
    private static final int GALLERY_ID = 2;
    Button btnCamera, btnGallery, btn_open;
    ImageView image;
    FloatingActionButton voice;
    TextToSpeech tts;
    TextInputEditText text;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCamera = findViewById(R.id.btn_camera);
        btn_open = findViewById(R.id.btn_open);
        btnGallery = findViewById(R.id.btn_gallery);
        voice = findViewById(R.id.voice);
        image = findViewById(R.id.image);
        text = findViewById(R.id.text);

        image.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btn_open.setOnClickListener(this);
        voice.setOnClickListener(this);
        initTextToSpeach();
    }

    private void initTextToSpeach() {
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
            return;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                openImageActivity(CAMERA_ID);
                break;
            case R.id.btn_gallery:
                openImageActivity(GALLERY_ID);
                break;
            case R.id.btn_open:
                openFile(null);
                break;
            case R.id.voice:
                speak();
                break;
            case R.id.image:
                if (imageFile != null)
                    openFile(imageFile);
        }
    }

    private void speak() {
        String textToSpeek = (text.getText() == null ||
                text.getText().toString().trim().length() == 0) ?
                "Content not available" : text.getText().toString();
        Utils.getInstance().SpeekWithHeadPhone(this, textToSpeek);
    }

    private void openFile(File imageFile) {
        if (imageFile == null) {
            ///storage/emulated/0/MyCache/clientminutes/2/clientminutes_2_2018-05-10T17:47:45_IMG_20180510_151048.jpg
            String filePath = "MyCache/clientminutes/2/clientminutes_2_2018-05-10T17:47:45_IMG_20180510_151048.jpg";
            imageFile = new File(Environment.getExternalStorageDirectory(), filePath);
        }

        openFileDialog(this, imageFile, null);

    }

    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Notification listner service");
        alertDialogBuilder.setMessage("For app to work you must enable Notification Listner service .Enable now");
        alertDialogBuilder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return (alertDialogBuilder.create());
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean openFileDialog(final Context context, File file, ProgressDialog progressDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(apkUri);
            Log.w("ImageUri", apkUri.toString());
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } else {
            Uri apkUri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        return true;
    }

    private void openImageActivity(int id) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_ID:
                    selectedFromCamera(data);
                    break;
                case GALLERY_ID:
                    selectedFromGallery(data);
                    break;
            }

        }
    }

    private void selectedFromGallery(Intent data) {
        Uri uri = data.getData();
        String path = getRealPathFromDocumentUri(this, uri);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        bmOptions.inSampleSize = 1;
        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        upload(bitmap);
        imageFile = new File(path);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }

    private void upload(Bitmap path2) {
        image.setImageBitmap(path2);
        Toast.makeText(this, "Size of image " + sizeOf(path2), Toast.LENGTH_LONG).show();

    }

    private void selectedFromCamera(Intent data) {

    }

    public static String getRealPathFromDocumentUri(Context context, Uri uri) {
        String filePath = "";

        Pattern p = Pattern.compile("(\\d+)$");
        Matcher m = p.matcher(uri.toString());
        if (!m.find()) {
            Log.e("asas", "ID for requested image not found: " + uri.toString());
            return filePath;
        }
        String imgId = m.group();

        String[] column = {MediaStore.Images.Media.DATA};
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{imgId}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();

        return filePath;
    }




}
