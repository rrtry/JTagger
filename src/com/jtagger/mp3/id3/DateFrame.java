package com.jtagger.mp3.id3;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_3;

public class DateFrame extends TextFrame {

    private MonthDay date;
    public static final String DATE_FORMAT_PATTERN = "ddMM";

    {
        setEncoding(TextEncoding.ENCODING_LATIN_1);
    }

    @Override
    public void setText(String text) {
        setDate(MonthDay.parse(text, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s, DATE: %s", getIdentifier(), date.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))
        );
    }

    public void setDate(MonthDay date) {
        this.date = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
        super.setText(date.format(formatter));
    }

    public static DateFrame.Builder newBuilder() { return new DateFrame().new Builder(); }
    public static DateFrame.Builder newBuilder(DateFrame frame) { return frame.new Builder(); }

    public static DateFrame createInstance(MonthDay date) {
        return DateFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(DATE, ID3V2_3))
                .setDate(date)
                .build(ID3V2_3);
    }

    public static DateFrame createInstance(String dateString) {
        return createInstance(
                MonthDay.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))
        );
    }

    public class Builder extends TextFrame.Builder {

        public Builder setHeader(FrameHeader frameHeader) {
            if (!frameHeader.getIdentifier().equals(DATE)) {
                throw new IllegalArgumentException(
                        "DateFrame should have identifier 'TDAT'"
                );
            }
            header = frameHeader;
            return this;
        }

        @Override
        public Builder setText(String text) {
            DateFrame.this.setText(text);
            return this;
        }

        @Override
        public Builder setEncoding(byte encoding) {
            throw new UnsupportedOperationException();
        }

        public Builder setDate(MonthDay date) {
            DateFrame.this.setDate(date);
            return this;
        }

        public DateFrame build(byte version) {
            if (version != ID3V2_3) {
                throw new IllegalArgumentException(
                        "DateFrame is specific to ID3V2.3"
                );
            }
            assemble(version);
            return DateFrame.this;
        }
    }
}
