package com.hermes.imagetovoice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageTransferTask implements Runnable {
    private MainActivity mainActivity;
    private Handler mainActivityHandler;
    private Uri imagePath;

    public ImageTransferTask(MainActivity mainActivity, Uri imagePath) {
        this.mainActivity = mainActivity;
        this.mainActivityHandler = mainActivity.getHandler();
        this.imagePath = imagePath;
    }

    @Override
    public void run() {
        prepareTessData();

        sendStartSignal();

        String result = convertByOCR();
        sendResultText(result);

        sendFinishSignal();
    }

    private void prepareTessData() {
        try {
            File dir = new File(FileManager.TESS_DATA);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileList[] = mainActivity.getAssets().list("");

            for (String fileName : fileList) {

                if (!StringUtils.endsWith(fileName, ".traineddata")) {
                    continue;
                }

                String pathToDataFile = FileManager.TESS_DATA + File.separator + fileName;
                if (new File(pathToDataFile).exists()) {
                    continue;
                }

                InputStream in = mainActivity.getAssets().open(fileName);
                OutputStream out = new FileOutputStream(pathToDataFile);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();

                out.close();
                in.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    private String convertByOCR() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 7;
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
            tessBaseAPI.init(FileManager.DATA_PATH, "eng");
            tessBaseAPI.setImage(bitmap);

            String result = tessBaseAPI.getUTF8Text();
            tessBaseAPI.end();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendResultText(String text) {
        Message message = new Message();
        message.what = MainActivity.HandlerCode.SET_RESULT.getCode();
        message.obj = text;

        mainActivityHandler.sendMessage(message);
    }
}
