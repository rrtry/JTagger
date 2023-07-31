package com.jtagger.utils;

import com.jtagger.mp3.MpegFrameParser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.ogg.opus.OggOpusParser.OPUS_IDENTIFICATION_HEADER_MAGIC;
import static com.jtagger.ogg.vorbis.VorbisHeader.VORBIS_HEADER_MAGIC;

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
        if (signature.startsWith(id3Magic))  return FileContentTypeDetector.MPEG_MIME_TYPE;
        if (signature.startsWith(flacMagic)) return FileContentTypeDetector.FLAC_MIME_TYPE;
        if (signature.contains(m4aMagic))    return FileContentTypeDetector.M4A_MIME_TYPE;

        if (signature.startsWith(mpegMagic[0]) ||
                signature.startsWith(mpegMagic[1]) ||
                signature.startsWith(mpegMagic[2]))
        {
            return FileContentTypeDetector.MPEG_MIME_TYPE;
        }
        if (signature.startsWith(oggMagic)) {

            file.seek(26);
            int segments = file.readUnsignedByte();
            if (segments > 255 || segments < 0) return null;

            file.skipBytes(segments); // skip segments
            byte[] headerMagic = new byte[OPUS_IDENTIFICATION_HEADER_MAGIC.length];
            file.read(headerMagic);

            if (Arrays.equals(headerMagic, OPUS_IDENTIFICATION_HEADER_MAGIC)) {
                file.seek(0); return FileContentTypeDetector.OGG_OPUS_MIME_TYPE;
            }

            file.seek(file.getFilePointer() - headerMagic.length + 1); // +1 skips header type
            headerMagic = new byte[VORBIS_HEADER_MAGIC.length];
            file.read(headerMagic);

            if (Arrays.equals(headerMagic, VORBIS_HEADER_MAGIC)) {
                file.seek(0); return FileContentTypeDetector.OGG_VORBIS_MIME_TYPE;
            }
        }

        MpegFrameParser parser = new MpegFrameParser();
        parser.parseFrame(file);

        if (parser.getMpegFrame() != null) {
            file.seek(0); return FileContentTypeDetector.MPEG_MIME_TYPE;
        }
        return null;
    }
}
