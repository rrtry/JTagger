package com.jtagger.mp3.id3;

import com.jtagger.AbstractTag;

import java.time.format.DateTimeParseException;
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

        String frameId = frameHeader.getIdentifier();
        AbstractFrame frame = null;

        if (TimestampFrame.isValidFrameId(frameId)) {
            frame = new TimestampFrame();
        }
        else if (frameId.equals(AbstractFrame.GENRE)) {
            frame = new GenreFrame();
        }
        else if (frameId.charAt(0) == 'T') {
            frame = new TextFrame();
        }
        else {
            switch (frameId) {
                case AbstractFrame.PICTURE:
                    frame = new AttachedPictureFrame();
                    break;
                case AbstractFrame.COMMENT:
                    frame = new CommentFrame();
                    break;
                case AbstractFrame.U_LYRICS:
                    frame = new UnsynchronisedLyricsFrame();
                    break;
                case AbstractFrame.S_LYRICS:
                    frame = new SynchronisedLyricsFrame();
                    break;
                case AbstractFrame.CUSTOM:
                    frame = new UserDefinedTextInfoFrame();
                    break;
            }
        }

        byte[] buffer = copyFrame(
                position + frameHeader.getFrameDataOffset(),
                frameHeader.getFrameSize()
        );
        if (frame == null) {
            return new UnknownFrame(frameHeader, buffer);
        }

        if (frameHeader.isFrameEncrypted())  return null;
        if (frameHeader.isFrameUnsynch())    buffer = fromUnsynch(buffer);
        if (frameHeader.isFrameCompressed()) buffer = decompressFrame(buffer);

        try {
            frame.parseFrameData(buffer, frameHeader);
        }
        catch (DateTimeParseException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return frame;
    }

    public final ArrayList<AbstractFrame> parseFrames() {

        FrameHeaderParser headerParser = new FrameHeaderParser(tagHeader);
        ArrayList<AbstractFrame> frames = new ArrayList<>();

        int position = framesOffset;
        while (position < frameData.length) {

            FrameHeader frameHeader = headerParser.parseFrameHeader(frameData, position);
            if (frameHeader == null) {
                paddingOffset = headerParser.getPaddingOffset();
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
