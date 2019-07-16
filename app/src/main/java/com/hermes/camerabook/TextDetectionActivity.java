package com.hermes.camerabook;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.scanlibrary.Utils;

import java.io.File;
import java.io.IOException;

public class TextDetectionActivity extends AppCompatActivity {
    private static final String TAG = TextDetectionActivity.class.getName();

    public static final String CAPTURE_IMAGE_PATH_KEY = "captureImagePath";

    private ImageView extractTargetImage;
    private Button extractWordsButton;

    private Bitmap targetImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_detection);

        extractTargetImage = findViewById(R.id.extractTargetImage);
        extractWordsButton = findViewById(R.id.extractWordsButton);

        Bundle bundle = getIntent().getExtras();
        String captureImagePath = (String) bundle.get(CAPTURE_IMAGE_PATH_KEY);

        Uri uri = Uri.fromFile(new File(captureImagePath));
        try {
            targetImage = Utils.getBitmap(this, uri);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't load capture image! : " + captureImagePath, e);
            finish();
            return;
        }

        extractTargetImage.setImageBitmap(targetImage);

    }
}
