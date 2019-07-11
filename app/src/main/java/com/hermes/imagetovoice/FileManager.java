package com.hermes.imagetovoice;

import android.os.Environment;

public class FileManager {
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tess";
    public static final String TESS_DATA = DATA_PATH + "/tessdata";
}
