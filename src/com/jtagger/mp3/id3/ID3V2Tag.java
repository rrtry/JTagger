package com.jtagger.mp3.id3;

import com.jtagger.AbstractTag;
import com.jtagger.AttachedPicture;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.jtagger.mp3.id3.AbstractFrame.*;
import static com.jtagger.mp3.id3.ID3V1Tag.ID3V1_1;
import static com.jtagger.mp3.id3.TimestampFrame.DATE_FORMAT_PATTERN;
import static com.jtagger.mp3.id3.TagHeaderParser.HEADER_LENGTH;
import static com.jtagger.mp3.id3.TextEncoding.ENCODING_LATIN_1;
import static com.jtagger.mp3.id3.TimestampFrame.TIME_FORMAT_PATTERN;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ID3V2Tag extends ID3Tag {

    public static final HashMap<String, String> FIELD_MAP_V23  = new HashMap<>();
    public static final HashMap<String, String> FIELD_MAP_V24  = new HashMap<>();
    private final LinkedHashMap<String, AbstractFrame> frameMap = new LinkedHashMap<>();

    static {
        FIELD_MAP_V23.put(AbstractTag.TITLE            ,"TIT2");
        FIELD_MAP_V23.put(AbstractTag.ARTIST           ,"TPE1");
        FIELD_MAP_V23.put(AbstractTag.ALBUM            ,"TALB");
        FIELD_MAP_V23.put(AbstractTag.COMMENT          ,"COMM");
        FIELD_MAP_V23.put(AbstractTag.YEAR             ,"TYER");
        FIELD_MAP_V23.put(AbstractTag.TRACK_NUMBER     ,"TRCK");
        FIELD_MAP_V23.put(AbstractTag.GENRE            ,"TCON");
        FIELD_MAP_V23.put(AbstractTag.ID3_GENRE        ,"TCON");
        FIELD_MAP_V23.put(AbstractTag.ALBUM_ARTIST     ,"TPE2");
        FIELD_MAP_V23.put(AbstractTag.ARRANGER         ,"IPLS");
        FIELD_MAP_V23.put(AbstractTag.AUTHOR           ,"TOLY");
        FIELD_MAP_V23.put(AbstractTag.BPM              ,"TBPM");
        FIELD_MAP_V23.put(AbstractTag.COMPILATION      ,"TCMP");
        FIELD_MAP_V23.put(AbstractTag.COMPOSER         ,"TCOM");
        FIELD_MAP_V23.put(AbstractTag.CONDUCTOR        ,"TPE3");
        FIELD_MAP_V23.put(AbstractTag.COPYRIGHT        ,"TCOP");
        FIELD_MAP_V23.put(AbstractTag.DESCRIPTION      ,"TIT3");
        FIELD_MAP_V23.put(AbstractTag.DISC_NUMBER      ,"TPOS");
        FIELD_MAP_V23.put(AbstractTag.ENCODED_BY       ,"TENC");
        FIELD_MAP_V23.put(AbstractTag.ENCODER_SETTINGS ,"TSSE");
        FIELD_MAP_V23.put(AbstractTag.GROUPING         ,"GRP1");
        FIELD_MAP_V23.put(AbstractTag.INITIAL_KEY      ,"TKEY");
        FIELD_MAP_V23.put(AbstractTag.ISRC             ,"TSRC");
        FIELD_MAP_V23.put(AbstractTag.LANGUAGE         ,"TLAN");
        FIELD_MAP_V23.put(AbstractTag.LYRICIST         ,"TEXT");
        FIELD_MAP_V23.put(AbstractTag.LYRICS           ,"USLT");
        FIELD_MAP_V23.put(AbstractTag.MEDIA            ,"TMED");
        FIELD_MAP_V23.put(AbstractTag.ORIGINAL_ALBUM   ,"TOAL");
        FIELD_MAP_V23.put(AbstractTag.ORIGINAL_ARTIST  ,"TOPE");
        FIELD_MAP_V23.put(AbstractTag.ORIGINAL_DATE    ,"TORY");
        FIELD_MAP_V23.put(AbstractTag.PERFORMER        ,"IPLS");
        FIELD_MAP_V23.put(AbstractTag.PICTURE          ,"APIC");
        FIELD_MAP_V23.put(AbstractTag.PUBLISHER        ,"TPUB");
        FIELD_MAP_V23.put(AbstractTag.RATING           ,"POPM");
        FIELD_MAP_V23.put(AbstractTag.REMIXER          ,"TPE4");
        FIELD_MAP_V23.put(AbstractTag.SORT_ALBUM       ,"TSOA");
        FIELD_MAP_V23.put(AbstractTag.SORT_ALBUM_ARTIST,"TSO2");
        FIELD_MAP_V23.put(AbstractTag.SORT_ARTIST      ,"TSOP");
        FIELD_MAP_V23.put(AbstractTag.SORT_COMPOSER    ,"TSOC");
        FIELD_MAP_V23.put(AbstractTag.SORT_NAME        ,"TSOT");
        FIELD_MAP_V23.put(AbstractTag.WEBSITE          ,"WOAR");
        FIELD_MAP_V23.put(AbstractTag.WORK             ,"TIT1");
        FIELD_MAP_V23.put(AbstractTag.WWW_AUDIO_FILE   ,"WOAF");
        FIELD_MAP_V23.put(AbstractTag.WWW_AUDIO_SOURCE ,"WOAS");

        FIELD_MAP_V24.put(AbstractTag.TITLE            ,"TIT2");
        FIELD_MAP_V24.put(AbstractTag.ARTIST           ,"TPE1");
        FIELD_MAP_V24.put(AbstractTag.ALBUM            ,"TALB");
        FIELD_MAP_V24.put(AbstractTag.COMMENT          ,"COMM");
        FIELD_MAP_V24.put(AbstractTag.YEAR             ,"TDRC");
        FIELD_MAP_V24.put(AbstractTag.TRACK_NUMBER     ,"TRCK");
        FIELD_MAP_V24.put(AbstractTag.GENRE            ,"TCON");
        FIELD_MAP_V24.put(AbstractTag.ID3_GENRE        ,"TCON");
        FIELD_MAP_V24.put(AbstractTag.ALBUM_ARTIST     ,"TPE2");
        FIELD_MAP_V24.put(AbstractTag.ARRANGER         ,"TIPL");
        FIELD_MAP_V24.put(AbstractTag.AUTHOR           ,"TOLY");
        FIELD_MAP_V24.put(AbstractTag.BPM              ,"TBPM");
        FIELD_MAP_V24.put(AbstractTag.COMPILATION      ,"TCMP");
        FIELD_MAP_V24.put(AbstractTag.COMPOSER         ,"TCOM");
        FIELD_MAP_V24.put(AbstractTag.CONDUCTOR        ,"TPE3");
        FIELD_MAP_V24.put(AbstractTag.COPYRIGHT        ,"TCOP");
        FIELD_MAP_V24.put(AbstractTag.DESCRIPTION      ,"TIT3");
        FIELD_MAP_V24.put(AbstractTag.DISC_NUMBER      ,"TPOS");
        FIELD_MAP_V24.put(AbstractTag.ENCODED_BY       ,"TENC");
        FIELD_MAP_V24.put(AbstractTag.ENCODER_SETTINGS ,"TSSE");
        FIELD_MAP_V24.put(AbstractTag.ENCODING_TIME    ,"TDEN");
        FIELD_MAP_V24.put(AbstractTag.GROUPING         ,"GRP1");
        FIELD_MAP_V24.put(AbstractTag.INITIAL_KEY      ,"TKEY");
        FIELD_MAP_V24.put(AbstractTag.ISRC             ,"TSRC");
        FIELD_MAP_V24.put(AbstractTag.LANGUAGE         ,"TLAN");
        FIELD_MAP_V24.put(AbstractTag.LYRICIST         ,"TEXT");
        FIELD_MAP_V24.put(AbstractTag.LYRICS           ,"USLT");
        FIELD_MAP_V24.put(AbstractTag.MEDIA            ,"TMED");
        FIELD_MAP_V24.put(AbstractTag.MOOD             ,"TMOO");
        FIELD_MAP_V24.put(AbstractTag.ORIGINAL_ALBUM   ,"TOAL");
        FIELD_MAP_V24.put(AbstractTag.ORIGINAL_ARTIST  ,"TOPE");
        FIELD_MAP_V24.put(AbstractTag.ORIGINAL_DATE    ,"TDOR");
        FIELD_MAP_V24.put(AbstractTag.PERFORMER        ,"TMCL");
        FIELD_MAP_V24.put(AbstractTag.PICTURE          ,"APIC");
        FIELD_MAP_V24.put(AbstractTag.PUBLISHER        ,"TPUB");
        FIELD_MAP_V24.put(AbstractTag.RATING           ,"POPM");
        FIELD_MAP_V24.put(AbstractTag.RELEASE_DATE     ,"TDRL");
        FIELD_MAP_V24.put(AbstractTag.REMIXER          ,"TPE4");
        FIELD_MAP_V24.put(AbstractTag.SORT_ALBUM       ,"TSOA");
        FIELD_MAP_V24.put(AbstractTag.SORT_ALBUM_ARTIST,"TSO2");
        FIELD_MAP_V24.put(AbstractTag.SORT_ARTIST      ,"TSOP");
        FIELD_MAP_V24.put(AbstractTag.SORT_COMPOSER    ,"TSOC");
        FIELD_MAP_V24.put(AbstractTag.SORT_NAME        ,"TSOT");
        FIELD_MAP_V24.put(AbstractTag.SUBTITLE         ,"TSST");
        FIELD_MAP_V24.put(AbstractTag.WEBSITE          ,"WOAR");
        FIELD_MAP_V24.put(AbstractTag.WORK             ,"TIT1");
        FIELD_MAP_V24.put(AbstractTag.WWW_AUDIO_FILE   ,"WOAF");
        FIELD_MAP_V24.put(AbstractTag.WWW_AUDIO_SOURCE ,"WOAS");
    }

    public static final byte ID3V2_3 = 0x03;
    public static final byte ID3V2_4 = 0x04;

    private TagHeader tagHeader;
    private byte[] tagBytes;

    public LinkedHashMap<String, AbstractFrame> getFrameMap() {
        return frameMap;
    }

    public Collection<AbstractFrame> getFrames() {
        return frameMap.values();
    }

    private void setFrames(ArrayList<AbstractFrame> frames) {
        for (AbstractFrame frame : frames) {
            frameMap.put(frame.getKey(), frame);
        }
    }

    private <T extends AbstractFrame> T getFrameById(String id) {
        for (AbstractFrame frame : getFrames()) {
            if (frame.getIdentifier().equals(id)) return (T) frame;
        }
        return null;
    }

    public <T extends AbstractFrame> T getFrame(String key) {
        return (T) frameMap.get(key);
    }

    public void setFrame(AbstractFrame frame) {
        if (frame.getHeader().getVersion() != tagHeader.getMajorVersion()) {
            throw new IllegalArgumentException("Frame version does not match tag version");
        }
        frameMap.put(frame.getKey(), frame);
    }

    private void removeFrameById(String id) {

        Iterator<AbstractFrame> iterator = getFrames().iterator();
        AbstractFrame frame;

        boolean hasDescription = hasContentDescription(id);
        while (iterator.hasNext()) {
            frame = iterator.next();
            if (frame.getIdentifier().equals(id)) {
                iterator.remove();
                if (!hasDescription) break;
            }
        }
    }

    public void removeFrame(String key) {
        frameMap.remove(key);
    }

    public TagHeader getTagHeader() {
        return tagHeader;
    }

    public void setTagHeader(TagHeader header) {
        this.tagHeader = header;
    }

    public int getFrameDataSize(byte version) {
        int size = 0;
        for (AbstractFrame frame : getFrames()) {
            if (tagHeader.isUnsynch() && version == ID3V2_4) {
                frame.getHeader().setFrameUnsynch(true);
            }
            frame.assemble(version);
            size += frame.getHeader().getFrameSize() + HEADER_LENGTH;
        }
        return size;
    }

    private void setCommentFrame(String comment, String language, String description) {
        setFrame(CommentFrame.createInstance(comment, language, description, getVersion()));
    }

    private void setTYER(String year) {
        setFrame(TimestampFrame.createInstance(AbstractFrame.YEAR, ID3V2_3, year));
    }

    private void setTDRC(String year) {
        setFrame(TimestampFrame.createInstance(AbstractFrame.RECORDING_TIME, ID3V2_4, year));
    }

    private void setTextFrame(String id, String text, byte encoding) {
        setFrame(TextFrame.createInstance(id, text, encoding, getVersion()));
    }

    @Override
    public byte[] getBytes() {
        if (tagHeader.getMajorVersion() == ID3V2_3 && tagHeader.isUnsynch()) {

            byte[] header    = tagHeader.getBytes();
            byte[] frameData = UnsynchronisationUtils.toUnsynch(
                    Arrays.copyOfRange(tagBytes, HEADER_LENGTH, tagBytes.length)
            );

            byte[] unsynchTag = new byte[HEADER_LENGTH + frameData.length];
            System.arraycopy(header, 0, unsynchTag, 0, HEADER_LENGTH);
            System.arraycopy(frameData, 0, unsynchTag, header.length, frameData.length);

            return unsynchTag;
        }
        return tagBytes;
    }

    @Override
    public byte getVersion() {
        return getTagHeader().getMajorVersion();
    }

    @Override
    public void setVersion(byte version) {
        tagHeader = TagHeader.newBuilder(tagHeader)
                .setMajorVersion(version)
                .build(version);
    }

    public void setTitle(String title) {
        setTextFrame(AbstractFrame.TITLE, title, TextEncoding.getEncodingForVersion(getVersion()));
    }

    public void setArtist(String artist) {
        setTextFrame(AbstractFrame.ARTIST, artist, TextEncoding.getEncodingForVersion(getVersion()));
    }

    public void setAlbum(String album) {
        setTextFrame(AbstractFrame.ALBUM, album, TextEncoding.getEncodingForVersion(getVersion()));
    }

    public void setAlbumTrack(String trackNum) {
        setTextFrame(AbstractFrame.TRACK_NUMBER, trackNum, ENCODING_LATIN_1);
    }

    public void setComment(String comment, String language, String description) {
        setCommentFrame(comment, language, description);
    }

    public void setGenre(int genre) {
        if (genre >= 0 && genre < ID3V1Tag.UNKNOWN) {
            setFrame(GenreFrame.newBuilder()
                    .setHeader(FrameHeader.createFrameHeader(AbstractFrame.GENRE, getVersion()))
                    .setEncoding(TextEncoding.getEncodingForVersion(getVersion()))
                    .addGenre(genre)
                    .build(getVersion()));
        }
    }

    public void setYear(String year) {
        byte version = getVersion();
        if (version == ID3V2_3) setTYER(year);
        if (version == ID3V2_4) setTDRC(year);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Total size: " + getTagHeader().getTagSize() + "\n");
        for (AbstractFrame frame : getFrames()) {
            stringBuilder.append(frame).append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public byte[] assemble(byte version) {

        if (getFrames().isEmpty()) {
            throw new IllegalStateException("Tag must contain at least one frame");
        }

        tagHeader.setHasExtendedHeader(false);
        tagHeader.setHasFooter(false);

        int tagSize = getFrameDataSize(version);
        byte[] tag = new byte[tagSize + HEADER_LENGTH];
        assembleFrames(getFrames(), version, tag, HEADER_LENGTH);

        this.tagBytes = tag;
        if (tagHeader.isUnsynch() && version == ID3V2_3) {
            byte[] unsynchFrameData = UnsynchronisationUtils.toUnsynch(
                    Arrays.copyOfRange(tagBytes, HEADER_LENGTH, tagBytes.length)
            );
            tagSize = unsynchFrameData.length;
        }

        tagHeader = TagHeader.newBuilder(tagHeader)
                .setMajorVersion(version)
                .setTagSize(tagSize)
                .build(version);

        System.arraycopy(tagHeader.getBytes(), 0, tag, 0, HEADER_LENGTH);
        return tag;
    }

    public static void assembleFrames(Collection<AbstractFrame> frames,
                                      byte version,
                                      byte[] buffer,
                                      int position)
    {
        for (AbstractFrame frame : frames) {

            if (frame.getHeader().getVersion() != version) {
                throw new IllegalStateException(
                        String.format("Frame %s must have same version number as the tag", frame.getHeader().getIdentifier())
                );
            }

            byte[] frameHeader = frame.getHeader().getBytes();
            byte[] frameData   = frame.getBytes();

            System.arraycopy(frameHeader, 0, buffer, position, frameHeader.length);
            System.arraycopy(frameData, 0, buffer, position + frameHeader.length, frameData.length);
            position += frameHeader.length + frameData.length;
        }
    }

    public static Builder newBuilder() {
        return new ID3V2Tag().new Builder();
    }

    public static Builder newBuilder(ID3V2Tag ID3V2Tag) {
        return ID3V2Tag.new Builder();
    }

    private static boolean hasContentDescription(String frameId) {
        return frameId.equals(AbstractFrame.COMMENT) || frameId.equals(AbstractFrame.PICTURE);
    }

    public void mergeWithID3V1(ID3V1Tag id3v1) {

        String[] fields = new String[] {
                AbstractTag.TITLE, AbstractTag.ARTIST, AbstractTag.ALBUM,
                AbstractTag.YEAR, AbstractTag.COMMENT, AbstractTag.TRACK_NUMBER
        };

        String value;
        int length = id3v1.getVersion() == ID3V1_1 ? 6 : 5;

        for (int i = 0; i < length; i++) {
            String field = fields[i];
            if (getFrameFromFieldName(field) == null) {
                value = id3v1.getFieldValue(field);
                if (!value.isBlank()) {
                    setFieldValue(field, value);
                }
            }
        }
    }

    private static void convertToID3V23Tag(ID3V2Tag tag) {

        TimestampFrame timestampFrame   = tag.getFrame(AbstractFrame.RECORDING_TIME);
        TimestampFrame releaseTimeFrame = tag.getFrame(AbstractFrame.ORIGINAL_RELEASE_TIME);

        if (releaseTimeFrame != null) {
            Year year = releaseTimeFrame.getYear();
            if (year != null) tag.setFrame(TimestampFrame.createTORY(year));
        }
        if (timestampFrame != null) {

            Year year      = timestampFrame.getYear();
            MonthDay date  = timestampFrame.getMonthDay();
            LocalTime time = timestampFrame.getTime();

            if (year != null) tag.setFrame(TimestampFrame.createTYER(year));
            if (date != null) tag.setFrame(TimestampFrame.createTDAT(date));
            if (time != null) tag.setFrame(TimestampFrame.createTIME(time));

            tag.removeFrame(AbstractFrame.RECORDING_TIME);
            tag.removeFrame(AbstractFrame.ORIGINAL_RELEASE_TIME);
        }
    }

    private static void convertToID3V24Tag(ID3V2Tag tag) {

        final byte version = ID3V2_4;

        StringBuilder dateString = new StringBuilder();
        StringBuilder pattern    = new StringBuilder();

        TimestampFrame releaseYear = tag.getFrame(AbstractFrame.ORIGINAL_RELEASE_YEAR);
        TimestampFrame yearFrame   = tag.getFrame(AbstractFrame.YEAR);
        TimestampFrame timeFrame   = tag.getFrame(AbstractFrame.TIME);
        TimestampFrame dateFrame   = tag.getFrame(AbstractFrame.DATE);

        if (releaseYear != null) {
            TimestampFrame releaseTime = TimestampFrame.newBuilder()
                    .setHeader(FrameHeader.createFrameHeader(AbstractFrame.ORIGINAL_RELEASE_TIME, version))
                    .setYear(releaseYear.getYear())
                    .build(version);
            tag.setFrame(releaseTime);
        }

        boolean isYear      = false;
        boolean isDate      = false;
        boolean isDateTime  = false;

        final String yearPattern = "yyyy";

        if (yearFrame != null) {
            pattern.append(yearPattern);
            dateString.append(yearFrame.getText());
            isYear = true;
        }
        if (isYear && dateFrame != null) {
            pattern.append("-").append(DATE_FORMAT_PATTERN);
            dateString.append("-").append(dateFrame.getText());
            isDate = true;
        }
        if (isYear && isDate && timeFrame != null) {
            pattern.append("-").append(TIME_FORMAT_PATTERN);
            dateString.append("-").append(timeFrame.getText());
            isDateTime = true;
        }

        tag.removeFrame(AbstractFrame.YEAR);
        tag.removeFrame(AbstractFrame.DATE);
        tag.removeFrame(AbstractFrame.TIME);
        tag.removeFrame(AbstractFrame.ORIGINAL_RELEASE_YEAR);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern.toString());
        try {

            TimestampFrame.Builder builder = TimestampFrame.newBuilder();
            builder = builder.setHeader(FrameHeader.createFrameHeader(AbstractFrame.RECORDING_TIME, version));

            if (isDateTime)  builder = builder.setDateTime(LocalDateTime.parse(dateString, formatter));
            else if (isDate) builder = builder.setDate(LocalDate.parse(dateString, formatter));
            else if (isYear) builder = builder.setYear(Year.parse(dateString.toString()));
            else return;

            TimestampFrame timestampFrame = builder.build(version);
            tag.setFrame(timestampFrame);

        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }
    }

    public static ID3V2Tag convertID3V2Tag(ID3V2Tag tag, byte version) {

        if (tag.getVersion() == version) return tag;
        Collection<AbstractFrame> frames = tag.getFrames();

        tag.getTagHeader().setMajorVersion(version);
        for (AbstractFrame frame : frames) {
            frame.getHeader().setTagVersion(version);
            frame.assemble(version);
        }

        if (version == ID3V2_4) {
            convertToID3V24Tag(tag);
            frames.removeIf((frame) -> V23_DEPRECATED_FRAMES.contains(frame.getIdentifier()));
        }
        if (version == ID3V2_3) {
            convertToID3V23Tag(tag);
            frames.removeIf((frame) -> V24_NEW_FRAMES.contains(frame.getIdentifier()));
        }
        return ID3V2Tag.newBuilder(tag).build(version);
    }

    public static ID3V2Tag fromID3V1Tag(ID3V1Tag id3v1Tag, byte version) {

        TagHeader header = TagHeader.newBuilder(version)
                .setMajorVersion(version)
                .build(version);

        ID3V2Tag.Builder builder = ID3V2Tag.newBuilder()
                .setHeader(header)
                .setGenre(id3v1Tag.getGenre())
                .setComment(id3v1Tag.getComment())
                .setYear(id3v1Tag.getYear())
                .setTitle(id3v1Tag.getTitle())
                .setAlbum(id3v1Tag.getAlbum())
                .setArtist(id3v1Tag.getArtist());

        if (id3v1Tag.getVersion() == ID3V1_1) {
            builder = builder.setAlbumTrack(String.valueOf(id3v1Tag.getTrackNumber()));
        }
        return builder.build(version);
    }

    private String getFrameIdFromFieldName(String field) {
        HashMap<String, String> fieldMap = getVersion() == ID3V2_3 ? FIELD_MAP_V23 : FIELD_MAP_V24;
        return fieldMap.get(field);
    }

    private <T> AbstractFrame<T> getFrameFromFieldName(String field) {
        String frameId = getFrameIdFromFieldName(field);
        if (frameId == null) return null;
        return hasContentDescription(frameId) ? getFrameById(frameId) : getFrame(frameId);
    }

    @Override
    protected <T> T getFieldValue(String field) {
        AbstractFrame<T> frame = getFrameFromFieldName(field);
        if (frame == null) return null;
        return field.equals(AbstractTag.ID3_GENRE) ?
                (T) ((TextFrame) frame).getText() :
                frame.getFrameData();
    }

    @Override
    public void removeField(String field) {

        String frameId = getFrameIdFromFieldName(field);
        if (frameId == null) return;

        if (hasContentDescription(frameId)) {
            removeFrameById(frameId);
        } else {
            removeFrame(frameId);
        }
    }

    @Override
    protected <T> void setFieldValue(String field, T value) {

        AbstractFrame<T> frame = getFrameFromFieldName(field);
        if (frame != null) {
            frame.setFrameData(value);
            return;
        }

        String frameId = getFrameIdFromFieldName(field);
        if (frameId == null) return;

        //"TDAT", "TYER", "TIME", "TORY", "TRDA" id3v2.4
        //"TDEN", "TDOR", "TDRC", "TDRL", "TDTG" id3v2.3
        switch (frameId) {

            case "TCON":
                frame = (AbstractFrame<T>) GenreFrame.newBuilder()
                        .setHeader(FrameHeader.createFrameHeader(frameId, getVersion()))
                        .setText((String) value)
                        .build(getVersion());
                break;
            case "APIC":
                removeFrameById(frameId);
                frame = (AbstractFrame<T>) AttachedPictureFrame.newBuilder()
                        .setHeader(FrameHeader.createFrameHeader(frameId, getVersion()))
                        .setAttachedPicture((AttachedPicture) value)
                        .build(getVersion());
                break;
            case "COMM":
                removeFrameById(frameId);
                frame = (AbstractFrame<T>) CommentFrame.newBuilder()
                        .setHeader(FrameHeader.createFrameHeader(frameId, getVersion()))
                        .setLanguage("XXX")
                        .setDescription("DEFAULT")
                        .setText((String) value)
                        .build(getVersion());
                break;

            case "TDAT":
            case "TYER":
            case "TIME":
            case "TORY":
            case "TRDA":
            case "TDEN":
            case "TDOR":
            case "TDRC":
            case "TDRL":
            case "TDTG":
                try {
                    frame = (AbstractFrame<T>) TimestampFrame.newBuilder()
                            .setHeader(FrameHeader.createFrameHeader(frameId, getVersion()))
                            .setTimestamp((String) value)
                            .build(getVersion());
                } catch (DateTimeParseException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            default:
                frame = (AbstractFrame<T>) TextFrame.newBuilder()
                        .setHeader(FrameHeader.createFrameHeader(frameId, getVersion()))
                        .setEncoding(TextEncoding.getEncodingForVersion(getVersion()))
                        .setText((String) value)
                        .build(getVersion());
        }
        if (frame != null) {
            setFrame(frame);
        }
    }

    public class Builder {

        public Builder setTitle(String title) {
            ID3V2Tag.this.setTitle(title);
            return this;
        }

        public Builder setGenre(int genre) {
            ID3V2Tag.this.setGenre(genre);
            return this;
        }

        public Builder setYear(String year) {
            ID3V2Tag.this.setYear(year);
            return this;
        }

        public Builder setComment(String comment) {
            ID3V2Tag.this.setComment(comment, "XXX", "DEFAULT");
            return this;
        }

        public Builder setAlbumTrack(String trackNum) {
            ID3V2Tag.this.setAlbumTrack(trackNum);
            return this;
        }

        public Builder setArtist(String artist) {
            ID3V2Tag.this.setArtist(artist);
            return this;
        }

        public Builder setAlbum(String album) {
            ID3V2Tag.this.setAlbum(album);
            return this;
        }

        public Builder setHeader(TagHeader tagHeader) {
            ID3V2Tag.this.tagHeader = tagHeader;
            return this;
        }

        public Builder setFrames(ArrayList<AbstractFrame> frames) {
            ID3V2Tag.this.setFrames(frames);
            return this;
        }

        public ID3V2Tag build() {
            return ID3V2Tag.this;
        }

        public ID3V2Tag build(byte version) {
            assemble(version);
            return ID3V2Tag.this;
        }
    }
}
