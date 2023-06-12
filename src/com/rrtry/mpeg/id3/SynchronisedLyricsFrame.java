package com.rrtry.mpeg.id3;

import com.rrtry.utils.IntegerUtils;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class SynchronisedLyricsFrame extends AbstractFrame<HashMap<Integer, String>> {

    public static final byte TIMESTAMP_FORMAT_MILLIS = 0x2;
    public static final byte TIMESTAMP_FORMAT_FRAMES = 0x1;

    public static final byte CONTENT_TYPE_OTHER         = 0x0;
    public static final byte CONTENT_TYPE_LYRICS        = 0x1;
    public static final byte CONTENT_TYPE_TRANSCRIPTION = 0x2;
    public static final byte CONTENT_TYPE_PART_NAME     = 0x3;
    public static final byte CONTENT_TYPE_EVENT         = 0x4;
    public static final byte CONTENT_TYPE_CHORD         = 0x5;
    public static final byte CONTENT_TYPE_TRIVIA        = 0x6;
    public static final byte CONTENT_TYPE_WEBPAGE_URL   = 0x7;
    public static final byte CONTENT_TYPE_PICTURE_URL   = 0x8;

    private byte encoding;
    private byte timestampFormat = TIMESTAMP_FORMAT_MILLIS;
    private byte contentType     = CONTENT_TYPE_LYRICS;

    private String language;
    private String description;

    private byte[] synchLyrics;
    private HashMap<Integer, String> lyricsMap;

    private void assembleSynchLyrics() {

        int length = 0;

        for (Integer timestamp : lyricsMap.keySet()) {
            byte[] lineBytes = TextEncoding.getStringBytes(lyricsMap.get(timestamp), encoding);
            length += lineBytes.length;
            length += 4;
        }

        int index   = 0;
        synchLyrics = new byte[length];

        SortedSet<Integer> keys = new TreeSet<>(lyricsMap.keySet());
        for (Integer timestamp : keys) {

            String line = lyricsMap.get(timestamp);

            byte[] lineBytes      = TextEncoding.getStringBytes(line, encoding);
            byte[] timestampBytes = IntegerUtils.fromUInt32BE(timestamp);

            System.arraycopy(lineBytes, 0, synchLyrics, index, lineBytes.length); index += lineBytes.length;
            System.arraycopy(timestampBytes, 0, synchLyrics, index, 4);     index += timestampBytes.length;
        }
    }

    @Override
    public byte[] assemble(byte version) {

        final byte encodingOffset    = 0;
        final byte langOffset        = 1;
        final byte timestampOffset   = 4;
        final byte contentTypeOffset = 5;
        final byte descriptionOffset = 6;

        byte[] languageBuffer    = language.getBytes(TextEncoding.getCharset(encoding));
        byte[] descriptionBuffer = TextEncoding.getStringBytes(description, encoding);

        assembleSynchLyrics();

        int size = 3 + languageBuffer.length + descriptionBuffer.length + synchLyrics.length;
        byte[] frame = new byte[size];

        frame[encodingOffset]    = encoding;
        frame[timestampOffset]   = timestampFormat;
        frame[contentTypeOffset] = contentType;

        System.arraycopy(languageBuffer, 0, frame, langOffset, languageBuffer.length);
        System.arraycopy(descriptionBuffer, 0, frame, descriptionOffset, descriptionBuffer.length);
        System.arraycopy(synchLyrics, 0, frame, descriptionOffset + descriptionBuffer.length, synchLyrics.length);

        this.frameBytes = frame;
        header = FrameHeader.newBuilder(header)
                .setFrameSize(getBytes().length)
                .build(version);

        return frame;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void setEncoding(byte encoding) {
        if (!TextEncoding.isValidEncodingByte(encoding)) {
            throw new IllegalArgumentException("Invalid encoding: " + encoding);
        }
        this.encoding = encoding;
    }

    public void setLanguage(String language) {
        if (language.length() != 3) throw new IllegalArgumentException("Invalid language code: " + language);
        this.language = language;
    }

    public static SynchronisedLyricsFrame.Builder newBuilder() {
        return new SynchronisedLyricsFrame().new Builder();
    }

    public static SynchronisedLyricsFrame.Builder newBuilder(SynchronisedLyricsFrame frame) {
        return frame.new Builder();
    }

    public static SynchronisedLyricsFrame createInstance(HashMap<Integer, String> syncLyrics,
                                                         String language,
                                                         byte version)
    {
        return SynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(S_LYRICS, version))
                .setLanguage(language)
                .setDescription("")
                .setLyrics(syncLyrics)
                .build(version);
    }

    @Override
    public HashMap<Integer, String> getFrameData() {
        return lyricsMap;
    }

    @Override
    public void setFrameData(HashMap<Integer, String> data) {
        this.lyricsMap = data;
    }

    public class Builder {

        public SynchronisedLyricsFrame.Builder setHeader(FrameHeader header) {
            SynchronisedLyricsFrame.this.header = header;
            return this;
        }

        public SynchronisedLyricsFrame.Builder setLyrics(HashMap<Integer, String> lyrics) {
            SynchronisedLyricsFrame.this.setFrameData(lyrics);
            return this;
        }

        public SynchronisedLyricsFrame.Builder setDescription(String description) {
            SynchronisedLyricsFrame.this.setDescription(description);
            return this;
        }

        public SynchronisedLyricsFrame.Builder setEncoding(byte encoding) {
            SynchronisedLyricsFrame.this.setEncoding(encoding);
            return this;
        }

        public SynchronisedLyricsFrame.Builder setLanguage(String language) {
            SynchronisedLyricsFrame.this.setLanguage(language);
            return this;
        }

        public SynchronisedLyricsFrame build(byte[] frameData) {
            SynchronisedLyricsFrame.this.frameBytes = frameData;
            return SynchronisedLyricsFrame.this;
        }

        public SynchronisedLyricsFrame build(byte version) {
            assemble(version);
            return SynchronisedLyricsFrame.this;
        }
    }
}
