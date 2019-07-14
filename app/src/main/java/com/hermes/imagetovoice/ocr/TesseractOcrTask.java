package com.hermes.imagetovoice.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.hermes.imagetovoice.XSelectActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;

public class TesseractOcrTask extends AsyncTask<Void, String, String> {
    private static final String TAG = TesseractOcrTask.class.getName();

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tess";
    private static final String TESS_DATA_PATH = DATA_PATH + "/tessdata";
    private static final String TESS_IMG_PATH = DATA_PATH + "/img";
    private static final String TESS_TMP_PATH = DATA_PATH + "/tmp";
    private static final String TESS_TRAINEDDATA = ".traineddata";
    private static final String TESS_OUTPUT_IMG_PATH = TESS_IMG_PATH + "/ocr.jpg";

    public static Uri outputFileDir = Uri.fromFile(new File(TESS_OUTPUT_IMG_PATH));

    private Handler mainActivityHandler;
    private AssetManager assetManager;
    private Uri imagePath;

    public TesseractOcrTask(Handler handler, AssetManager assetManager, Uri imagePath) {
        this.mainActivityHandler = handler;
        this.assetManager = assetManager;
        this.imagePath = imagePath;
    }

    @Override
    protected void onPreExecute() {
        sendStartSignal();

        ErrorCode errorCode = prepareTessData();
        if (errorCode != ErrorCode.SUCCESS) {
            sendFailInitializeTessSignal();
            cancel(true);
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        return convertByOCR();
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null) {
            sendFailConvertBitmapToTextSignal();
            cancel(true);
        }
        sendResultText(result);

        sendFinishSignal();
    }

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

    private ErrorCode prepareTessData() {
        try {
            File dir = new File(TESS_DATA_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String[] fileList = assetManager.list("");
            for (String fileName : fileList) {

                if (!StringUtils.endsWith(fileName, TESS_TRAINEDDATA)) {
                    continue;
                }

                String pathToDataFile = TESS_DATA_PATH + File.separator + fileName;
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

    private String convertByOCR() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getPath(), options);

            return convertBitmapToText(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't convert bitmap to text!", e);
            return null;
        }
    }

    private String convertBitmapToText(Bitmap bitmap) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(DATA_PATH, "eng");
        tessBaseAPI.setImage(bitmap);
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ1234567890\'\",.?;:-/@()&");

        String result = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();

        return result;
    }

    private void sendResultText(String text) {
        Message message = new Message();
        message.what = XSelectActivity.HandlerCode.SET_RESULT.getCode();
        message.obj = text;

        mainActivityHandler.sendMessage(message);
    }

    private void sendStartSignal() {
        Message message = new Message();
        message.what = XSelectActivity.HandlerCode.START_OCR.getCode();

        mainActivityHandler.sendMessage(message);
    }

    private void sendFinishSignal() {
        Message message = new Message();
        message.what = XSelectActivity.HandlerCode.FINISH_OCR.getCode();

        mainActivityHandler.sendMessage(message);
    }

    private void sendFailInitializeTessSignal() {
        Message message = new Message();
        message.what = XSelectActivity.HandlerCode.FAIL_INITIALIZE_TESS.getCode();
        message.obj = "Couldn't initialize tess data!";

        mainActivityHandler.sendMessage(message);
    }

    private void sendFailConvertBitmapToTextSignal() {
        Message message = new Message();
        message.what = XSelectActivity.HandlerCode.FAIL_CONVERT_BITMAP_TO_TEXT.getCode();
        message.obj = "Couldn't convert bitmap to text!";

        mainActivityHandler.sendMessage(message);
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
