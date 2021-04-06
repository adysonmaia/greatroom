package br.ufc.great.greatroom.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by adyson on 02/12/15.
 */
public class LogFileHelper {
    private static final String TAG = LogFileHelper.class.getSimpleName();

    public static void log(String... texts) {
        String filePath = Environment.getExternalStorageDirectory() + "/greatRoom/logs.csv";
        File logFile = new File(filePath);
        if (!logFile.exists()) {
            try {
                File dir = logFile.getParentFile();
                if (dir != null && !dir.exists())
                    dir.mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
            }
        }
        try {
            String text = TextUtils.join(";", texts);
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
            Log.d(TAG, text);
        } catch (IOException e) {
            Log.d(TAG, "error " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void success(String tag, String message, long startTime) {
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        log(tag, message, new Date(startTime).toString(), "" + elapsedTime, "success");
    }

    public static void error(String tag, String message, long startTime, String errorMessage) {
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        LogFileHelper.log(tag, message, new Date(startTime).toString(), "" + elapsedTime, "error", errorMessage);
    }
}
