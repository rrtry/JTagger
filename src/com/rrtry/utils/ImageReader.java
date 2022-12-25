package com.rrtry.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageReader {

    private static boolean isPictureMimeType(String mimeType) {
        if (mimeType.isEmpty()) return false;
        return mimeType.substring(0, mimeType.indexOf("/")).equals("image");
    }

    public static String getMimeType(URL url) {
        try {
            URLConnection connection = url.openConnection();
            return connection.getContentType();
        } catch (IOException e) {
            return "";
        }
    }

    public static String getMimeType(File file) {
        try {
            return Files.probeContentType(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            return "";
        }
    }

    public static byte[] readFromFile(File file) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            String contentType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
            if (!isPictureMimeType(contentType)) throw new IllegalArgumentException("Invalid mime type: " + contentType);

            byte[] buffer = new byte[1024];

            FileInputStream in = new FileInputStream(file);
            while (in.read(buffer, 0, buffer.length) != -1) {
                out.write(buffer, 0, buffer.length);
            }

            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static byte[] readFromURL(URL url) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            URLConnection urlConnection = url.openConnection();
            String contentType = urlConnection.getContentType();

            if (!isPictureMimeType(contentType)) throw new IllegalArgumentException("Invalid mime type: " + contentType);

            InputStream in = urlConnection.getInputStream();
            int data;
            while ((data = in.read()) != -1) {
                out.write(data);
            }

            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}
