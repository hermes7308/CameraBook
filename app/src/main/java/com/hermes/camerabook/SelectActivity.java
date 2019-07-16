package com.hermes.camerabook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

public class SelectActivity extends AppCompatActivity {
    private static final String TAG = SelectActivity.class.getName();

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
        findViewById(R.id.camera_area).setOnClickListener(onTouchListener);
        findViewById(R.id.gallery_area).setOnClickListener(onTouchListener);
    }

    private View.OnClickListener onTouchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.camera_area:
                    openCamera();
                    break;
                case R.id.gallery_area:
                    openGallery();
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

    private void openGallery() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_MEDIA);
        startActivityForResult(intent, RequestCode.GALLERY.getCode());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == RequestCode.CAMERA.getCode() || requestCode == RequestCode.GALLERY.getCode()) {
            String captureImagePath = data.getExtras().getString(ScanConstants.SCANNED_RESULT);

            Intent intent = new Intent(this, TextDetectionActivity.class);
            intent.putExtra(TextDetectionActivity.CAPTURE_IMAGE_PATH_KEY, captureImagePath);
            startActivity(intent);
        }
    }

    enum RequestCode {
        CAMERA(1),
        GALLERY(2);

        private int code;

        RequestCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
