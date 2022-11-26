package com.rrtry;

import java.util.Arrays;

public class TextFrameParser implements FrameBodyParser<TextFrame> {

    private final int TEXT_FRAME_DATA_OFFSET = 1;
    private final int TEXT_FRAME_ENCODING_OFFSET = 0;

    @Override
    public final TextFrame parse(TagHeader tagHeader, FrameHeader frameHeader, byte[] frameData) {

        byte encoding = parseFrameTextEncoding(frameData);
        String text = parseFrameData(frameData, encoding);

        return TextFrame.newBuilder()
                .setHeader(frameHeader)
                .setText(text)
                .setEncoding(encoding)
                .build(frameData);
    }

    private byte parseFrameTextEncoding(byte[] frameData) {
        return frameData[TEXT_FRAME_ENCODING_OFFSET];
    }

    private String parseFrameData(byte[] frameData, byte encoding) {

        final int from = TEXT_FRAME_DATA_OFFSET;
        final int to = from + frameData.length - 1;

        String textData = new String(
                Arrays.copyOfRange(frameData, from, to),
                TextEncoding.getCharset(encoding)
        );
        return textData.replace("\0", "");
    }
}
