package br.ufc.great.greatroom.util;

/**
 * Created by belmondorodrigues on 18/11/2015.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
    private static final String TAG = Downloader.class.getSimpleName();
    private static final int MEGABYTE = 1024 * 1024;

    public static void downloadFile(String fileUrl, File directory) {
        long startTime = System.currentTimeMillis();
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(directory);
            int totalSize = urlConnection.getContentLength();

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();
            LogFileHelper.success(TAG, fileUrl, startTime);
        } catch (Exception e) {
            LogFileHelper.error(TAG, fileUrl, startTime, e.getMessage());
        }
    }
}
