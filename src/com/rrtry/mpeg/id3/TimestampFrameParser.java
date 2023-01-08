package com.rrtry.mpeg.id3;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.rrtry.mpeg.id3.TimestampFrame.VALID_PATTERNS;

public class TimestampFrameParser implements FrameBodyParser<TimestampFrame> {

    @Override
    public TimestampFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        String formatPattern = "";
        String recordingTime = new String(
                Arrays.copyOfRange(frameData, 1, frameData.length),
                StandardCharsets.ISO_8859_1
        ).replace("\0", "");

        int index = -1;

        for (int i = 0; i < VALID_PATTERNS.length; i++) {
            String pattern = VALID_PATTERNS[i].replace("'", "");
            if (pattern.length() == recordingTime.length()) {
                formatPattern = VALID_PATTERNS[i];
                index = i;
            }
        }

        TimestampFrame.Builder builder = TimestampFrame.createBuilder().setHeader(frameHeader);

        if (index == -1) return null;

        if (index == 0) {
            builder = builder.setYear(Year.parse(recordingTime));
        } else if (index == 1) {
            builder = builder.setYearMonth(YearMonth.parse(recordingTime));
        } else if (index == 2) {
            builder = builder.setDate(LocalDate.parse(recordingTime, DateTimeFormatter.ofPattern(formatPattern)));
        } else {
            builder = builder.setDateTime(LocalDateTime.parse(recordingTime, DateTimeFormatter.ofPattern(formatPattern)));
        }
        return builder.build(tagHeader.getMajorVersion());
    }
}
