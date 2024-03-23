package com.jtagger.mp3.id3;

import java.util.ArrayList;
import java.util.Arrays;

import static com.jtagger.mp3.id3.AbstractFrame.decompressFrame;
import static com.jtagger.mp3.id3.TagHeaderParser.HEADER_LENGTH;
import static com.jtagger.mp3.id3.UnsynchronisationUtils.fromUnsynch;

@SuppressWarnings("rawtypes")
class FrameParser {

    private TagHeader tagHeader;
    private byte[] frameData;

    private int paddingOffset = 0;
    private int framesOffset  = 0;

    public int getPaddingOffset() {
        return paddingOffset;
    }

    public int getFramesOffset() {
        return framesOffset;
    }

    public void setFramesOffset(int framesOffset) {
        this.framesOffset = framesOffset;
    }

    public void setTagHeader(TagHeader tagHeader) {
        this.tagHeader = tagHeader;
    }

    public void setFrames(byte[] frameData) {
        this.frameData = frameData;
    }

    private byte[] copyFrame(int pos, int frameSize) {
        int from = pos + FrameHeader.FRAME_HEADER_DATA_OFFSET;
        int to = from + frameSize;
        return Arrays.copyOfRange(frameData, from, to);
    }

    private AbstractFrame parseFrame(int position, FrameHeader frameHeader) {

        FrameType frameType = FrameType.fromIdentifier(frameHeader.getIdentifier());
        if (frameType == null) {
            return new UnknownFrame(frameHeader, copyFrame(position, frameHeader.getFrameSize()));
        }

        byte[] frame = copyFrame(
                position + frameHeader.getFrameDataOffset(),
                frameHeader.getFrameSize()
        );

        if (frameHeader.isFrameEncrypted())  return null;
        if (frameHeader.isFrameUnsynch())    frame = fromUnsynch(frame);
        if (frameHeader.isFrameCompressed()) frame = decompressFrame(frame);

        FrameBodyParser frameParser = FrameParserFactory.getParser(frameType);
        return frameParser.parse(frameHeader.getIdentifier(), frameHeader, frame, tagHeader);
    }

    public final ArrayList<AbstractFrame> parseFrames() {

        FrameHeaderParser frameHeaderParser = new FrameHeaderParser(tagHeader);
        ArrayList<AbstractFrame> frames = new ArrayList<>();

        int position = framesOffset;
        while (position < frameData.length) {

            FrameHeader frameHeader = frameHeaderParser.parseFrameHeader(frameData, position);
            if (frameHeader == null) {
                paddingOffset = frameHeaderParser.getPaddingOffset();
                break;
            }

            final int frameSize = frameHeader.getFrameSize();
            AbstractFrame frame = parseFrame(position, frameHeader);
            if (frame != null) frames.add(frame);

            position += (frameSize + FrameHeader.FRAME_HEADER_LENGTH);
        }
        return frames;
    }
}
