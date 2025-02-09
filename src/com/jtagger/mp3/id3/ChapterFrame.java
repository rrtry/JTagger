package com.jtagger.mp3.id3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;

import static com.jtagger.mp3.id3.ID3V2Tag.assembleFrames;
import static com.jtagger.mp3.id3.TagHeaderParser.HEADER_LENGTH;
import static com.jtagger.mp3.id3.TextEncoding.ENCODING_LATIN_1;
import static com.jtagger.utils.IntegerUtils.fromUInt32BE;
import static com.jtagger.utils.IntegerUtils.toUInt32BE;
import static java.lang.Integer.toUnsignedLong;

@SuppressWarnings("rawtypes")
public class ChapterFrame extends AbstractFrame<LinkedHashMap<String, AbstractFrame>> {

    private String elementId;
    private LinkedHashMap<String, AbstractFrame> frameMap = new LinkedHashMap<>();

    private long startTime;
    private long endTime;

    private long startOffset;
    private long endOffset;

    private String getTextFrameData(String id) {
        TextFrame frame = (TextFrame) frameMap.get(id);
        return frame != null ? frame.getText() : null;
    }

    private void setTextFrame(String id, String text) {
        TextFrame frame = TextFrame.createInstance(
                id, text, TextEncoding.getEncodingForVersion(header.getVersion()), header.getVersion()
        );
        frameMap.put(id, frame);
    }

    public String getTitle() {
        return getTextFrameData("TIT2");
    }

    public String getDescription() {
        return getTextFrameData("TIT3");
    }

    public String getElementId() {
        return elementId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setTitle(String title) {
        setTextFrame("TIT2", title);
    }

    public void setDescription(String description) {
        setTextFrame("TIT3", description);
    }

    public void setElementId(String elementId) {
        if (elementId.isEmpty()) {
            throw new IllegalArgumentException("Element ID cannot be empty");
        }
        this.elementId = elementId;
    }

    public void setStartTime(long startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("Chapter start time cannot be negative");
        }
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        if (endTime < 0) {
            throw new IllegalArgumentException("Chapter end time cannot be negative");
        }
        this.endTime = endTime;
    }

    public void setStartOffset(long startOffset) {
        if (startOffset < 0) {
            throw new IllegalArgumentException("Chapter start offset cannot be negative");
        }
        this.startOffset = startOffset;
    }

    public void setEndOffset(long endOffset) {
        if (endOffset < 0) {
            throw new IllegalArgumentException("Chapter end offset cannot be negative");
        }
        this.endOffset = endOffset;
    }

    public static void parseEmbeddedFrames(byte[] buffer,
                                           int offset,
                                           LinkedHashMap<String, AbstractFrame> frameMap,
                                           byte version)
    {
        FrameParser parser  = new FrameParser();
        TagHeader tagHeader = new TagHeader();

        tagHeader.setMajorVersion(version);
        parser.setTagHeader(tagHeader);
        parser.setFrames(buffer);
        parser.setFramesOffset(offset);

        ArrayList<AbstractFrame> frames = new ArrayList<>();
        parser.parseFrames(frames);

        for (AbstractFrame frame : frames) {
            frameMap.put(frame.getKey(), frame);
        }
    }

    public static Builder newBuilder() {
        return new ChapterFrame().new Builder();
    }

    public static int assembleEmbeddedFrames(Collection<AbstractFrame> frames, byte version) {
        int size = 0;
        for (AbstractFrame frame : frames) {
            frame.assemble(version);
            size += frame.getHeader().getFrameSize() + HEADER_LENGTH;
        }
        return size;
    }

    @Override
    public String getKey() {
        return String.format("%s:%s", getIdentifier(), elementId);
    }

    @Override
    public byte[] assemble(byte version) {

        int position = 0;
        byte[] idBuffer = TextEncoding.getStringBytes(elementId, ENCODING_LATIN_1);
        byte[] buffer = new byte[assembleEmbeddedFrames(frameMap.values(), version) + idBuffer.length + 16];

        System.arraycopy(idBuffer, 0, buffer, position, idBuffer.length); position += idBuffer.length;
        System.arraycopy(fromUInt32BE((int) startTime), 0, buffer, position, 4); position += 4;
        System.arraycopy(fromUInt32BE((int) endTime), 0, buffer, position, 4);   position += 4;

        System.arraycopy(fromUInt32BE((int) startOffset), 0, buffer, position, 4); position += 4;
        System.arraycopy(fromUInt32BE((int) endOffset), 0, buffer, position, 4);   position += 4;

        assembleFrames(
                frameMap.values(),
                version,
                buffer,
                position
        );
        frameBytes = buffer;
        header = FrameHeader.newBuilder(header)
                .setFrameSize(getBytes().length)
                .build(version);
        return frameBytes;
    }

    @Override
    public LinkedHashMap<String, AbstractFrame> getFrameData() {
        return frameMap;
    }

    @Override
    public void setFrameData(LinkedHashMap<String, AbstractFrame> data) {
        this.frameMap = data;
    }

    @Override
    public void parseFrameData(byte[] buffer, FrameHeader header) {

        this.header = header;
        int idLength = TextEncoding.getStringLength(buffer, 0, ENCODING_LATIN_1);
        int offset   = idLength;

        elementId   = TextEncoding.getString(buffer, 0, idLength, ENCODING_LATIN_1);
        startTime   = toUnsignedLong(toUInt32BE(Arrays.copyOfRange(buffer, offset, offset += 4)));
        endTime     = toUnsignedLong(toUInt32BE(Arrays.copyOfRange(buffer, offset, offset += 4)));
        startOffset = toUnsignedLong(toUInt32BE(Arrays.copyOfRange(buffer, offset, offset += 4)));
        endOffset   = toUnsignedLong(toUInt32BE(Arrays.copyOfRange(buffer, offset, offset += 4)));
        parseEmbeddedFrames(buffer, offset, frameMap, header.getVersion());
    }

    public class Builder {

        public Builder setHeader(FrameHeader frameHeader) {
            if (!frameHeader.getIdentifier().equals(AbstractFrame.CHAPTER)) {
                throw new IllegalArgumentException("Illegal frame identifier, expected: CHAP");
            }
            ChapterFrame.this.header = frameHeader;
            return this;
        }

        public Builder setElementId(String elementId) {
            ChapterFrame.this.setElementId(elementId);
            return this;
        }

        public Builder setStartTime(long startTime) {
            ChapterFrame.this.setStartTime(startTime);
            return this;
        }

        public Builder setEndTime(long endTime) {
            ChapterFrame.this.setEndTime(endTime);
            return this;
        }

        public Builder setStartOffset(long startOffset) {
            ChapterFrame.this.setStartOffset(startOffset);
            return this;
        }

        public Builder setEndOffset(long endOffset) {
            ChapterFrame.this.setEndOffset(endOffset);
            return this;
        }

        public Builder setFrames(LinkedHashMap<String, AbstractFrame> frames) {
            ChapterFrame.this.setFrameData(frames);
            return this;
        }

        public ChapterFrame build(byte version) {
            assemble(version);
            return ChapterFrame.this;
        }
    }
}
