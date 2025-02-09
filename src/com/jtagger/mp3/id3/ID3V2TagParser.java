package com.jtagger.mp3.id3;

import com.jtagger.TagParser;

import java.io.IOException;
import com.jtagger.FileWrapper;
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
    public ID3V2Tag parseTag(FileWrapper file) {
        try {

            byte[] tagHeader = new byte[HEADER_LENGTH];
            file.readFully(tagHeader);

            tagHeaderParser.setHeaderData(tagHeader);
            TagHeader header = tagHeaderParser.parse();

            if (header == null)
                return null; // Invalid header, unsupported version or no tag present

            byte[] frameData = new byte[header.getTagSize()];
            file.readFully(frameData);

            int frameDataOffset = 0;
            if (header.isUnsynch() && header.getMajorVersion() == ID3V2_3) {
                frameData = fromUnsynch(frameData);
            }
            if (header.hasExtendedHeader()) {

                byte[] sizeBytes = Arrays.copyOfRange(frameData, 0, 4);
                int extendedHeaderSize = header.getMajorVersion() == ID3V2_4 ?
                        fromSynchSafeInteger(toUInt32BE(sizeBytes)) : toUInt32BE(sizeBytes);

                if (header.getMajorVersion() == ID3V2_4) {
                    frameDataOffset += extendedHeaderSize;
                } else {
                    frameDataOffset += extendedHeaderSize + 4;
                }
            }

            frameParser.setTagHeader(header);
            frameParser.setFramesOffset(frameDataOffset);
            frameParser.setFrames(frameData);

            ArrayList<AbstractFrame> frames = new ArrayList<>();
            frameParser.parseFrames(frames);

            return ID3V2Tag.newBuilder()
                    .setFrames(frames)
                    .setHeader(header)
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
