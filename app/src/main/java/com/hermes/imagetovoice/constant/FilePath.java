package com.hermes.imagetovoice.constant;

import android.net.Uri;
import android.os.Environment;

import java.io.File;

public class FilePath {
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/CameraBook";
    private static final String TESS_IMG_PATH = DATA_PATH + "/img";
    private static final String TESS_TMP_PATH = DATA_PATH + "/tmp";
    private static final String CAMERA_OUTPUT_IMG_PATH = TESS_IMG_PATH + "/ocr.jpg";
    public static final Uri CAMERA_OUTPUT_IMAG_URI = Uri.fromFile(new File(CAMERA_OUTPUT_IMG_PATH));

    // Tesseract
    private static final String TESS_DATA_PATH = DATA_PATH + "/tessdata";
    private static final String TESS_TRAINEDDATA = ".traineddata";
}
