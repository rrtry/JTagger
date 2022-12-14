package com.rrtry.mpeg.id3;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import static com.rrtry.mpeg.id3.ID3V2Tag.ID3V2_3;

public class TimeFrame extends TextFrame {

    private LocalTime time;
    public static final String TIME_FORMAT_PATTERN = "HHmm";

    {
        setEncoding(TextEncoding.ENCODING_LATIN_1);
    }

    @Override
    public void setText(String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s, TIME: %s", getIdentifier(), time
        );
    }

    public void setTime(LocalTime time) {
        this.time = time;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);
        super.setText(time.format(formatter));
    }

    public static TimeFrame.Builder createBuilder() { return new TimeFrame().new Builder(); }
    public static TimeFrame.Builder newBuilder(TimeFrame frame) { return frame.new Builder(); }

    public static TimeFrame createInstance(LocalTime localTime) {
        return TimeFrame.createBuilder()
                .setHeader(FrameHeader.createFrameHeader(TIME, ID3V2_3))
                .setTime(localTime)
                .build(ID3V2_3);
    }

    public class Builder {

        public Builder setHeader(FrameHeader frameHeader) {
            if (!frameHeader.getIdentifier().equals(TIME)) {
                throw new IllegalArgumentException(
                        "TimeFrame should have identifier 'TIME'"
                );
            }
            header = frameHeader;
            return this;
        }

        public Builder setTime(LocalTime localTime) {
            TimeFrame.this.setTime(localTime);
            return this;
        }

        public TimeFrame build(byte version) {
            if (version != ID3V2_3) {
                throw new IllegalArgumentException(
                        "TimeFrame is specific to ID3V2.3"
                );
            }
            assemble(version);
            return TimeFrame.this;
        }
    }
}
