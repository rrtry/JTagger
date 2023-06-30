package com.jtagger.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class FileContentTypeDetector {

    public static final String MPEG_MIME_TYPE       = "audio/mpeg";
    public static final String FLAC_MIME_TYPE       = "audio/flac";
    public static final String OGG_VORBIS_MIME_TYPE = "audio/x-vorbis+ogg";
    public static final String OGG_OPUS_MIME_TYPE   = "audio/x-opus+ogg";
    public static final String M4A_MIME_TYPE        = "audio/m4a";

    public static String getFileContentType(RandomAccessFile file) throws IOException {

        final String oggMagic    = "OggS";
        final String id3Magic    = "ID3";
        final String flacMagic   = "fLaC";
        final String m4aMagic    = "ftypM4A";
        final String[] mpegMagic = new String[] { "ÿû", "ÿó", "ÿò" };

        byte[] buffer = new byte[20];
        file.read(buffer);
        file.seek(0);

        final String signature = new String(buffer, StandardCharsets.ISO_8859_1);
        if (signature.startsWith(oggMagic))  return FileContentTypeDetector.OGG_VORBIS_MIME_TYPE;
        if (signature.startsWith(id3Magic))  return FileContentTypeDetector.MPEG_MIME_TYPE;
        if (signature.startsWith(flacMagic)) return FileContentTypeDetector.FLAC_MIME_TYPE;
        if (signature.contains(m4aMagic))    return FileContentTypeDetector.M4A_MIME_TYPE;

        if (signature.startsWith(mpegMagic[0]) ||
                signature.startsWith(mpegMagic[1]) ||
                signature.startsWith(mpegMagic[2]))
        {
            return FileContentTypeDetector.MPEG_MIME_TYPE;
        }
        return null;
    }
}