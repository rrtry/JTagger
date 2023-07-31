package com.jtagger.mp3.id3;

import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import static com.jtagger.mp3.id3.AbstractFrame.*;
import static com.jtagger.mp3.id3.DateFrame.DATE_FORMAT_PATTERN;
import static com.jtagger.mp3.id3.TimeFrame.TIME_FORMAT_PATTERN;
import static com.jtagger.mp3.id3.YearFrame.YEAR_FORMAT_PATTERN;

public class TextFrameParser implements FrameBodyParser<TextFrame> {

    public final byte TEXT_FRAME_DATA_OFFSET     = 1;
    public final byte TEXT_FRAME_ENCODING_OFFSET = 0;

    @Override
    public final TextFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        byte encoding = parseFrameTextEncoding(frameData);
        String text   = parseFrameData(frameData, encoding);

        try {
            if (TimestampFrame.isTimestampFrame(identifier)) {
                return TimestampFrame.createInstance(identifier, text);
            }
            if (identifier.equals(TIME)) return TimeFrame.createInstance(LocalTime.parse(text, DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN)));
            if (identifier.equals(DATE)) return DateFrame.createInstance(MonthDay.parse(text, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
            if (identifier.equals(YEAR)) return YearFrame.createInstance(identifier, Year.parse(text, DateTimeFormatter.ofPattern(YEAR_FORMAT_PATTERN)));
        } catch (DateTimeParseException e) {
            return null;
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
        final int to   = from + frameData.length - 1;

        String textData = new String(
                Arrays.copyOfRange(frameData, from, to),
                TextEncoding.getCharset(encoding)
        );
        return textData.replace("\0", "");
    }
}
