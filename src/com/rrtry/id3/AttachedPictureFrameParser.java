package com.rrtry.id3;

import java.nio.charset.Charset;
import java.util.Arrays;

public class AttachedPictureFrameParser implements FrameBodyParser<AttachedPictureFrame> {

    private int position;
    private static final int ENCODING_OFFSET = 0;
    private static final int MIME_TYPE_OFFSET = 1;

    @Override
    public AttachedPictureFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        position = 0;

        byte encoding = frameData[ENCODING_OFFSET];
        Charset charset = TextEncoding.getCharset(encoding);
        String mimeType = parseMimeType(frameData);

        byte pictureType = frameData[position];
        String pictureDescription = parsePictureDescription(frameData, charset);

        byte[] pictureData = parsePictureData(frameData);

        return AttachedPictureFrame.newBuilder()
                .setHeader(frameHeader)
                .setMimeType(mimeType)
                .setDescription(pictureDescription)
                .setPictureData(pictureData)
                .setPictureType(pictureType)
                .setEncoding(encoding)
                .build(frameData);
    }

    private byte[] parsePictureData(byte[] frameData) {
        return Arrays.copyOfRange(frameData, ++position, frameData.length);
    }

    private String parsePictureDescription(byte[] frameData, Charset charset) {

        final int from = ++position;

        if (TextEncoding.hasByteOrderMark(frameData, position)) {
            position += TextEncoding.UTF_16_BOM_LENGTH;
        }

        int step = 1;
        if (TextEncoding.isUTF16(charset)) step++;

        while (toCharFromByte(frameData[position]) != '\0') {
            position += step;
        }

        final String description = new String(Arrays.copyOfRange(frameData, from, position), charset);
        if (TextEncoding.isUTF16(charset)) position += 1;
        return description;
    }

    private String parseMimeType(byte[] frameData) {

        position += MIME_TYPE_OFFSET;

        StringBuilder mimeType = new StringBuilder();
        char ch = toCharFromByte(frameData[position]);

        while (ch != '\0' && position < 30) {
            ch = toCharFromByte(frameData[position]);
            mimeType.append(ch);
            position++;
        }
        return mimeType.toString().replace("\0", "");
    }

    private char toCharFromByte(byte b) { return (char) (b & 0xFF); }
}
