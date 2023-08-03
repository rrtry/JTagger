package com.jtagger.mp3.id3;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jtagger.mp3.id3.TimestampFrame.REGEX_PATTERNS;
import static com.jtagger.mp3.id3.TimestampFrame.VALID_PATTERNS;

public class TimestampFrameParser implements FrameBodyParser<TimestampFrame> {

    @Override
    public TimestampFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        String formatPattern = "";
        String recordingTime = new String(
                Arrays.copyOfRange(frameData, 1, frameData.length),
                StandardCharsets.ISO_8859_1
        ).replace("\0", "");

        int index = -1;
        for (int i = 0; i < REGEX_PATTERNS.length; i++) {

            Pattern pattern = REGEX_PATTERNS[i];
            Matcher matcher = pattern.matcher(recordingTime);

            if (matcher.matches()) {
                index         = i;
                formatPattern = VALID_PATTERNS[i];
                break;
            }
        }

        TimestampFrame.Builder builder = TimestampFrame.newBuilder().setHeader(frameHeader);
        if (index == -1) return null;

        if (index == 0) builder = builder.setYear(Year.parse(recordingTime));
        if (index == 1) builder = builder.setYearMonth(YearMonth.parse(recordingTime));
        if (index == 2) builder = builder.setDate(LocalDate.parse(recordingTime, DateTimeFormatter.ofPattern(formatPattern)));
        if (index >= 3) builder = builder.setDateTime(LocalDateTime.parse(recordingTime, DateTimeFormatter.ofPattern(formatPattern)));

        return builder.build(tagHeader.getMajorVersion());
    }
}
