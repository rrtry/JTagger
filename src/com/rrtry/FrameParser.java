package com.rrtry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

class FrameParser {

    private final TagHeaderParser tagHeaderParser;
    private FrameHeaderParser frameHeaderParser;

    private TagHeader tagHeader;
    private byte[] tagBuffer;

    public FrameParser(TagHeaderParser tagHeaderParser) {
        this.tagHeaderParser = tagHeaderParser;
    }

    public void read(RandomAccessFile file, int pos, int length) throws IOException, InvalidTagException {

        tagHeader = tagHeaderParser.parse();
        tagBuffer = new byte[length];

        file.seek(pos);
        file.read(tagBuffer, 0, length);

        if (tagHeader.isUnsynch() && tagHeader.getMajorVersion() == ID3V2Tag.ID3V2_3) {
            tagBuffer = UnsynchronisationHelper.fromUnsynch(tagBuffer);
        }
        frameHeaderParser = new FrameHeaderParser(tagHeader);
    }

    private byte[] copyFrame(int pos, int frameSize) {

        int from = pos + FrameHeader.FRAME_HEADER_DATA_OFFSET;
        int to = from + frameSize;

        return Arrays.copyOfRange(tagBuffer, from, to);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractFrame> T parseFrame(String frameId) throws IOException, NoSuchFrameException {
        for (AbstractFrame frame : parseFrames()) {
            if (frame.getIdentifier().equals(frameId)) {
                return (T) frame;
            }
        }
        throw new NoSuchFrameException(String.format("Frame %s was not found", frameId));
    }

    @SuppressWarnings("rawtypes")
    private AbstractFrame parseFrame(int position, FrameHeader frameHeader) {

        FrameType frameType = FrameType.fromIdentifier(frameHeader.getIdentifier());
        if (frameType == null) {
            return new UnknownFrame(frameHeader, copyFrame(position, frameHeader.getFrameSize()));
        }

        byte[] frame = copyFrame(
                position + frameHeader.getFrameDataOffset(),
                frameHeader.getFrameSize()
        );

        if (frameHeader.isFrameEncrypted()) {
            return null;
        }
        if (frameHeader.isFrameUnsynch()) {
            frame = UnsynchronisationHelper.fromUnsynch(frame);
        }
        if (frameHeader.isFrameCompressed()) {
            frame = AbstractFrame.decompressFrame(frame);
        }

        FrameBodyParser frameParser = FrameParserFactory.getParser(frameType);
        return frameParser.parse(frameHeader.getIdentifier(), frameHeader, frame, tagHeader);
    }

    public final ArrayList<AbstractFrame> parseFrames() {

        ArrayList<AbstractFrame> frames = new ArrayList<>();

        int position = 0;
        while (position < tagBuffer.length) {

            FrameHeader frameHeader = frameHeaderParser.parseFrameHeader(tagBuffer, position);
            if (frameHeader == null) break;

            AbstractFrame frame = parseFrame(position, frameHeader);
            if (frame != null) frames.add(frame);

            position += (frameHeader.getFrameSize() + FrameHeader.FRAME_HEADER_LENGTH);
        }
        return frames;
    }
}
