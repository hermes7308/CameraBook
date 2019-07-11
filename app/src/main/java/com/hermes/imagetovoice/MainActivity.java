package com.hermes.imagetovoice;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String MY_AD_ID = "ca-app-pub-5539737498268274~4397298097";
    private static final String TEST_AD_ID = "ca-app-pub-3940256099942544~3347511713";

    private static final int IMAGE_CAPTURE_CODE = 1;
    private static final int GALLERY_CODE = 2;

    private AdView mAdView;

    private ImageView mTargetImage;
    private EditText mResultEditText;

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;

    private Uri outputFileDir;

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Permission
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();

        // TTS
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        // AdMob
        MobileAds.initialize(this, TEST_AD_ID);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener());

        // View initialize
        mTargetImage = findViewById(R.id.targetImage);
        mResultEditText = findViewById(R.id.resultEditText);

        alertDialogBuilder = new AlertDialog.Builder(this).setView(R.layout.progressbar_modal)
                .setCancelable(false);
        alertDialog = alertDialogBuilder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_camera:
                try {
                    String imagePath = FileManager.DATA_PATH + "/imgs";
                    File dir = new File(imagePath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    String imageFilePath = imagePath + "/ocr.jpg";
                    outputFileDir = Uri.fromFile(new File(imageFilePath));

                    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileDir);

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, IMAGE_CAPTURE_CODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.menu_gallery:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
                break;
            case R.id.menu_media_play:
                textToSpeech.speak(mResultEditText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                break;
            case R.id.menu_copy:
                String text = mResultEditText.getText().toString();

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", text);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "successful copy", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_clear:
                mResultEditText.setText(StringUtils.EMPTY);

                Toast.makeText(getApplicationContext(), "successful clear", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_CAPTURE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    CropImage.activity(outputFileDir).start(this);
                    break;
                case RESULT_CANCELED:
                    break;
            }
            return;
        }

        if (requestCode == GALLERY_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri imageUri = data.getData();
                    CropImage.activity(imageUri).start(this);
                    break;
                case RESULT_CANCELED:
                    break;
            }
            return;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                setTargetImage(resultUri);

                new Thread(new ImageTransferTask(this, resultUri)).start();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.getError().printStackTrace();
            }

            return;
        }
    }

    private void setTargetImage(Uri imageUri) {
        Bitmap targetBitmap = getBitmapOrNull(imageUri);
        if (targetBitmap == null) {
            return;
        }
        mTargetImage.setImageBitmap(targetBitmap);
    }

    private Bitmap getBitmapOrNull(Uri imageUri) {
        InputStream in;
        try {
            in = getContentResolver().openInputStream(imageUri);
            Bitmap targetBitmap = BitmapFactory.decodeStream(in);

            if (in != null) {
                in.close();
            }

            return targetBitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (HandlerCode.SET_RESULT.equals(msg.what)) {
                mResultEditText.setText((String) msg.obj);
                return;
            }

            if (HandlerCode.START_OCR.equals(msg.what)) {
                alertDialog.show();
                return;
            }

            if (HandlerCode.FINISH_OCR.equals(msg.what)) {
                alertDialog.cancel();
                return;
            }
        }
    };

    public Handler getHandler() {
        return mHandler;
    }

    public enum HandlerCode {
        SET_RESULT(1),
        START_OCR(2),
        FINISH_OCR(3);

        private int code;

        HandlerCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public boolean equals(int code) {
            return this.code == code;
        }
    }

}