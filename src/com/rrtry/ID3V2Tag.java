package com.rrtry;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

import static com.rrtry.AbstractFrame.*;
import static com.rrtry.DateFrame.DATE_FORMAT_PATTERN;
import static com.rrtry.TextEncoding.ENCODING_LATIN_1;
import static com.rrtry.TimeFrame.TIME_FORMAT_PATTERN;

public class ID3V2Tag implements ID3Tag, Component {

    public static final String[] DEPRECATED_V23_FRAMES = new String[] {
            "EQUA", "IPLS", "RVAD", "TDAT", "TIME", "TORY", "TRDA", "TSIZ", "TYER"
    };

    public static final byte ID3V2   = 0x02;
    public static final byte ID3V2_3 = 0x03;
    public static final byte ID3V2_4 = 0x04;

    private static final int PADDING = 2048;

    private TagHeader tagHeader;
    private ArrayList<AbstractFrame> frames = new ArrayList<>();
    private byte[] tagBytes;

    public TagHeader getTagHeader()             { return tagHeader; }
    public ArrayList<AbstractFrame> getFrames() { return frames; }
    public AbstractFrame getFrameAt(int index)  { return frames.get(index); }

    public TextFrame getTextFrame(String id) { return getFrame(id); }

    public <T extends AbstractFrame> T getFrame(String id) {

        boolean knownFrame = false;
        if (getVersion() == ID3V2_3) knownFrame = Arrays.asList(V2_3_FRAMES).contains(id);
        if (getVersion() == ID3V2_4) knownFrame = Arrays.asList(V2_4_FRAMES).contains(id);

        if (!knownFrame) throw new NotImplementedException();

        for (AbstractFrame frame : frames) {
            if (frame.getIdentifier().equals(id)) {
                return (T) frame;
            }
        }
        return null;
    }

    public void addFrame(AbstractFrame frame) { frames.add(frame); }
    public void setFrameAtIndex(int index, AbstractFrame frame) { frames.set(index, frame); }

    public void setFrame(AbstractFrame frame) {
        int index = indexOf(frame.getIdentifier());
        if (index != -1) setFrameAtIndex(indexOf(frame.getIdentifier()), frame);
        else addFrame(frame);
    }

    public void removeFrameAtIndex(int index)       { frames.remove(index); }
    public boolean removePictures()                 { return removeFramesWithId(PICTURE); }
    public boolean removeFrame(AbstractFrame frame) { return frames.remove(frame); }
    public boolean removeFramesWithId(String id)    { return frames.removeIf(frame -> frame.getIdentifier().equals(id)); }

    private int getTagSize(byte version) {

        int size = 0;

        for (AbstractFrame frame : frames) {
            if (tagHeader.isUnsynch() && version == ID3V2_4) {
                frame.getHeader().setFrameUnsynch(true);
            }
            frame.assemble(version);
            size += frame.getHeader().getFrameSize() + TagHeaderParser.HEADER_LENGTH;
        }
        return size + PADDING;
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
        RecordingTimeFrame recordingTimeFrame = RecordingTimeFrame.createBuilder()
                .setHeader(FrameHeader.createFrameHeader(id, ID3V2_4))
                .setYear(Year.parse(year))
                .build(ID3V2_4);
        setFrame(recordingTimeFrame);
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
                        .setHeader(FrameHeader.createFrameHeader(PICTURE, getVersion()))
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
                        .setHeader(FrameHeader.createFrameHeader(PICTURE, getVersion()))
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
                    Arrays.copyOfRange(tagBytes, TagHeaderParser.HEADER_LENGTH, tagBytes.length)
            );

            byte[] unsynchTag = new byte[TagHeaderParser.HEADER_LENGTH + frameData.length];
            System.arraycopy(header, 0, unsynchTag, 0, TagHeaderParser.HEADER_LENGTH);
            System.arraycopy(frameData, 0, unsynchTag, header.length, frameData.length);

            return unsynchTag;
        }
        return tagBytes;
    }

    @Override
    public String getTitle() {
        TextFrame titleFrame = getTextFrame(TITLE);
        return titleFrame == null ? "" : titleFrame.getText();
    }

    @Override
    public String getArtist() {
        TextFrame artistFrame = getTextFrame(ARTIST);
        return artistFrame == null ? "" : artistFrame.getText();
    }

    @Override
    public String getAlbum() {
        TextFrame albumFrame = getTextFrame(ALBUM);
        return albumFrame == null ? "" : albumFrame.getText();
    }

    public String getRecordingTimestamp() {
        if (getVersion() == ID3V2_4) {
            RecordingTimeFrame recordingTimeFrame = getFrame(RECORDING_TIME);
            return recordingTimeFrame == null ? "" : recordingTimeFrame.getText();
        }
        return "";
    }

    public String getRecordingTime() {

        String recTime = "";

        if (getVersion() == ID3V2_3) {
            TimeFrame timeFrame = (TimeFrame) getTextFrame(TIME);
            recTime = timeFrame == null ? "" : timeFrame.getText();
        }
        if (getVersion() == ID3V2_4) {
            RecordingTimeFrame recordingTimeFrame = getFrame(RECORDING_TIME);
            LocalTime time = recordingTimeFrame.getTime();
            recTime = time == null ? "" : time.toString();
        }
        return recTime;
    }

    public String getRecordingDate() {

        String recDate = "";

        if (getVersion() == ID3V2_3) {
            DateFrame dateFrame = (DateFrame) getTextFrame(DATE);
            recDate = dateFrame == null ? "" : dateFrame.getText();
        }
        if (getVersion() == ID3V2_4) {
            RecordingTimeFrame recordingTimeFrame = getFrame(RECORDING_TIME);
            LocalDate date = recordingTimeFrame.getDate();
            recDate = date == null ? "" : date.toString();
        }
        return recDate;
    }

    @Override
    public String getYear() {

        String year = "";

        if (getVersion() == ID3V2_3) {
            TextFrame yearFrame = getTextFrame(YEAR);
            year = yearFrame == null ? "" : yearFrame.getText();
        }
        if (getVersion() == ID3V2_4) {
            RecordingTimeFrame recordingTimeFrame = getFrame(RECORDING_TIME);
            year = recordingTimeFrame == null ? "" : recordingTimeFrame.getYear().toString();
        }
        return year;
    }

    @Override
    public byte getVersion() { return getTagHeader().getMajorVersion(); }

    @Override
    public void setTitle(String title) { setTextFrame(TITLE, title, TextEncoding.getAppropriateEncoding(getVersion())); }

    @Override
    public void setArtist(String artist) { setTextFrame(ARTIST, artist, TextEncoding.getAppropriateEncoding(getVersion())); }

    @Override
    public void setAlbum(String album) { setTextFrame(ALBUM, album, TextEncoding.getAppropriateEncoding(getVersion())); }

    @Override
    public void setYear(String year) {
        byte version = getVersion();
        if (version == ID3V2_3) setTextFrame(YEAR, year, ENCODING_LATIN_1);
        if (version == ID3V2_4) setRecordingYear(RECORDING_TIME, year);
    }

    @Override
    public void setVersion(byte version) {
        tagHeader = TagHeader.newBuilder(tagHeader)
                .setMajorVersion(version)
                .build(version);
    }

    public void setComment(String comment, String language) {
        setCommentFrame(comment, language);
    }

    public boolean removeComment() { return removeFramesWithId(COMMENT); }

    @Override
    public boolean removeTitle() { return removeFramesWithId(TITLE); }

    @Override
    public boolean removeArtist() { return removeFramesWithId(ARTIST); }

    @Override
    public boolean removeAlbum() { return removeFramesWithId(ALBUM); }

    @Override
    public boolean removeYear() {
        if (getVersion() == ID3V2_3) return removeFramesWithId(YEAR);
        else if (getVersion() == ID3V2_4) return removeFramesWithId(RECORDING_TIME);
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

        if (tagHeader.hasExtendedHeader()) throw new NotImplementedException();
        if (tagHeader.hasFooter()) throw new NotImplementedException();

        int tagSize  = getTagSize(version);
        int position = TagHeaderParser.HEADER_LENGTH;

        byte[] tag = new byte[tagSize + TagHeaderParser.HEADER_LENGTH];

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
                    Arrays.copyOfRange(tagBytes, TagHeaderParser.HEADER_LENGTH, tagBytes.length)
            );
            tagSize = unsynchFrameData.length;
        }

        tagHeader = TagHeader.newBuilder(tagHeader)
                .setMajorVersion(version)
                .setTagSize(tagSize)
                .build(version);

        System.arraycopy(tagHeader.getBytes(), 0, tag, 0, TagHeaderParser.HEADER_LENGTH);
        return tag;
    }

    public static Builder newBuilder() { return new ID3V2Tag().new Builder(); }
    public static Builder newBuilder(ID3V2Tag ID3V2Tag) { return ID3V2Tag.new Builder(); }

    private static void convertToID3V23Tag(ID3V2Tag id3V2Tag) {

        final byte version = ID3V2_3;
        ArrayList<AbstractFrame> frames = id3V2Tag.getFrames();

        RecordingTimeFrame recordingTimeFrame = id3V2Tag.getFrame(RECORDING_TIME);
        TextFrame releaseTimeFrame = id3V2Tag.getFrame(ORIGINAL_RELEASE_TIME);

        if (releaseTimeFrame != null && releaseTimeFrame.getText().length() >= 4) {
            TextFrame releaseYear = TextFrame.createInstance(
                    ORIGINAL_RELEASE_YEAR,
                    releaseTimeFrame.getText().substring(0, 4),
                    ENCODING_LATIN_1,
                    version);
            frames.add(releaseYear);
        }

        frames.remove(releaseTimeFrame);

        if (recordingTimeFrame != null) {

            Year year      = recordingTimeFrame.getYear();
            MonthDay date  = recordingTimeFrame.getMonthDay();
            LocalTime time = recordingTimeFrame.getTime();

            TextFrame frame;

            if (year != null) {
                frame = TextFrame.createInstance(
                        YEAR, String.valueOf(year),
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

        TextFrame releaseYear = id3V2Tag.getFrame(ORIGINAL_RELEASE_YEAR);
        TextFrame yearFrame   = id3V2Tag.getFrame(YEAR);
        TimeFrame timeFrame   = id3V2Tag.getFrame(TIME);
        DateFrame dateFrame   = id3V2Tag.getFrame(DATE);

        if (releaseYear != null && releaseYear.getText().length() == 4) {

            RecordingTimeFrame releaseTime = RecordingTimeFrame.createBuilder()
                    .setHeader(FrameHeader.createFrameHeader(ORIGINAL_RELEASE_TIME, version))
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

            RecordingTimeFrame.Builder recTime = RecordingTimeFrame.createBuilder();
            recTime = recTime.setHeader(FrameHeader.createFrameHeader(RECORDING_TIME, version));

            if (isDateTime) {
                recTime.setDateTime(LocalDateTime.parse(dateString, formatter));
            } else if (isDate) {
                recTime.setDate(LocalDate.parse(dateString, formatter));
            } else if (isYear) {
                recTime.setYear(Year.parse(dateString.toString()));
            } else {
                return;
            }

            RecordingTimeFrame recordingTimeFrame = recTime.build(version);
            frames.add(recordingTimeFrame);

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

        ID3V2Tag id3v2Tag = ID3V2Tag.newBuilder()
                .setHeader(header)
                .setTitle(id3v1Tag.getTitle())
                .setAlbum(id3v1Tag.getAlbum())
                .setArtist(id3v1Tag.getArtist())
                .build(version);

        id3v2Tag.setYear(id3v1Tag.getYear());
        return ID3V2Tag.newBuilder(id3v2Tag).build(version);
    }

    public class Builder {

        public Builder setTitle(String title) {
            ID3V2Tag.this.setTitle(title);
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
