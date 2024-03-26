package com.jtagger.mp3.id3;

import com.jtagger.utils.IntegerUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.jtagger.mp3.id3.CommentFrame.isISOLanguage;
import static com.jtagger.mp3.id3.TextEncoding.ENCODING_UTF_16;
import static com.jtagger.mp3.id3.TextEncoding.ENCODING_UTF_16_BE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class SynchronisedLyricsFrame extends AbstractFrame<TreeMap<Integer, String>> {

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

    private String language    = Locale.ENGLISH.getISO3Language();
    private String description = "Synchronised lyrics";

    private byte[] synchLyrics;
    private TreeMap<Integer, String> lyricsMap;

    @Override
    public String getKey() {
        return String.format("%s:%s:%s", getIdentifier(), getLanguage(), getDescription());
    }

    @Override
    public byte[] assemble(byte version) {

        final byte encodingOffset    = 0;
        final byte langOffset        = 1;
        final byte timestampOffset   = 4;
        final byte contentTypeOffset = 5;
        final byte descriptionOffset = 6;

        byte[] languageBuffer    = language.getBytes(ISO_8859_1);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte getEncoding() {
        return encoding;
    }

    public void setEncoding(byte encoding) {
        if (!TextEncoding.isValidEncodingByte(encoding)) {
            throw new IllegalArgumentException("Invalid encoding: " + encoding);
        }
        this.encoding = encoding;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (!isISOLanguage(language)) {
            throw new IllegalArgumentException("Invalid language code: " + language);
        }
        this.language = language;
    }

    public byte getContentType() {
        return contentType;
    }

    public void setContentType(byte contentType) {
        this.contentType = contentType;
    }

    public byte getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(byte timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public static SynchronisedLyricsFrame.Builder newBuilder() {
        return new SynchronisedLyricsFrame().new Builder();
    }

    public static SynchronisedLyricsFrame.Builder newBuilder(SynchronisedLyricsFrame frame) {
        return frame.new Builder();
    }

    public static SynchronisedLyricsFrame createInstance(TreeMap<Integer, String> syncLyrics,
                                                         String language,
                                                         byte version)
    {
        return SynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(S_LYRICS, version))
                .setLanguage(language)
                .setLyrics(syncLyrics)
                .build(version);
    }

    @Override
    public TreeMap<Integer, String> getFrameData() {
        return lyricsMap;
    }

    @Override
    public void setFrameData(TreeMap<Integer, String> data) {
        this.lyricsMap = data;
    }

    private void assembleSynchLyrics() {

        int length = 0;
        for (Integer timestamp : lyricsMap.keySet()) {
            byte[] syllableBytes = TextEncoding.getStringBytes(lyricsMap.get(timestamp), encoding);
            length += syllableBytes.length;
            length += 4;
        }

        int index   = 0;
        synchLyrics = new byte[length];

        for (Integer timestamp : lyricsMap.keySet()) {

            String syllable       = lyricsMap.get(timestamp);
            byte[] syllableBytes  = TextEncoding.getStringBytes(syllable, encoding);
            byte[] timestampBytes = IntegerUtils.fromUInt32BE(timestamp);

            System.arraycopy(syllableBytes, 0, synchLyrics, index, syllableBytes.length);
            index += syllableBytes.length;

            System.arraycopy(timestampBytes, 0, synchLyrics, index, 4);
            index += timestampBytes.length;
        }
    }

    private void parseSynchronisedLyrics(byte[] buffer, int position) {

        int strLength;
        int timestamp;
        String syllable;

        lyricsMap = new TreeMap<>();
        while (position < buffer.length) {

            strLength = TextEncoding.getStringLength(buffer, position, encoding);
            syllable  = TextEncoding.getString(buffer, position, strLength, encoding);
            position += strLength;

            timestamp = IntegerUtils.toUInt32BE(Arrays.copyOfRange(buffer, position, position += 4));
            lyricsMap.put(timestamp, syllable);
        }
    }

    @Override
    public void parseFrameData(byte[] buffer, FrameHeader header) {

        String language;
        String description;

        byte encoding;
        byte format;
        byte type;

        int position = 0;
        int strLength;

        encoding = buffer[position++];
        language = new String(Arrays.copyOfRange(buffer, position, position += 3), ISO_8859_1);
        format   = buffer[position++];
        type     = buffer[position++];

        strLength   = TextEncoding.getStringLength(buffer, position, encoding);
        description = TextEncoding.getString(buffer, position, strLength, encoding);
        position += strLength;

        setEncoding(encoding);
        setLanguage(language);
        setDescription(description);
        setContentType(type);
        setTimestampFormat(format);
        parseSynchronisedLyrics(buffer, position);

        this.header = header;
    }

    public class Builder {

        public SynchronisedLyricsFrame.Builder setHeader(FrameHeader header) {
            if (!header.getIdentifier().equals(S_LYRICS)) {
                throw new IllegalArgumentException("Expected frame id: " + S_LYRICS);
            }
            SynchronisedLyricsFrame.this.header = header;
            return this;
        }

        public SynchronisedLyricsFrame.Builder setLyrics(TreeMap<Integer, String> lyrics) {
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

        public SynchronisedLyricsFrame.Builder setContentType(byte contentType) {
            SynchronisedLyricsFrame.this.setContentType(contentType);
            return this;
        }

        public SynchronisedLyricsFrame.Builder setTimestampFormat(byte timestampFormat) {
            SynchronisedLyricsFrame.this.setTimestampFormat(timestampFormat);
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
