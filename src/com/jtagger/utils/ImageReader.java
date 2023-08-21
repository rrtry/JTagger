package com.jtagger.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

public class ImageReader {

    public static final List<String> IMAGE_TYPES = Arrays.asList(
            "png", "jpeg", "jpg"
    );

    public static boolean isPictureMimeType(String mimeType) {
        if (!mimeType.contains("/")) {
            return false;
        }

        String[] parts = mimeType.split("/");
        if (parts.length != 2) return false;

        return parts[0].equals("image") && IMAGE_TYPES.contains(parts[1]);
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

        String[] parts = file.getName().split("\\.");
        if (parts.length < 2) {
            return "";
        }

        String type = parts[parts.length - 1];
        if (!IMAGE_TYPES.contains(type)) {
            throw new IllegalArgumentException("File hasn't got image type: " + type);
        }
        return "image/" + type;
    }

    public static byte[] readFromFile(File file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            String contentType = getMimeType(file);
            if (!isPictureMimeType(contentType)) {
                throw new IllegalArgumentException("Invalid mime type: " + contentType);
            }

            byte[] buffer      = new byte[1024];
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

            if (!isPictureMimeType(contentType)) {
                throw new IllegalArgumentException("Invalid mime type: " + contentType);
            }

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
