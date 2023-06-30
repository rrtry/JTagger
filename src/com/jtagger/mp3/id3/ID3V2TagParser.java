package com.jtagger.mp3.id3;

import com.jtagger.InvalidTagException;
import com.jtagger.TagParser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.ArrayList;

import static com.jtagger.mp3.id3.ID3SynchSafeInteger.fromSynchSafeInteger;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_3;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_4;
import static com.jtagger.mp3.id3.TagHeaderParser.HEADER_LENGTH;
import static com.jtagger.mp3.id3.UnsynchronisationUtils.fromUnsynch;
import static com.jtagger.utils.IntegerUtils.toUInt32BE;

public class ID3V2TagParser implements TagParser<ID3V2Tag> {

    private final TagHeaderParser tagHeaderParser;
    private final FrameParser frameParser;

    public ID3V2TagParser(TagHeaderParser tagHeaderParser, FrameParser frameParser) {
        this.tagHeaderParser = tagHeaderParser;
        this.frameParser = frameParser;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ID3V2Tag parseTag(RandomAccessFile file) {
        try {

            byte[] tagHeader = new byte[HEADER_LENGTH];
            file.read(tagHeader, 0, tagHeader.length);

            tagHeaderParser.setHeaderData(tagHeader);
            TagHeader header = tagHeaderParser.parse();

            byte[] frameData    = new byte[header.getTagSize()];
            file.read(frameData, 0, header.getTagSize());

            int frameDataOffset = 0;
            if (header.isUnsynch() && header.getMajorVersion() == ID3V2_3) {
                frameData = fromUnsynch(frameData);
            }
            if (header.hasExtendedHeader()) {

                byte[] sizeBytes = Arrays.copyOfRange(frameData, HEADER_LENGTH, 4);
                int extendedHeaderSize = header.getMajorVersion() == ID3V2_4 ? fromSynchSafeInteger(toUInt32BE(sizeBytes)) : toUInt32BE(sizeBytes);

                if (header.getMajorVersion() == ID3V2_4) {
                    frameDataOffset += extendedHeaderSize;
                } else {
                    frameDataOffset += extendedHeaderSize + 6;
                }
            }

            byte[] tag = new byte[tagHeader.length + frameData.length];
            System.arraycopy(tagHeader, 0, tag, 0, tagHeader.length);
            System.arraycopy(frameData, 0, tag, tagHeader.length, frameData.length);

            frameParser.setTagHeader(header);
            frameParser.setFrames(Arrays.copyOfRange(frameData, frameDataOffset, frameData.length));
            ArrayList<AbstractFrame> frames = frameParser.parseFrames();

            return ID3V2Tag.newBuilder()
                    .setFrames(frames)
                    .setHeader(header)
                    .buildExisting(tag);

        } catch (IOException | InvalidTagException e) {
            e.printStackTrace();
            return null;
        }
    }
}
