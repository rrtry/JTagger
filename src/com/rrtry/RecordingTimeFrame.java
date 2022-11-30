package com.rrtry;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.regex.Pattern;

public class RecordingTimeFrame extends TextFrame {

    public static final String[] VALID_PATTERNS = new String[] {
            "yyyy", "yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd'T'HH",
            "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss"
    };

    private Temporal temporal;

    {
        setEncoding(TextEncoding.ENCODING_LATIN_1);
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

    public LocalTime getTime() {

        int hour   = getField(ChronoField.HOUR_OF_DAY);
        int minute = getField(ChronoField.MINUTE_OF_HOUR);

        if (hour == -1 || minute == -1) {
            return null;
        }
        return LocalTime.of(hour, minute);
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

    public static RecordingTimeFrame.Builder createBuilder() { return new RecordingTimeFrame().new Builder(); }
    public static RecordingTimeFrame.Builder newBuilder(RecordingTimeFrame frame) { return frame.new Builder(); }

    public class Builder {

        public RecordingTimeFrame.Builder setHeader(FrameHeader frameHeader) {
            if (!frameHeader.getIdentifier().equals(RECORDING_TIME) &&
                    !frameHeader.getIdentifier().equals(ORIGINAL_RELEASE_TIME))
            {
                throw new IllegalArgumentException(
                        "RecordingTimeFrame should have identifier 'TDRC' or 'TDOR'"
                );
            }
            header = frameHeader;
            return this;
        }

        public Builder setYearMonth(YearMonth date) {
            RecordingTimeFrame.this.setYearMonth(date);
            return this;
        }

        public Builder setYear(Year year) {
            RecordingTimeFrame.this.setYear(year);
            return this;
        }

        public Builder setDate(LocalDate localDate) {
            RecordingTimeFrame.this.setDate(localDate);
            return this;
        }

        public Builder setDateTime(LocalDateTime localDateTime) {
            RecordingTimeFrame.this.setDateTime(localDateTime);
            return this;
        }

        public RecordingTimeFrame build(byte[] frameData) {
            RecordingTimeFrame.this.frameBytes = frameData;
            return RecordingTimeFrame.this;
        }

        public RecordingTimeFrame build(byte version) {
            assemble(version);
            return RecordingTimeFrame.this;
        }
    }
}
