package com.jtagger.mp3.id3;

import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_3;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_4;

public class TimestampFrame extends TextFrame {

    public static final String TIME_FORMAT_PATTERN = "HHmm";
    public static final String DATE_FORMAT_PATTERN = "ddMM";
    public static final String YEAR_FORMAT_PATTERN = "yyyy";

    public static final Set<String> V23_IDENTIFIERS = Set.of("TDAT", "TYER", "TIME", "TORY", "TRDA");
    public static final Set<String> V24_IDENTIFIERS = Set.of("TDEN", "TDOR", "TDRC", "TDRL", "TDTG");

    public static final String[] VALID_PATTERNS = new String[] {
            "yyyy", "yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd'T'HH",
            "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss"
    };

    public static final Pattern[] REGEX_PATTERNS = new Pattern[VALID_PATTERNS.length];
    private TemporalAccessor temporal;

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

    public static boolean isValidFrameId(String id) {
        return V23_IDENTIFIERS.contains(id) ||
               V24_IDENTIFIERS.contains(id);
    }

    private int getField(ChronoField field) {
        try {
            return temporal.get(field);
        } catch (DateTimeException e) {
            return -1;
        }
    }

    public TemporalAccessor getTemporal() {
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

        if (text.length() < 4) {
            throw new IllegalArgumentException("Timestamp cannot be less than 4 characters");
        }

        switch (getIdentifier()) {
            case TIME:
                setTime(LocalTime.parse(text.substring(0, 4), DateTimeFormatter.ofPattern("HHmm")));
                return;
            case DATE:
                setMonthDay(MonthDay.parse(text.substring(0, 4), DateTimeFormatter.ofPattern("ddMM")));
                return;
            case YEAR:
                setYear(Year.parse(text.substring(0, 4)));
                return;
        }

        int index = -1;
        for (int i = 0; i < REGEX_PATTERNS.length; i++) {

            Pattern pattern = REGEX_PATTERNS[i];
            Matcher matcher = pattern.matcher(text);

            if (matcher.matches()) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new DateTimeParseException("Invalid timestamp string: " + text, "", 0);
        }

        String format = VALID_PATTERNS[index];
        switch (index) {
            case 0:
                setYear(Year.parse(text));
                break;
            case 1:
                setYearMonth(YearMonth.parse(text, DateTimeFormatter.ofPattern(format)));
                break;
            case 2:
                setDate(LocalDate.parse(text, DateTimeFormatter.ofPattern(format)));
                break;
            default:
                setDateTime(LocalDateTime.parse(text, DateTimeFormatter.ofPattern(format)));
        }
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

    public void setTime(LocalTime localTime) {
        this.temporal = localTime;
        super.setText(localTime.format(DateTimeFormatter.ofPattern("HHmm")));
    }

    public void setMonthDay(MonthDay monthDay) {
        this.temporal = monthDay;
        super.setText(monthDay.format(DateTimeFormatter.ofPattern("ddMM")));
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.temporal = yearMonth;
        super.setText(yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
    }

    public void setDate(LocalDate localDate) {
        this.temporal = localDate;
        super.setText(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    public void setDateTime(LocalDateTime localDateTime) {
        this.temporal = localDateTime;
        super.setText(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
    }

    public static TimestampFrame.Builder newBuilder() { return new TimestampFrame().new Builder(); }
    public static TimestampFrame.Builder newBuilder(TimestampFrame frame) { return frame.new Builder(); }

    public static TimestampFrame createTORY(Year year) {
        return TimestampFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(AbstractFrame.ORIGINAL_RELEASE_YEAR, ID3V2_3))
                .setYear(year)
                .build(ID3V2_3);
    }

    public static TimestampFrame createTYER(Year year) {
        return TimestampFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(AbstractFrame.YEAR, ID3V2_3))
                .setYear(year)
                .build(ID3V2_3);
    }

    public static TimestampFrame createTDAT(MonthDay monthDay) {
        return TimestampFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(AbstractFrame.DATE, ID3V2_3))
                .setMonthDay(monthDay)
                .build(ID3V2_3);
    }

    public static TimestampFrame createTIME(LocalTime time) {
        return TimestampFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(AbstractFrame.TIME, ID3V2_3))
                .setTime(time)
                .build(ID3V2_3);
    }

    public static TimestampFrame createInstance(String identifier, byte version, String timestamp) {
        return TimestampFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(identifier, version))
                .setTimestamp(timestamp)
                .build(version);
    }

    public class Builder extends TextFrame.Builder<Builder, TimestampFrame> {

        public TimestampFrame.Builder setHeader(FrameHeader frameHeader) {
            if ((frameHeader.getVersion() == ID3V2_3 && !V23_IDENTIFIERS.contains(frameHeader.getIdentifier())) ||
                (frameHeader.getVersion() == ID3V2_4 && !V24_IDENTIFIERS.contains(frameHeader.getIdentifier())))
            {
                throw new IllegalArgumentException("Invalid identifier " +
                        frameHeader.getIdentifier() + ", version: " + frameHeader.getVersion());
            }
            header = frameHeader;
            return this;
        }

        @Override
        public Builder setEncoding(byte encoding) {
            TimestampFrame.this.setEncoding(TextEncoding.ENCODING_LATIN_1); // override to allow only LATIN_1
            return this;
        }

        public Builder setTime(LocalTime time) {
            TimestampFrame.this.setTime(time);
            return this;
        }

        public Builder setMonthDay(MonthDay monthDay) {
            TimestampFrame.this.setMonthDay(monthDay);
            return this;
        }

        public Builder setYearMonth(YearMonth yearMonth) {
            TimestampFrame.this.setYearMonth(yearMonth);
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
