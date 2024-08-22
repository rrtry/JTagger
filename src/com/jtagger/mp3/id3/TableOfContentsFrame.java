package com.jtagger.mp3.id3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import static com.jtagger.mp3.id3.ChapterFrame.assembleEmbeddedFrames;
import static com.jtagger.mp3.id3.ChapterFrame.parseEmbeddedFrames;
import static com.jtagger.mp3.id3.ID3V2Tag.assembleFrames;
import static com.jtagger.mp3.id3.TextEncoding.*;
import static java.lang.Byte.toUnsignedInt;

@SuppressWarnings("rawtypes")
public class TableOfContentsFrame extends AbstractFrame<Collection<String>> {

    public static final int FLAG_TOP_LEVEL = 0x1;
    public static final int FLAG_ORDERED   = 0x2;

    private String elementId;
    private int flags = 0x0;
    private int entryCount;

    private Collection<String> childIds = new ArrayList<>();
    private LinkedHashMap<String, AbstractFrame> frameMap = new LinkedHashMap<>();

    public String getElementId() {
        return elementId;
    }

    public int getFlags() {
        return flags;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public Collection<String> getChildIds() {
        return childIds;
    }

    public LinkedHashMap<String, AbstractFrame> getFrameMap() {
        return frameMap;
    }

    public void setElementId(String elementId) {
        if (elementId.isEmpty()) {
            throw new IllegalArgumentException("Element ID cannot be empty");
        }
        this.elementId = elementId;
    }

    public void setChildIds(Collection<String> childIds) {
        this.childIds = childIds;
        entryCount = childIds.size();
    }

    public void setFrameMap(LinkedHashMap<String, AbstractFrame> frameMap) {
        this.frameMap = frameMap;
    }

    public void setFlags(int flags) {
        if (((flags & 0xFF) >> 2) != 0) {
            throw new IllegalArgumentException("Illegal flag set");
        }
        this.flags = flags;
    }

    private int getChildIdsSize() {
        int size = 0;
        for (String id : childIds) {
            size += id.length() + 1;
        }
        return size;
    }

    @Override
    public String getKey() {
        return String.format("%s:%s", getIdentifier(), elementId);
    }

    @Override
    public byte[] assemble(byte version) {

        byte[] idBuffer = TextEncoding.getStringBytes(elementId, ENCODING_LATIN_1);
        int bufferSize  = assembleEmbeddedFrames(frameMap.values(), header.getVersion()) +
                          getChildIdsSize() +
                          idBuffer.length + 2;

        byte[] buffer = new byte[bufferSize];
        int offset = 0;

        System.arraycopy(idBuffer, 0, buffer, offset, idBuffer.length); offset += idBuffer.length;
        buffer[offset++] = (byte) flags;
        buffer[offset++] = (byte) childIds.size();

        for (String id : childIds) {
            byte[] elementBuffer = TextEncoding.getStringBytes(id, ENCODING_LATIN_1);
            System.arraycopy(elementBuffer, 0, buffer, offset, elementBuffer.length);
            offset += elementBuffer.length;
        }
        assembleFrames(
                frameMap.values(),
                version,
                buffer,
                offset
        );
        frameBytes = buffer;
        header = FrameHeader.newBuilder(header)
                .setFrameSize(getBytes().length)
                .build(version);
        return buffer;
    }

    @Override
    public Collection<String> getFrameData() {
        return childIds;
    }

    @Override
    public void setFrameData(Collection<String> data) {
        this.childIds = data;
    }

    @Override
    public void parseFrameData(byte[] buffer, FrameHeader header) {

        this.header = header;
        int idLength = getStringLength(buffer, 0, ENCODING_LATIN_1);
        int entryPos = 0;
        int offset   = idLength;

        elementId = getString(buffer, 0, idLength, ENCODING_LATIN_1);
        flags = buffer[offset++];
        entryCount = toUnsignedInt(buffer[offset++]);

        int entryLength;
        String entry;
        while (entryPos < entryCount) {

            entryLength = getStringLength(buffer, offset, ENCODING_LATIN_1);
            entry = getString(buffer, offset, entryLength, ENCODING_LATIN_1);

            offset += entryLength;
            entryPos++;
            childIds.add(entry);
        }
        if (offset < buffer.length) {
            parseEmbeddedFrames(
                    buffer,
                    offset,
                    frameMap,
                    header.getVersion()
            );
        }
    }

    public static Builder newBuilder() {
        return new TableOfContentsFrame().new Builder();
    }

    public class Builder {

        public Builder setHeader(FrameHeader header) {
            if (!header.getIdentifier().equals(AbstractFrame.TABLE_OF_CONTENTS)) {
                throw new IllegalArgumentException("Illegal frame identifier, expected: CTOC");
            }
            TableOfContentsFrame.this.header = header;
            return this;
        }

        public Builder setElementId(String elementId) {
            TableOfContentsFrame.this.setElementId(elementId);
            return this;
        }

        public Builder setChildIds(Collection<String> childIds) {
            TableOfContentsFrame.this.setChildIds(childIds);
            return this;
        }

        public Builder setFrameMap(LinkedHashMap<String, AbstractFrame> frameMap) {
            TableOfContentsFrame.this.setFrameMap(frameMap);
            return this;
        }

        public Builder setFlags(int flags) {
            TableOfContentsFrame.this.setFlags(flags);
            return this;
        }

        public TableOfContentsFrame build(byte version) {
            assemble(version);
            return TableOfContentsFrame.this;
        }
    }
}
