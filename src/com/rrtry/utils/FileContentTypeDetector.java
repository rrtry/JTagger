package com.rrtry.utils;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class FileContentTypeDetector {

    public static final String MPEG_MIME_TYPE       = "audio/mpeg";
    public static final String FLAC_MIME_TYPE       = "audio/flac";
    public static final String OGG_VORBIS_MIME_TYPE = "audio/x-vorbis+ogg";
    public static final String OGG_OPUS_MIME_TYPE   = "audio/x-opus+ogg";

    private static final HashMap<String, String> mimeTypesMap = new HashMap<>();

    static {
        mimeTypesMap.put("MP3", MPEG_MIME_TYPE);
        mimeTypesMap.put("FLAC", FLAC_MIME_TYPE);
        mimeTypesMap.put("OGG", OGG_VORBIS_MIME_TYPE);
        mimeTypesMap.put("OPUS", OGG_OPUS_MIME_TYPE);
    }

    private static String probeContentType(File file) {
        try {
            return Files.probeContentType(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileContentType(File file) {

        if (!file.isFile()) {
            throw new IllegalArgumentException("Argument must be a regular file");
        }

        String contentType = probeContentType(file);
        if (contentType != null) return contentType;

        String name = file.getName();
        String extension;

        int startIndex = name.lastIndexOf(".");
        if (startIndex == -1) return null; // Does not have an extension

        extension = name.substring(startIndex + 1);
        return mimeTypesMap.get(extension.toUpperCase());
    }
}
