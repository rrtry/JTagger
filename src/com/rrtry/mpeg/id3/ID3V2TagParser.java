package com.rrtry.mpeg.id3;

import com.rrtry.InvalidTagException;
import com.rrtry.TagParser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.ArrayList;

import static com.rrtry.mpeg.id3.ID3SynchSafeInteger.fromSynchSafeInteger;
import static com.rrtry.mpeg.id3.ID3V2Tag.ID3V2_3;
import static com.rrtry.mpeg.id3.ID3V2Tag.ID3V2_4;
import static com.rrtry.mpeg.id3.TagHeaderParser.HEADER_LENGTH;
import static com.rrtry.mpeg.id3.UnsynchronisationUtils.fromUnsynch;
import static com.rrtry.utils.IntegerUtils.toUInt32BE;

public class ID3V2TagParser implements TagParser<ID3V2Tag> {

    private final TagHeaderParser tagHeaderParser;
    private final FrameParser frameParser;

    public ID3V2TagParser(TagHeaderParser tagHeaderParser, FrameParser frameParser) {
        this.tagHeaderParser = tagHeaderParser;
        this.frameParser = frameParser;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ID3V2Tag parse(RandomAccessFile file) throws IOException {
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

            frameParser.setTagHeader(header);
            frameParser.setFrames(Arrays.copyOfRange(frameData, frameDataOffset, frameData.length));
            ArrayList<AbstractFrame> frames = frameParser.parseFrames();

            return ID3V2Tag.newBuilder()
                    .setFrames(frames)
                    .setHeader(header)
                    .build(header.getMajorVersion());

        } catch (InvalidTagException e) {
            return null;
        }
    }
}
