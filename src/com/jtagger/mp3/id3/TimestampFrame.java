package com.jtagger.mp3.id3;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_4;

public class TimestampFrame extends TextFrame {

    private static final String[] VALID_IDENTIFIERS = new String[] {
            "TDEN", "TDOR", "TDRC", "TDRL", "TDTG"
    };

    public static final String[] VALID_PATTERNS = new String[] {
            "yyyy", "yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd'T'HH",
            "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss"
    };

    public static final Pattern[] REGEX_PATTERNS = new Pattern[VALID_PATTERNS.length];
    private Temporal temporal;

    static {
        REGEX_PATTERNS[0] = Pattern.compile("\\d\\d\\d\\d");
        REGEX_PATTERNS[1] = Pattern.compile("\\d\\d\\d\\d-\\d\\d");
        REGEX_PATTERNS[2] = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
        REGEX_PATTERNS[3] = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d");
        REGEX_PATTERNS[4] = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d");
        REGEX_PATTERNS[5] = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d");
    }

    {
        setEncoding(TextEncoding.ENCODING_LATIN_1);
    }

    public static boolean isTimestampFrame(String id) {
        return Arrays.asList(VALID_IDENTIFIERS).contains(id);
    }

    private int getField(ChronoField field) {
        try {
            return temporal.get(field);
        } catch (DateTimeException e) {
            return -1;
        }
    }

    public Temporal getTemporal() {
        return temporal;
    }

    public Year getYear() {
        int year = getField(ChronoField.YEAR);
        if (year == -1) {
            return null;
        }
        return Year.of(year);
    }

    public MonthDay getMonthDay() {

        int day   = getField(ChronoField.DAY_OF_MONTH);
        int month = getField(ChronoField.MONTH_OF_YEAR);

        if (month == -1 || day == -1) {
            return null;
        }
        return MonthDay.of(month, day);
    }

    public LocalDate getDate() {

        Year year         = getYear();
        MonthDay monthDay = getMonthDay();

        if (year == null || monthDay == null) {
            return null;
        }
        return LocalDate.of(
                year.getValue(), monthDay.getMonth(), monthDay.getDayOfMonth()
        );
    }

    public LocalDateTime getTimestamp() {

        LocalDate date = getDate();
        LocalTime time = getTime();

        if (date == null || time == null) {
            return null;
        }
        return LocalDateTime.of(date, time);
    }

    public LocalTime getTime() {

        int hour   = getField(ChronoField.HOUR_OF_DAY);
        int minute = getField(ChronoField.MINUTE_OF_HOUR);

        if (hour == -1 || minute == -1) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }

    @Override
    public String getFrameData() {
        return getText();
    }

    @Override
    public void setFrameData(String text) {
        setText(text);
    }

    @Override
    public void setText(String text) {

        int index     = -1;
        String format = "";

        for (int i = 0; i < REGEX_PATTERNS.length; i++) {

            Pattern pattern = REGEX_PATTERNS[i];
            Matcher matcher = pattern.matcher(text);

            if (matcher.matches()) {
                format = VALID_PATTERNS[i];
                index  = i;
                break;
            }
        }

        if (index == -1) {
            throw new IllegalArgumentException("Invalid timestamp string: " + text);
        }

        if (index == 0) setYear(Year.parse(text));
        if (index == 1) setYearMonth(YearMonth.parse(text, DateTimeFormatter.ofPattern(format)));
        if (index == 2) setDate(LocalDate.parse(text, DateTimeFormatter.ofPattern(format)));
        if (index >= 3) setDateTime(LocalDateTime.parse(text, DateTimeFormatter.ofPattern(format)));
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s, RECORDING_TIME: %s", getIdentifier(), temporal
        );
    }

    public void setYear(Year year) {
        this.temporal = year;
        super.setText(year.toString());
    }

    public void setYearMonth(YearMonth date) {
        this.temporal = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        super.setText(date.format(formatter));
    }

    public void setDate(LocalDate localDate) {
        this.temporal = localDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        super.setText(formatter.format(localDate));
    }

    public void setDateTime(LocalDateTime localDateTime) {
        this.temporal = localDateTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        super.setText(formatter.format(localDateTime));
    }

    public static TimestampFrame.Builder newBuilder() { return new TimestampFrame().new Builder(); }
    public static TimestampFrame.Builder newBuilder(TimestampFrame frame) { return frame.new Builder(); }

    public static TimestampFrame createInstance(String identifier, String timestamp) {
        return TimestampFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(identifier, ID3V2_4))
                .setTimestamp(timestamp)
                .build(ID3V2_4);
    }

    public class Builder extends TextFrame.Builder<Builder, TimestampFrame> {

        public TimestampFrame.Builder setHeader(FrameHeader frameHeader) {
            if (!Arrays.asList(VALID_IDENTIFIERS).contains(frameHeader.getIdentifier())) {
                throw new IllegalArgumentException(
                        "Invalid identifier"
                );
            }
            header = frameHeader;
            return this;
        }

        @Override
        public Builder setEncoding(byte encoding) {
            throw new UnsupportedOperationException();
        }

        public Builder setYearMonth(YearMonth date) {
            TimestampFrame.this.setYearMonth(date);
            return this;
        }

        public Builder setYear(Year year) {
            TimestampFrame.this.setYear(year);
            return this;
        }

        public Builder setDate(LocalDate localDate) {
            TimestampFrame.this.setDate(localDate);
            return this;
        }

        public Builder setDateTime(LocalDateTime localDateTime) {
            TimestampFrame.this.setDateTime(localDateTime);
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            TimestampFrame.this.setFrameData(timestamp);
            return this;
        }
    }
}
