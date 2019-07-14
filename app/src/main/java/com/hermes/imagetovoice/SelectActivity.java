package com.hermes.imagetovoice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.IOException;

public class SelectActivity extends AppCompatActivity {
    private static final String TAG = SelectActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
            Log.e(TAG, "RequestCode : " + requestCode + "ResultCode : " + resultCode + ", There are some problem in your intent!");
            return;
        }

        if (requestCode == RequestCode.CAMERA.getCode() || requestCode == RequestCode.GALLERY.getCode()) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = getBitmapWithNullable(uri);
            if (bitmap == null) {
                Toast.makeText(getApplicationContext(), "There are some problem your image path!", Toast.LENGTH_SHORT).show();
                return;
            }

            
            return;
        }
    }

    private Bitmap getBitmapWithNullable(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            getContentResolver().delete(uri, null, null);
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't convert uri to bitmap. uri :" + uri.getPath(), e);
            return null;
        }
    }

    enum RequestCode {
        CAMERA(99),
        GALLERY(99);

        private int code;

        RequestCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
