package com.hermes.imagetovoice.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.hermes.imagetovoice.MainActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TessOCRTask implements Runnable {
    private MainActivity mainActivity;
    private Handler mainActivityHandler;
    private Uri imagePath;

    public TessOCRTask(MainActivity mainActivity, Uri imagePath) {
        this.mainActivity = mainActivity;
        this.mainActivityHandler = mainActivity.getHandler();
        this.imagePath = imagePath;
    }

    @Override
    public void run() {
        sendStartSignal();

        prepareTessData();
        String result = convertByOCR();
        sendResultText(result);

        sendFinishSignal();
    }

    private void prepareTessData() {
        String fileList[];
        try {
            fileList = mainActivity.getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        AssetManager assetManager = mainActivity.getAssets();
        TessFileManager.prepareTessData(fileList, assetManager);
    }

    private String convertByOCR() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getPath(), options);

            return convertBitmapToText(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Couldn't convert image to text.";
    }

    private String convertBitmapToText(Bitmap bitmap) {
        try {
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(TessFileManager.DATA_PATH, "eng");
            tessBaseAPI.setImage(bitmap);
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ1234567890\'\",.?;:-/@()&");

            String result = tessBaseAPI.getUTF8Text();
            tessBaseAPI.end();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendFinishSignal() {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.FINISH_OCR.getCode();
        mainActivityHandler.sendMessage(message);
    }

    private void sendStartSignal() {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.START_OCR.getCode();
        mainActivityHandler.sendMessage(message);
    }

    private void sendResultText(String text) {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.SET_RESULT.getCode();
        message.obj = text;

        mainActivityHandler.sendMessage(message);
    }
}
