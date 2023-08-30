package com.jtagger.mp3.id3;

import java.util.Arrays;
import static com.jtagger.mp3.id3.AttachedPictureFrame.MIME_TYPE_MAX_LENGTH;

public class AttachedPictureFrameParser extends ContentDescriptionFrameParser<AttachedPictureFrame> {

    private static final int ENCODING_OFFSET  = 0;
    private static final int MIME_TYPE_OFFSET = 1;

    @Override
    public AttachedPictureFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        byte encoding;
        byte pictureType;

        String mimeType;
        String description;

        encoding    = frameData[ENCODING_OFFSET];
        mimeType    = parseMimeType(frameData);
        pictureType = frameData[position++];
        description = parseContentDescription(frameData, TextEncoding.getCharset(encoding));

        return AttachedPictureFrame.newBuilder()
                .setHeader(frameHeader)
                .setMimeType(mimeType)
                .setDescription(description)
                .setPictureData(parsePictureData(frameData))
                .setPictureType(pictureType)
                .setEncoding(encoding)
                .build(frameData);
    }

    private byte[] parsePictureData(byte[] frameData) {
        return Arrays.copyOfRange(frameData, position, frameData.length);
    }

    private String parseMimeType(byte[] frameData) {

        position += MIME_TYPE_OFFSET;
        final int to = position + MIME_TYPE_MAX_LENGTH + 1;

        StringBuilder mimeType = new StringBuilder();
        char ch;

        while ((ch = (char) (frameData[position] & 0xFF)) != '\0' && position < to) {
            mimeType.append(ch);
            position++;
        }
        position++;
        return mimeType.toString();
    }
}
