package com.hermes.imagetovoice.ocr;

import android.content.res.AssetManager;
import android.os.Environment;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TessFileManager {
    static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tess";
    private static final String TESS_DATA_PATH = DATA_PATH + "/tessdata";
    private static final String TESS_IMG_PATH = DATA_PATH + "/imgs";
    private static final String TESS_OUTPUT_IMG_PATH = TESS_IMG_PATH + "/ocr.jpg";

    private static final String TESS_TRAINEDDATA = ".traineddata";

    public static String initTessPathAndGetOutputPath() {
        File dir = new File(TESS_IMG_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return TESS_OUTPUT_IMG_PATH;
    }

    static void prepareTessData(String[] fileList, AssetManager assetManager) {
        try {
            File dir = new File(TessFileManager.TESS_DATA_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (String fileName : fileList) {

                if (!StringUtils.endsWith(fileName, TESS_TRAINEDDATA)) {
                    continue;
                }

                String pathToDataFile = TessFileManager.TESS_DATA_PATH + File.separator + fileName;
                if (new File(pathToDataFile).exists()) {
                    continue;
                }

                InputStream in = assetManager.open(fileName);
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
}
