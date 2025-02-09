package com.jtagger.mp3.id3;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.jtagger.mp3.id3.AbstractFrame.decompressFrame;
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

        final int from = pos + FrameHeader.FRAME_HEADER_DATA_OFFSET;
        final int to   = from + frameSize;

        if (to > frameData.length)
            return null;
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
                case AbstractFrame.TABLE_OF_CONTENTS:
                    frame = new TableOfContentsFrame();
                    break;
                case AbstractFrame.CHAPTER:
                    frame = new ChapterFrame();
                    break;
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

        if (buffer == null)
            return null;
        if (frame == null)
            return new UnknownFrame(frameHeader, buffer);

        if (frameHeader.isFrameEncrypted())  return null;
        if (frameHeader.isFrameUnsynch())    buffer = fromUnsynch(buffer);
        if (frameHeader.isFrameCompressed()) buffer = decompressFrame(buffer);

        try {
            frame.parseFrameData(buffer, frameHeader);
        }
        catch (DateTimeParseException |
               IllegalArgumentException e)
        {
            return new UnknownFrame(frameHeader, buffer);
        }
        return frame;
    }

    public final void parseFrames(ArrayList<AbstractFrame> frames) {

        FrameHeaderParser headerParser = new FrameHeaderParser(tagHeader);
        int position = framesOffset;

        while (position < frameData.length) {

            if (position + 10 >= frameData.length)
                break;

            FrameHeader frameHeader = headerParser.parseFrameHeader(frameData, position);
            if (frameHeader == null) {
                paddingOffset = headerParser.getPaddingOffset();
                break;
            }

            int frameSize = frameHeader.getFrameSize();
            if (frameSize > 0) {
                AbstractFrame frame = parseFrame(position, frameHeader);
                if (frame != null) frames.add(frame);
            }
            position += (frameSize + FrameHeader.FRAME_HEADER_LENGTH);
        }
    }
}
