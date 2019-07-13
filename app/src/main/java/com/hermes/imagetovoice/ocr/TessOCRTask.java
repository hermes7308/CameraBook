package com.hermes.imagetovoice.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.hermes.imagetovoice.MainActivity;

public class TessOCRTask implements Runnable {
    private static final String TAG = TessOCRTask.class.getName();

    private Handler mainActivityHandler;
    private AssetManager assetManager;
    private Uri imagePath;

    public TessOCRTask(Handler handler, AssetManager assetManager, Uri imagePath) {
        this.mainActivityHandler = handler;
        this.assetManager = assetManager;
        this.imagePath = imagePath;
    }

    @Override
    public void run() {
        sendStartSignal();

        TessFileManager.ErrorCode errorCode = TessFileManager.prepareTessData(assetManager);
        if (errorCode != TessFileManager.ErrorCode.SUCCESS) {
            sendFailInitializeTessSignal();
            return;
        }

        String result = convertByOCR();
        if (result == null) {
            sendFailConvertBitmapToTextSignal();
            return;
        }

        sendResultText(result);

        sendFinishSignal();
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
        tessBaseAPI.init(TessFileManager.DATA_PATH, "eng");
        tessBaseAPI.setImage(bitmap);
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ1234567890\'\",.?;:-/@()&");

        String result = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();

        return result;
    }

    private void sendResultText(String text) {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.SET_RESULT.getCode();
        message.obj = text;

        mainActivityHandler.sendMessage(message);
    }

    private void sendStartSignal() {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.START_OCR.getCode();

        mainActivityHandler.sendMessage(message);
    }

    private void sendFinishSignal() {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.FINISH_OCR.getCode();

        mainActivityHandler.sendMessage(message);
    }

    private void sendFailInitializeTessSignal() {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.FAIL_INITIALIZE_TESS.getCode();
        message.obj = "Couldn't initialize tess data!";

        mainActivityHandler.sendMessage(message);
    }

    private void sendFailConvertBitmapToTextSignal() {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.FAIL_CONVERT_BITMAP_TO_TEXT.getCode();
        message.obj = "Couldn't convert bitmap to text!";

        mainActivityHandler.sendMessage(message);
    }
}
