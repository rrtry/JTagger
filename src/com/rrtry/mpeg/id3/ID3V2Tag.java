package com.rrtry.mpeg.id3;

import com.rrtry.Tag;
import com.rrtry.AttachedPicture;
import com.rrtry.PaddingTag;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.rrtry.mpeg.id3.AbstractFrame.V2_3_FRAMES;
import static com.rrtry.mpeg.id3.AbstractFrame.V2_4_FRAMES;
import static com.rrtry.mpeg.id3.DateFrame.DATE_FORMAT_PATTERN;
import static com.rrtry.mpeg.id3.TagHeaderParser.HEADER_LENGTH;
import static com.rrtry.mpeg.id3.TextEncoding.ENCODING_LATIN_1;
import static com.rrtry.mpeg.id3.TimeFrame.TIME_FORMAT_PATTERN;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ID3V2Tag extends ID3Tag implements PaddingTag {

    public static final String[] DEPRECATED_V23_FRAMES = new String[] {
            "EQUA", "IPLS", "RVAD", "TDAT", "TIME", "TORY", "TRDA", "TSIZ", "TYER"
    };

    private static final HashMap<String, String> FIELD_MAP_V23 = new HashMap<>();
    private static final HashMap<String, String> FIELD_MAP_V24 = new HashMap<>();

    static {
        FIELD_MAP_V23.put(Tag.TITLE            ,"TIT2");
        FIELD_MAP_V23.put(Tag.ARTIST           ,"TPE1");
        FIELD_MAP_V23.put(Tag.ALBUM            ,"TALB");
        FIELD_MAP_V23.put(Tag.COMMENT          ,"COMM");
        FIELD_MAP_V23.put(Tag.YEAR             ,"TYER");
        FIELD_MAP_V23.put(Tag.TRACK_NUMBER     ,"TRCK");
        FIELD_MAP_V23.put(Tag.GENRE            ,"TCON");
        FIELD_MAP_V23.put(Tag.ALBUM_ARTIST     ,"TPE2");
        FIELD_MAP_V23.put(Tag.ARRANGER         ,"IPLS");
        FIELD_MAP_V23.put(Tag.AUTHOR           ,"TOLY");
        FIELD_MAP_V23.put(Tag.BPM              ,"TBPM");
        FIELD_MAP_V23.put(Tag.COMPILATION      ,"TCMP");
        FIELD_MAP_V23.put(Tag.COMPOSER         ,"TCOM");
        FIELD_MAP_V23.put(Tag.CONDUCTOR        ,"TPE3");
        FIELD_MAP_V23.put(Tag.COPYRIGHT        ,"TCOP");
        FIELD_MAP_V23.put(Tag.DESCRIPTION      ,"TIT3");
        FIELD_MAP_V23.put(Tag.DISC_NUMBER      ,"TPOS");
        FIELD_MAP_V23.put(Tag.ENCODED_BY       ,"TENC");
        FIELD_MAP_V23.put(Tag.ENCODER_SETTINGS ,"TSSE");
        FIELD_MAP_V23.put(Tag.GROUPING         ,"GRP1");
        FIELD_MAP_V23.put(Tag.INITIAL_KEY      ,"TKEY");
        FIELD_MAP_V23.put(Tag.ISRC             ,"TSRC");
        FIELD_MAP_V23.put(Tag.LANGUAGE         ,"TLAN");
        FIELD_MAP_V23.put(Tag.LYRICIST         ,"TEXT");
        FIELD_MAP_V23.put(Tag.LYRICS           ,"USLT");
        FIELD_MAP_V23.put(Tag.MEDIA            ,"TMED");
        FIELD_MAP_V23.put(Tag.ORIGINAL_ALBUM   ,"TOAL");
        FIELD_MAP_V23.put(Tag.ORIGINAL_ARTIST  ,"TOPE");
        FIELD_MAP_V23.put(Tag.ORIGINAL_DATE    ,"TORY");
        FIELD_MAP_V23.put(Tag.PERFORMER        ,"IPLS");
        FIELD_MAP_V23.put(Tag.PICTURE          ,"APIC");
        FIELD_MAP_V23.put(Tag.PUBLISHER        ,"TPUB");
        FIELD_MAP_V23.put(Tag.RATING           ,"POPM");
        FIELD_MAP_V23.put(Tag.REMIXER          ,"TPE4");
        FIELD_MAP_V23.put(Tag.SORT_ALBUM       ,"TSOA");
        FIELD_MAP_V23.put(Tag.SORT_ALBUM_ARTIST,"TSO2");
        FIELD_MAP_V23.put(Tag.SORT_ARTIST      ,"TSOP");
        FIELD_MAP_V23.put(Tag.SORT_COMPOSER    ,"TSOC");
        FIELD_MAP_V23.put(Tag.SORT_NAME        ,"TSOT");
        FIELD_MAP_V23.put(Tag.WEBSITE          ,"WOAR");
        FIELD_MAP_V23.put(Tag.WORK             ,"TIT1");
        FIELD_MAP_V23.put(Tag.WWW_AUDIO_FILE   ,"WOAF");
        FIELD_MAP_V23.put(Tag.WWW_AUDIO_SOURCE ,"WOAS");

        FIELD_MAP_V24.put(Tag.TITLE            ,"TIT2");
        FIELD_MAP_V24.put(Tag.ARTIST           ,"TPE1");
        FIELD_MAP_V24.put(Tag.ALBUM            ,"TALB");
        FIELD_MAP_V24.put(Tag.COMMENT          ,"COMM");
        FIELD_MAP_V24.put(Tag.YEAR             ,"TDRC");
        FIELD_MAP_V24.put(Tag.TRACK_NUMBER     ,"TRCK");
        FIELD_MAP_V24.put(Tag.GENRE            ,"TCON");
        FIELD_MAP_V24.put(Tag.ALBUM_ARTIST     ,"TPE2");
        FIELD_MAP_V24.put(Tag.ARRANGER         ,"TIPL");
        FIELD_MAP_V24.put(Tag.AUTHOR           ,"TOLY");
        FIELD_MAP_V24.put(Tag.BPM              ,"TBPM");
        FIELD_MAP_V24.put(Tag.COMPILATION      ,"TCMP");
        FIELD_MAP_V24.put(Tag.COMPOSER         ,"TCOM");
        FIELD_MAP_V24.put(Tag.CONDUCTOR        ,"TPE3");
        FIELD_MAP_V24.put(Tag.COPYRIGHT        ,"TCOP");
        FIELD_MAP_V24.put(Tag.DESCRIPTION      ,"TIT3");
        FIELD_MAP_V24.put(Tag.DISC_NUMBER      ,"TPOS");
        FIELD_MAP_V24.put(Tag.ENCODED_BY       ,"TENC");
        FIELD_MAP_V24.put(Tag.ENCODER_SETTINGS ,"TSSE");
        FIELD_MAP_V24.put(Tag.ENCODING_TIME    ,"TDEN");
        FIELD_MAP_V24.put(Tag.GROUPING         ,"GRP1");
        FIELD_MAP_V24.put(Tag.INITIAL_KEY      ,"TKEY");
        FIELD_MAP_V24.put(Tag.ISRC             ,"TSRC");
        FIELD_MAP_V24.put(Tag.LANGUAGE         ,"TLAN");
        FIELD_MAP_V24.put(Tag.LYRICIST         ,"TEXT");
        FIELD_MAP_V24.put(Tag.LYRICS           ,"USLT");
        FIELD_MAP_V24.put(Tag.MEDIA            ,"TMED");
        FIELD_MAP_V24.put(Tag.MOOD             ,"TMOO");
        FIELD_MAP_V24.put(Tag.ORIGINAL_ALBUM   ,"TOAL");
        FIELD_MAP_V24.put(Tag.ORIGINAL_ARTIST  ,"TOPE");
        FIELD_MAP_V24.put(Tag.ORIGINAL_DATE    ,"TDOR");
        FIELD_MAP_V24.put(Tag.PERFORMER        ,"TMCL");
        FIELD_MAP_V24.put(Tag.PICTURE          ,"APIC");
        FIELD_MAP_V24.put(Tag.PUBLISHER        ,"TPUB");
        FIELD_MAP_V24.put(Tag.RATING           ,"POPM");
        FIELD_MAP_V24.put(Tag.RELEASE_DATE     ,"TDRL");
        FIELD_MAP_V24.put(Tag.REMIXER          ,"TPE4");
        FIELD_MAP_V24.put(Tag.SORT_ALBUM       ,"TSOA");
        FIELD_MAP_V24.put(Tag.SORT_ALBUM_ARTIST,"TSO2");
        FIELD_MAP_V24.put(Tag.SORT_ARTIST      ,"TSOP");
        FIELD_MAP_V24.put(Tag.SORT_COMPOSER    ,"TSOC");
        FIELD_MAP_V24.put(Tag.SORT_NAME        ,"TSOT");
        FIELD_MAP_V24.put(Tag.SUBTITLE         ,"TSST");
        FIELD_MAP_V24.put(Tag.WEBSITE          ,"WOAR");
        FIELD_MAP_V24.put(Tag.WORK             ,"TIT1");
        FIELD_MAP_V24.put(Tag.WWW_AUDIO_FILE   ,"WOAF");
        FIELD_MAP_V24.put(Tag.WWW_AUDIO_SOURCE ,"WOAS");
    }

    public static final byte ID3V2   = 0x02;
    public static final byte ID3V2_3 = 0x03;
    public static final byte ID3V2_4 = 0x04;

    private TagHeader tagHeader;
    private ArrayList<AbstractFrame> frames = new ArrayList<>();
    private byte[] tagBytes;

    private int padding = MIN_PADDING;

    public TagHeader getTagHeader() {
        return tagHeader;
    }

    public ArrayList<AbstractFrame> getFrames() {
        return frames;
    }

    public AbstractFrame getFrameAt(int index)  {
        return frames.get(index);
    }

    public TextFrame getTextFrame(String id) {
        return getFrame(id);
    }

    public <T extends AbstractFrame> T getFrame(String id) {

        boolean knownFrame = false;
        if (getVersion() == ID3V2_3) knownFrame = Arrays.asList(V2_3_FRAMES).contains(id);
        if (getVersion() == ID3V2_4) knownFrame = Arrays.asList(V2_4_FRAMES).contains(id);

        if (!knownFrame) return null;

        for (AbstractFrame frame : frames) {
            if (frame.getIdentifier().equals(id)) {
                return (T) frame;
            }
        }
        return null;
    }

    private <T> AbstractFrame<T> getFrameFromFieldName(String field) {
        String frameId = getFrameIdFromFieldName(field);
        return frameId != null ? getFrame(frameId) : null;
    }

    public void addFrame(AbstractFrame frame) {
        frames.add(frame);
    }

    public void setFrameAtIndex(int index, AbstractFrame frame) {
        frames.set(index, frame);
    }

    public void setTagHeader(TagHeader header) {
        this.tagHeader = header;
    }

    public void setFrame(AbstractFrame frame) {
        int index = indexOf(frame.getIdentifier());
        if (index != -1) setFrameAtIndex(indexOf(frame.getIdentifier()), frame);
        else addFrame(frame);
    }

    public boolean removePictures() {
        return removeFrame(PICTURE);
    }

    public boolean removeFrame(String id) {
        return frames.removeIf(frame -> frame.getIdentifier().equals(id));
    }

    public int getFrameDataSize(byte version) {

        int size = 0;

        for (AbstractFrame frame : frames) {
            if (tagHeader.isUnsynch() && version == ID3V2_4) {
                frame.getHeader().setFrameUnsynch(true);
            }
            frame.assemble(version);
            size += frame.getHeader().getFrameSize() + HEADER_LENGTH;
        }
        return size;
    }

    public int indexOf(String id) {

        int index = -1;

        for (AbstractFrame frame : frames) {
            if (frame.getIdentifier().equals(id)) {
                index = frames.indexOf(frame);
                break;
            }
        }
        return index;
    }

    public void setPictureFrame(AttachedPictureFrame newFrame, int index) {

        newFrame = AttachedPictureFrame.newBuilder(newFrame).build(getVersion()); // assemble frame

        if (index != -1 && getFrameAt(index) instanceof AttachedPictureFrame) {
            setFrameAtIndex(index, newFrame);
            return;
        }
        throw new IllegalArgumentException("Index must be non-negative and frame at the position must have id 'APIC'");
    }

    private void setCommentFrame(String comment, String language) {
        setFrame(CommentFrame.createInstance(comment, language, getVersion()));
    }

    private void setRecordingYear(String id, String year) {
        TimestampFrame timestampFrame = TimestampFrame.createBuilder()
                .setHeader(FrameHeader.createFrameHeader(id, ID3V2_4))
                .setYear(Year.parse(year))
                .build(ID3V2_4);
        setFrame(timestampFrame);
    }

    private void setTextFrame(String id, String text, byte encoding) {
        setFrame(TextFrame.createInstance(id, text, encoding, getVersion()));
    }

    public void addPictureURL(URL url, String description, byte pictureType) {
        final String mimeType = "-->";
        addPicture(
                url.toString().getBytes(StandardCharsets.ISO_8859_1),
                description,
                mimeType,
                pictureType
        );
    }

    public void addPicture(byte[] buffer, String description, String mimeType, byte pictureType) {
        addFrame(AttachedPictureFrame.createInstance(description, mimeType, pictureType, buffer, getVersion()));
    }

    public void addPictureFromURL(URL url, String description, byte pictureType) throws IOException {
        addPictureFrame(url, description, pictureType);
    }

    public void addPictureFromFile(File file, String description, byte pictureType) throws IOException {
        addPictureFrame(file, description, pictureType);
    }

    private void addPictureFrame(URL url, String description, byte pictureType) throws IOException {
        addFrame(
                AttachedPictureFrame.newBuilder()
                        .setHeader(FrameHeader.createFrameHeader(AbstractFrame.PICTURE, getVersion()))
                        .setEncoding(TextEncoding.getAppropriateEncoding(getVersion()))
                        .setPictureType(pictureType)
                        .setDescription(description)
                        .setPictureData(url)
                        .build(getVersion())
        );
    }

    private void addPictureFrame(File file, String description, byte pictureType) throws IOException {
        String mimeType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
        addFrame(
                AttachedPictureFrame.newBuilder()
                        .setHeader(FrameHeader.createFrameHeader(AbstractFrame.PICTURE, getVersion()))
                        .setEncoding(TextEncoding.getAppropriateEncoding(getVersion()))
                        .setMimeType(mimeType)
                        .setPictureType(pictureType)
                        .setDescription(description)
                        .setPictureData(file)
                        .build(getVersion())
        );
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
    public int getPaddingAmount() {
        return padding;
    }

    @Override
    public void setPaddingAmount(int padding) {
        if (padding == 0 || (padding >= MIN_PADDING && padding <= MAX_PADDING)) {
            this.padding = padding;
        }
    }

    public String getTitle() {
        TextFrame titleFrame = getTextFrame(AbstractFrame.TITLE);
        return titleFrame == null ? "" : titleFrame.getText();
    }

    public String getArtist() {
        TextFrame artistFrame = getTextFrame(AbstractFrame.ARTIST);
        return artistFrame == null ? "" : artistFrame.getText();
    }

    public String getAlbum() {
        TextFrame albumFrame = getTextFrame(AbstractFrame.ALBUM);
        return albumFrame == null ? "" : albumFrame.getText();
    }

    public String getRecordingTimestamp() {
        if (getVersion() == ID3V2_4) {
            TimestampFrame timestampFrame = getFrame(AbstractFrame.RECORDING_TIME);
            return timestampFrame == null ? "" : timestampFrame.getText();
        }
        return "";
    }

    public String getRecordingTime() {

        String recTime = "";

        if (getVersion() == ID3V2_3) {
            TimeFrame timeFrame = (TimeFrame) getTextFrame(AbstractFrame.TIME);
            recTime = timeFrame == null ? "" : timeFrame.getText();
        }
        if (getVersion() == ID3V2_4) {
            TimestampFrame timestampFrame = getFrame(AbstractFrame.RECORDING_TIME);
            LocalTime time = timestampFrame.getTime();
            recTime = time == null ? "" : time.toString();
        }
        return recTime;
    }

    public String getRecordingDate() {

        String recDate = "";

        if (getVersion() == ID3V2_3) {
            DateFrame dateFrame = (DateFrame) getTextFrame(AbstractFrame.YEAR);
            recDate = dateFrame == null ? "" : dateFrame.getText();
        }
        if (getVersion() == ID3V2_4) {
            TimestampFrame timestampFrame = getFrame(AbstractFrame.RECORDING_TIME);
            LocalDate date = timestampFrame.getDate();
            recDate = date == null ? "" : date.toString();
        }
        return recDate;
    }

    public String getYear() {

        String year = "";

        if (getVersion() == ID3V2_3) {
            TextFrame yearFrame = getTextFrame(AbstractFrame.YEAR);
            year = yearFrame == null ? "" : yearFrame.getText();
        }
        if (getVersion() == ID3V2_4) {
            TimestampFrame timestampFrame = getFrame(AbstractFrame.RECORDING_TIME);
            year = timestampFrame == null ? "" : timestampFrame.getYear().toString();
        }
        return year;
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
        setTextFrame(AbstractFrame.TITLE, title, TextEncoding.getAppropriateEncoding(getVersion()));
    }

    public void setArtist(String artist) {
        setTextFrame(AbstractFrame.ARTIST, artist, TextEncoding.getAppropriateEncoding(getVersion()));
    }

    public void setAlbum(String album) {
        setTextFrame(AbstractFrame.ALBUM, album, TextEncoding.getAppropriateEncoding(getVersion()));
    }

    public void setAlbumTrack(String trackNum) {
        setTextFrame(AbstractFrame.TRACK_NUMBER, trackNum, ENCODING_LATIN_1);
    }

    public void setGenre(int genre) {
        String genreString = ID3V1Tag.GENRES[genre];
        setTextFrame(AbstractFrame.GENRE, genreString, ENCODING_LATIN_1);
    }

    public void setGenre(String genre) {
        setTextFrame(AbstractFrame.GENRE, genre, TextEncoding.getAppropriateEncoding(getVersion()));
    }

    public void setYear(String year) {
        byte version = getVersion();
        if (version == ID3V2_3) setTextFrame(AbstractFrame.YEAR, year, ENCODING_LATIN_1);
        if (version == ID3V2_4) setRecordingYear(AbstractFrame.RECORDING_TIME, year);
    }

    public void setComment(String comment, String language) {
        setCommentFrame(comment, language);
    }

    public boolean removeYear() {
        if (getVersion() == ID3V2_3) return removeFrame(AbstractFrame.YEAR);
        else if (getVersion() == ID3V2_4) return removeFrame(AbstractFrame.RECORDING_TIME);
        throw new IllegalArgumentException("Invalid version number: " + getVersion());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Total size: " + getTagHeader().getTagSize() + "\n");
        for (AbstractFrame frame : frames) {
            stringBuilder.append(frame).append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public byte[] assemble(byte version) {

        if (frames.isEmpty()) {
            throw new IllegalStateException("Tag must contain at least one frame");
        }

        tagHeader.setHasExtendedHeader(false);
        tagHeader.setHasFooter(false);

        int tagSize  = getFrameDataSize(version) + padding;
        int position = HEADER_LENGTH;

        byte[] tag = new byte[tagSize + HEADER_LENGTH];

        for (AbstractFrame frame : frames) {

            if (frame.getHeader().getVersion() != version) {
                throw new IllegalStateException(
                        String.format("Frame %s must have same version number as the tag", frame.getHeader().getIdentifier())
                );
            }

            byte[] frameHeader = frame.getHeader().getBytes();
            byte[] frameData = frame.getBytes();

            System.arraycopy(frameHeader, 0, tag, position, frameHeader.length);
            System.arraycopy(frameData, 0, tag, position + frameHeader.length, frameData.length);

            position += frameHeader.length + frameData.length;
        }

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

    public static Builder newBuilder() { return new ID3V2Tag().new Builder(); }
    public static Builder newBuilder(ID3V2Tag ID3V2Tag) { return ID3V2Tag.new Builder(); }

    private String getFrameIdFromFieldName(String field) {
        HashMap<String, String> fieldMap = getVersion() == ID3V2_3 ? FIELD_MAP_V23 : FIELD_MAP_V24;
        return fieldMap.get(field);
    }

    private static void convertToID3V23Tag(ID3V2Tag id3V2Tag) {

        final byte version = ID3V2_3;
        ArrayList<AbstractFrame> frames = id3V2Tag.getFrames();

        TimestampFrame timestampFrame = id3V2Tag.getFrame(AbstractFrame.RECORDING_TIME);
        TextFrame releaseTimeFrame = id3V2Tag.getFrame(AbstractFrame.ORIGINAL_RELEASE_TIME);

        if (releaseTimeFrame != null && releaseTimeFrame.getText().length() >= 4) {
            TextFrame releaseYear = TextFrame.createInstance(
                    AbstractFrame.ORIGINAL_RELEASE_YEAR,
                    releaseTimeFrame.getText().substring(0, 4),
                    ENCODING_LATIN_1,
                    version);
            frames.add(releaseYear);
        }

        frames.remove(releaseTimeFrame);

        if (timestampFrame != null) {

            Year year      = timestampFrame.getYear();
            MonthDay date  = timestampFrame.getMonthDay();
            LocalTime time = timestampFrame.getTime();

            TextFrame frame;

            if (year != null) {
                frame = TextFrame.createInstance(
                        AbstractFrame.YEAR, String.valueOf(year),
                        ENCODING_LATIN_1, version
                );
                frames.add(frame);
            }
            if (date != null) {
                frame = DateFrame.createInstance(date);
                frames.add(frame);
            }
            if (time != null) {
                frame = TimeFrame.createInstance(time);
                frames.add(frame);
            }
            frames.remove(releaseTimeFrame);
        }
    }

    private static void convertToID3V24Tag(ID3V2Tag id3V2Tag) {

        final byte version = ID3V2_4;
        ArrayList<AbstractFrame> frames = id3V2Tag.getFrames();

        StringBuilder dateString = new StringBuilder();
        StringBuilder pattern    = new StringBuilder();

        TextFrame releaseYear = id3V2Tag.getFrame(AbstractFrame.ORIGINAL_RELEASE_YEAR);
        TextFrame yearFrame   = id3V2Tag.getFrame(AbstractFrame.YEAR);
        TimeFrame timeFrame   = id3V2Tag.getFrame(AbstractFrame.TIME);
        DateFrame dateFrame   = id3V2Tag.getFrame(AbstractFrame.YEAR);

        if (releaseYear != null && releaseYear.getText().length() == 4) {

            TimestampFrame releaseTime = TimestampFrame.createBuilder()
                    .setHeader(FrameHeader.createFrameHeader(AbstractFrame.ORIGINAL_RELEASE_TIME, version))
                    .setYear(Year.parse(releaseYear.getText()))
                    .build(version);

            frames.set(frames.indexOf(releaseYear), releaseTime);
        }

        frames.remove(releaseYear);

        boolean isYear      = false;
        boolean isDate      = false;
        boolean isDateTime  = false;

        final String yearPattern = "yyyy";

        if (yearFrame != null && yearFrame.getText().length() == 4) {
            pattern.append(yearPattern);
            dateString.append(yearFrame.getText());
            isYear = true;
        }
        if (isYear && dateFrame != null && dateFrame.getText().length() == 4) {
            pattern.append("-").append(DATE_FORMAT_PATTERN);
            dateString.append("-").append(dateFrame.getText());
            isDate = true;
        }
        if (isYear && isDate && timeFrame != null && timeFrame.getText().length() == 4) {
            pattern.append("-").append(TIME_FORMAT_PATTERN);
            dateString.append("-").append(timeFrame.getText());
            isDateTime = true;
        }

        frames.remove(yearFrame);
        frames.remove(dateFrame);
        frames.remove(timeFrame);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern.toString());

        try {

            TimestampFrame.Builder recTime = TimestampFrame.createBuilder();
            recTime = recTime.setHeader(FrameHeader.createFrameHeader(AbstractFrame.RECORDING_TIME, version));

            if (isDateTime) {
                recTime.setDateTime(LocalDateTime.parse(dateString, formatter));
            } else if (isDate) {
                recTime.setDate(LocalDate.parse(dateString, formatter));
            } else if (isYear) {
                recTime.setYear(Year.parse(dateString.toString()));
            } else {
                return;
            }

            TimestampFrame timestampFrame = recTime.build(version);
            frames.add(timestampFrame);

        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }
    }

    public static ID3V2Tag convertID3V2Tag(ID3V2Tag id3V2Tag, byte version) {

        if (id3V2Tag.getVersion() == version) return id3V2Tag;
        ArrayList<AbstractFrame> frames = id3V2Tag.getFrames();

        for (AbstractFrame frame : frames) {
            frame.getHeader().setTagVersion(version);
            frame.assemble(version);
        }

        if (version == ID3V2_4) {
            convertToID3V24Tag(id3V2Tag);
            frames.removeIf((frame) -> !Arrays.asList(V2_4_FRAMES).contains(frame.getIdentifier()));
        }
        if (version == ID3V2_3) {
            convertToID3V23Tag(id3V2Tag);
            frames.removeIf((frame) -> !Arrays.asList(V2_3_FRAMES).contains(frame.getIdentifier()));
        }
        return ID3V2Tag.newBuilder(id3V2Tag).build(version);
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

        if (id3v1Tag.getVersion() == ID3V1Tag.ID3V1_1) {
            builder = builder.setAlbumTrack(String.valueOf(id3v1Tag.getTrackNumber()));
        }
        return builder.build(version);
    }

    @Override
    protected <T> T getFieldValue(String field) {
        AbstractFrame<T> frame = getFrameFromFieldName(field);
        if (frame == null) return null;
        return frame.getFrameData();
    }

    @Override
    public void removeField(String field) {
        String frameId = getFrameIdFromFieldName(field);
        if (frameId != null) removeFrame(frameId);
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
        if (field.equals(Tag.PICTURE)) {
            AttachedPicture picture = (AttachedPicture) value;
            AttachedPictureFrame pictureFrame = AttachedPictureFrame.createInstance(
                    picture.getDescription(), picture.getMimeType(),
                    (byte) picture.getPictureType(), picture.getPictureData(),
                    getVersion()
            );
            frames.add(pictureFrame);
        } else if (TimestampFrame.isTimestampFrame(frameId)) {
            Year year = Year.parse((String) value);
            TimestampFrame timestampFrame = TimestampFrame.createBuilder()
                    .setYear(year)
                    .setHeader(FrameHeader.createFrameHeader(frameId, getVersion()))
                    .build(getVersion());
            frames.add(timestampFrame);
        } else {
            TextFrame textFrame = TextFrame.createInstance(
                    frameId,
                    ((String) value),
                    TextEncoding.getAppropriateEncoding(getVersion()),
                    getVersion());
            frames.add(textFrame);
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

        public Builder setGenre(String genre) {
            ID3V2Tag.this.setGenre(genre);
            return this;
        }

        public Builder setYear(String year) {
            ID3V2Tag.this.setYear(year);
            return this;
        }

        public Builder setComment(String comment) {
            ID3V2Tag.this.setComment(comment, "XXX");
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
            ID3V2Tag.this.frames = frames;
            return this;
        }

        public ID3V2Tag buildExisting(byte[] data) {
            tagBytes = data;
            return ID3V2Tag.this;
        }

        public ID3V2Tag build(byte version) {
            assemble(version);
            return ID3V2Tag.this;
        }
    }
}
