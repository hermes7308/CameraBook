package com.hermes.camerabook;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.hermes.camerabook.ocr.TesseractOcrTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class XSelectActivity extends AppCompatActivity {
    private static final String TAG = XSelectActivity.class.getName();

    // private static final String AD_ID = "ca-app-pub-5539737498268274~4397298097"; // real
    private static final String AD_ID = "ca-app-pub-3940256099942544~3347511713"; // test

    private static final int IMAGE_CAPTURE_CODE = 1;
    private static final int GALLERY_CODE = 2;
    public static final String DISABLE_DEATH_ON_FILE_URI_EXPOSURE = "disableDeathOnFileUriExposure";

    private ImageView mTargetImage;
    private EditText mResultEditText;

    private AlertDialog progressBarAlertDialog;
    private AlertDialog ttsAlertDialog;

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xselect);

        // Set for different file system
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod(DISABLE_DEATH_ON_FILE_URI_EXPOSURE);
                m.invoke(null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        // View initialize
        mTargetImage = findViewById(R.id.targetImage);
        mResultEditText = findViewById(R.id.resultEditText);

        // AdMob
        MobileAds.initialize(this, AD_ID);
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener());

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

        // AlertDialog Modal
        progressBarAlertDialog = new AlertDialog.Builder(this)
                .setView(R.layout.progressbar_modal)
                .setCancelable(false)
                .create();

        LayoutInflater inflater = getLayoutInflater();
        View ttsModalView = inflater.inflate(R.layout.tts_modal, null);
        ttsAlertDialog = new AlertDialog.Builder(this)
                .setView(ttsModalView)
                .setCancelable(false)
                .create();

        ttsModalView.findViewById(R.id.ttsCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }

                ttsAlertDialog.cancel();
            }
        });

        // TTS
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            sendShowSignal();
                        }

                        @Override
                        public void onDone(String s) {
                            sendCancelSignal();
                        }

                        @Override
                        public void onError(String s) {
                            sendCancelSignal();
                        }

                        private void sendShowSignal() {
                            Message message = new Message();
                            message.what = HandlerCode.START_TTS.getCode();
                            mHandler.sendMessage(message);
                        }

                        private void sendCancelSignal() {
                            Message message = new Message();
                            message.what = HandlerCode.FINISH_TTS.getCode();
                            mHandler.sendMessage(message);
                        }
                    });

                    // set Language
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
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
//                TesseractOcrTask.initTessPath();
//
//                final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, TesseractOcrTask.outputFileDir);
//
//                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
//                }
                int REQUEST_CODE = 99;
                int preference = ScanConstants.OPEN_CAMERA;
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                startActivityForResult(intent, REQUEST_CODE);

                break;
            case R.id.menu_gallery:
                final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
            case R.id.menu_media_play:
                String mostRecentUtteranceID = (new Random().nextInt() % 9999999) + ""; // "" is String force
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mostRecentUtteranceID);

                textToSpeech.speak(mResultEditText.getText().toString(), TextToSpeech.QUEUE_FLUSH, params);
                break;
            case R.id.menu_copy:
                String text = mResultEditText.getText().toString();

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null) {
                    Toast.makeText(getApplicationContext(), "Couldn't copy!", Toast.LENGTH_SHORT).show();
                    break;
                }

                ClipData clip = ClipData.newPlainText("Copied Text", text);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Successful copy!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_clear:
                mResultEditText.setText(StringUtils.EMPTY);

                Toast.makeText(getApplicationContext(), "Successful clear!", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_CAPTURE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    CropImage.activity(TesseractOcrTask.outputFileDir).start(this);
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

                new TesseractOcrTask(mHandler, getAssets(), resultUri).execute();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, error.getMessage(), error);
            }

            return;
        }

        if (requestCode == 99 && resultCode == RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setTargetImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            mTargetImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't get bitmap from uri : " + imageUri.getPath(), e);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (HandlerCode.SET_RESULT.equals(msg.what)) {
                mResultEditText.setText((String) msg.obj);
                return;
            }

            if (HandlerCode.START_OCR.equals(msg.what)) {
                progressBarAlertDialog.show();
                return;
            }

            if (HandlerCode.FINISH_OCR.equals(msg.what)) {
                progressBarAlertDialog.cancel();
                return;
            }

            if (HandlerCode.START_TTS.equals(msg.what)) {
                ttsAlertDialog.show();
                return;
            }

            if (HandlerCode.FINISH_TTS.equals(msg.what)) {
                ttsAlertDialog.cancel();
                return;
            }

            if (HandlerCode.FAIL_INITIALIZE_TESS.equals(msg.what)) {
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                return;
            }

            if (HandlerCode.FAIL_CONVERT_BITMAP_TO_TEXT.equals(msg.what)) {
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    public enum HandlerCode {
        SET_RESULT(1),
        START_OCR(2),
        FINISH_OCR(3),
        START_TTS(4),
        FINISH_TTS(5),
        FAIL_INITIALIZE_TESS(6),
        FAIL_CONVERT_BITMAP_TO_TEXT(7);

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