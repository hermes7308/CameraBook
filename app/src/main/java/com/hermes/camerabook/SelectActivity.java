package com.hermes.camerabook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

public class SelectActivity extends AppCompatActivity {
    private static final String TAG = SelectActivity.class.getName();
    public static final String DISABLE_DEATH_ON_FILE_URI_EXPOSURE = "disableDeathOnFileUriExposure";

    private AlertDialog selectMediaDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        // AdMob
        MobileAds.initialize(this, getResources().getString(R.string.ad_app_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener());

        // View
        findViewById(R.id.addNewPage).setOnClickListener(onClickListener);
        findViewById(R.id.openCapturedPage).setOnClickListener(onClickListener);

        LayoutInflater inflater = getLayoutInflater();
        View selectMediaPopup = inflater.inflate(R.layout.select_media_popup, null);
        selectMediaPopup.findViewById(R.id.select_media_popup_camera)
                .setOnClickListener(selectMediaOnClickListener);
        selectMediaPopup.findViewById(R.id.select_media_popup_picture)
                .setOnClickListener(selectMediaOnClickListener);

        selectMediaDialog = new AlertDialog.Builder(this)
                .setView(selectMediaPopup)
                .setCancelable(true)
                .create();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.addNewPage:
                    selectMediaDialog.show();
                    break;
                case R.id.openCapturedPage:
                    openCapturedPage();
                    break;
                default:
                    break;
            }
        }
    };

    private void openCamera() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
        startActivityForResult(intent, RequestCode.CAMERA.getCode());
    }

    private void openMedia() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_MEDIA);
        startActivityForResult(intent, RequestCode.MEDIA.getCode());
    }

    private void openCapturedPage() {
        final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(galleryIntent,  RequestCode.CAPTURED_PAGE.getCode());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == RequestCode.CAMERA.getCode() || requestCode == RequestCode.MEDIA.getCode()) {
            String captureImagePath = data.getExtras().getString(ScanConstants.SCANNED_RESULT);
            Intent intent = new Intent(this, TextDetectionActivity.class);
            intent.putExtra(TextDetectionActivity.CAPTURE_IMAGE_PATH_KEY, captureImagePath);
            startActivity(intent);
            return;
        }

        if (requestCode == RequestCode.CAPTURED_PAGE.getCode()) {
            Uri uri = data.getData();
            Intent intent = new Intent(this, TextDetectionActivity.class);
            intent.putExtra(TextDetectionActivity.IMAGE_URI_KEY, uri);
            startActivity(intent);
            return;
        }
    }

    private View.OnClickListener selectMediaOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.select_media_popup_camera:
                    openCamera();
                    break;
                case R.id.select_media_popup_picture:
                    openMedia();
                    break;
            }
        }
    };

    enum RequestCode {
        CAMERA(1),
        MEDIA(2),
        CAPTURED_PAGE(3);

        private int code;

        RequestCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
