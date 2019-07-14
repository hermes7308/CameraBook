package com.hermes.imagetovoice;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int PLAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        YoYo.with(Techniques.FadeIn)
                .duration(PLAY_TIME)
                .withListener(animatorListener)
                .playOn(this.<TextView>findViewById(R.id.intro_logo));

        // Permission
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(multiplePermissionsListener)
                .check();
    }

    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            goToNextActivity();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private MultiplePermissionsListener multiplePermissionsListener = new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport report) {
            if (!report.areAllPermissionsGranted()) {
                Toast.makeText(getApplicationContext(), "If you don't give permission, you can't use this application!", Toast.LENGTH_LONG).show();
                exit();
                return;
            }
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

        }
    };

    private void goToNextActivity() {
        Intent intent = new Intent(this, SelectActivity.class);
        startActivity(intent);
        finish();
    }

    private void exit() {
        ActivityCompat.finishAffinity(this);
        System.runFinalizersOnExit(true);
        System.exit(0);
    }
}
