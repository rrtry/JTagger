package com.jtagger.mp3.id3;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.utils.IntegerUtils.toUInt32BE;
import static com.jtagger.mp3.id3.ID3SynchSafeInteger.fromSynchSafeIntegerBytes;

public class FrameHeaderParser {

    private final TagHeader tagHeader;
    private byte[] header;

    private int offset;
    private int paddingOffset = 0;

    public FrameHeaderParser(TagHeader tagHeader) {
        this.tagHeader = tagHeader;
    }

    int getPaddingOffset() {
        return paddingOffset;
    }

    private byte[] parseFlags() {
        return Arrays.copyOfRange(
                header, FrameHeader.FRAME_HEADER_FLAGS_OFFSET, FrameHeader.FRAME_HEADER_FLAGS_OFFSET + FrameHeader.FRAME_HEADER_FLAGS_LENGTH
        );
    }

    private int parseFrameSize() {
        byte[] frameSize = Arrays.copyOfRange(
                this.header,
                FrameHeader.FRAME_HEADER_SIZE_OFFSET,
                FrameHeader.FRAME_HEADER_SIZE_OFFSET + FrameHeader.FRAME_HEADER_SIZE_LENGTH
        );
        if (tagHeader.getMajorVersion() == ID3V2Tag.ID3V2_4) {
            return fromSynchSafeIntegerBytes(frameSize);
        }
        return toUInt32BE(frameSize);
    }

    public FrameHeader parseFrameHeader(byte[] tagBuffer, int position) {
        header = Arrays.copyOfRange(tagBuffer, position, position + FrameHeader.FRAME_HEADER_LENGTH);
        offset = position;
        return parseFrameHeader();
    }

    private FrameHeader parseFrameHeader() {
        try {

            String id = new String(Arrays.copyOfRange(header, 0, FrameHeader.FRAME_HEADER_ID_LENGTH), StandardCharsets.ISO_8859_1);
            if (id.equals("\0\0\0\0")) {
                paddingOffset = offset;
                return null;
            }

            int frameSize = parseFrameSize();
            byte[] flags  = parseFlags();
            byte version  = tagHeader.getMajorVersion();

            FrameHeader header = FrameHeader.newBuilder(version)
                    .setIdentifier(id, version)
                    .setFrameSize(frameSize)
                    .setFlags(flags)
                    .build(version);

            if (header.isFrameEncrypted()) {
                throw new IllegalStateException();
            }
            return header;

        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            return null;
        }
    }
}
