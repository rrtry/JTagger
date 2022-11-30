package com.rrtry;

import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.rrtry.AbstractFrame.*;
import static com.rrtry.DateFrame.DATE_FORMAT_PATTERN;
import static com.rrtry.TimeFrame.TIME_FORMAT_PATTERN;

public class TextFrameParser implements FrameBodyParser<TextFrame> {

    public final byte TEXT_FRAME_DATA_OFFSET = 1;
    public final byte TEXT_FRAME_ENCODING_OFFSET = 0;

    @Override
    public final TextFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        byte encoding = parseFrameTextEncoding(frameData);
        String text = parseFrameData(frameData, encoding);

        if (identifier.equals(TIME)) {
            return TimeFrame.createInstance(
                    LocalTime.parse(text, DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN))
            );
        }
        if (identifier.equals(DATE)) {
            return DateFrame.createInstance(
                    MonthDay.parse(text, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))
            );
        }

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
