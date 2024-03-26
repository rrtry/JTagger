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
        String[] parts = mimeType.split("/");
        if (parts.length != 2) return false;
        return parts[0].equals("image") && IMAGE_TYPES.contains(parts[1]);
    }

    public static String getMimeType(URL url) throws IOException {
        return url.openConnection().getContentType();
    }

    public static String getMimeType(File file) {

        String[] parts = file.getName().split("\\.");
        if (parts.length < 2) {
            return "";
        }

        String type = parts[parts.length - 1];
        if (!IMAGE_TYPES.contains(type)) {
            return "";
        }
        return "image/" + type;
    }

    public static byte[] readFromFile(File file) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String contentType = ImageReader.getMimeType(file);

        if (!isPictureMimeType(contentType)) {
            throw new IOException("Invalid mime type: " + contentType);
        }
        try (FileInputStream in = new FileInputStream(file)) {
            BytesIO.copyBytes(in, out);
        }
        return out.toByteArray();
    }

    public static byte[] readFromURL(URL url) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        URLConnection urlConnection = url.openConnection();
        String contentType = getMimeType(url);

        if (!isPictureMimeType(contentType)) {
            throw new IOException("Invalid mime type: " + contentType);
        }
        try (InputStream in = urlConnection.getInputStream()) {
            BytesIO.copyBytes(in, out);
        }
        return out.toByteArray();
    }
}
