package com.jtagger.mp3.id3;

import java.time.Year;
import java.time.format.DateTimeFormatter;

import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_3;

public class YearFrame extends TextFrame {

    private Year year;
    public static final String YEAR_FORMAT_PATTERN = "yyyy";

    {
        setEncoding(TextEncoding.ENCODING_LATIN_1);
    }

    public static boolean isYearFrame(String id) {
        return id.equals(YEAR) || id.equals(ORIGINAL_RELEASE_YEAR);
    }

    @Override
    public void setText(String text) {
        setYear(Year.parse(text));
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s, YEAR: %s", getIdentifier(), year
        );
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YEAR_FORMAT_PATTERN);
        super.setText(year.format(formatter));
    }

    public static YearFrame.Builder newBuilder() { return new YearFrame().new Builder(); }
    public static YearFrame.Builder newBuilder(YearFrame frame) { return frame.new Builder(); }

    public static YearFrame createInstance(String frameId, Year year) {
        return YearFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(frameId, ID3V2_3))
                .setYear(year)
                .build(ID3V2_3);
    }

    public class Builder extends TextFrame.Builder<Builder, YearFrame> {

        @Override
        public YearFrame.Builder setHeader(FrameHeader frameHeader) {
            if (!isYearFrame(frameHeader.getIdentifier())) {
                throw new IllegalArgumentException(
                        "YearFrame should have identifier 'TYER' or 'TORY'"
                );
            }
            header = frameHeader;
            return this;
        }

        @Override
        public Builder setEncoding(byte encoding) {
            throw new UnsupportedOperationException();
        }

        public YearFrame.Builder setYear(Year year) {
            YearFrame.this.setYear(year);
            return this;
        }

        @Override
        public YearFrame build(byte version) {
            if (version != ID3V2_3) {
                throw new IllegalArgumentException(
                        "YearFrame is specific to ID3V2.3"
                );
            }
            assemble(version);
            return YearFrame.this;
        }
    }
}
