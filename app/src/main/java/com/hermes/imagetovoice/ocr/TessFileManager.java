package com.hermes.imagetovoice.ocr;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;

public class TessFileManager {
    private static final String TAG = TessFileManager.class.getName();

    static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tess";
    private static final String TESS_DATA_PATH = DATA_PATH + "/tessdata";
    private static final String TESS_IMG_PATH = DATA_PATH + "/imgs";
    private static final String TESS_TRAINEDDATA = ".traineddata";

    public static final String TESS_OUTPUT_IMG_PATH = TESS_IMG_PATH + "/ocr.jpg";

    public static void initTessPath() {
        try {
            File dir = new File(TESS_IMG_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

        } catch (Exception e) {
            Log.e(TAG, "Couldn't initialize tess path directory!");
        }
    }

    static ErrorCode prepareTessData(AssetManager assetManager) {
        try {
            File dir = new File(TessFileManager.TESS_DATA_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String[] fileList = assetManager.list("");
            for (String fileName : fileList) {

                if (!StringUtils.endsWith(fileName, TESS_TRAINEDDATA)) {
                    continue;
                }

                String pathToDataFile = TessFileManager.TESS_DATA_PATH + File.separator + fileName;
                if (new File(pathToDataFile).exists()) {
                    continue;
                }

                InputStream in = assetManager.open(fileName);
                FileUtils.copyInputStreamToFile(in, new File(pathToDataFile));
                in.close();
            }

            return ErrorCode.SUCCESS;
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.NOT_INITIALIZED;
            Log.e(TAG, errorCode.getErrorMessage(), e);

            return errorCode;
        }
    }

    enum ErrorCode {
        SUCCESS(0, "Tesseract file initialized successfully!"),
        NOT_INITIALIZED(1, "Tesseract file is not initialized!");

        private int code;
        private String errorMessage;

        ErrorCode(int code, String errorMessage) {
            this.code = code;
            this.errorMessage = errorMessage;
        }

        public int getCode() {
            return code;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
