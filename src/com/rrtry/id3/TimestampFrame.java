package com.rrtry.id3;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Arrays;

public class TimestampFrame extends TextFrame {

    private static final String[] VALID_IDENTIFIERS = new String[] {
            "TDEN", "TDOR", "TDRC", "TDRL", "TDTG"
    };

    public static final String[] VALID_PATTERNS = new String[] {
            "yyyy", "yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd'T'HH",
            "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss"
    };

    private Temporal temporal;

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

    public Year getYear() {
        int year = getField(ChronoField.YEAR);
        if (year == -1) {
            return null;
        }
        return Year.of(year);
    }

    public MonthDay getMonthDay() {

        int day = getField(ChronoField.DAY_OF_MONTH);
        int month = getField(ChronoField.MONTH_OF_YEAR);

        if (month == -1 || day == -1) {
            return null;
        }
        return MonthDay.of(month, day);
    }

    public LocalDate getDate() {

        Year year = getYear();
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
        setYear(Year.parse(text));
    }

    @Override
    public void setText(String text) {
        throw new UnsupportedOperationException();
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

    public static TimestampFrame.Builder createBuilder() { return new TimestampFrame().new Builder(); }
    public static TimestampFrame.Builder newBuilder(TimestampFrame frame) { return frame.new Builder(); }

    public class Builder {

        public TimestampFrame.Builder setHeader(FrameHeader frameHeader) {
            if (!Arrays.asList(VALID_IDENTIFIERS).contains(frameHeader.getIdentifier())) {
                throw new IllegalArgumentException(
                        "Invalid identifier"
                );
            }
            header = frameHeader;
            return this;
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

        public TimestampFrame build(byte[] frameData) {
            TimestampFrame.this.frameBytes = frameData;
            return TimestampFrame.this;
        }

        public TimestampFrame build(byte version) {
            assemble(version);
            return TimestampFrame.this;
        }
    }
}
