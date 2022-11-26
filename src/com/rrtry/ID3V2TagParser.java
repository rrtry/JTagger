package com.rrtry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static com.rrtry.TagHeaderParser.HEADER_LENGTH;

public class ID3V2TagParser implements TagParser<ID3V2Tag> {

    private final TagHeaderParser tagHeaderParser;
    private final FrameParser frameParser;

    public ID3V2TagParser(TagHeaderParser tagHeaderParser, FrameParser frameParser) {
        this.tagHeaderParser = tagHeaderParser;
        this.frameParser = frameParser;
    }

    @Override
    public ID3V2Tag parse(RandomAccessFile file) throws IOException {
        try {

            tagHeaderParser.read(file);

            final int from = tagHeaderParser.parseExtendedHeaderSize() + HEADER_LENGTH;
            final int to   = tagHeaderParser.parseTagSize();

            frameParser.read(file, from, to);

            TagHeader header = tagHeaderParser.parse();
            ArrayList<AbstractFrame> frames = frameParser.parseFrames();

            final int size = header.getTagSize();
            byte[] tagBytes = new byte[size + HEADER_LENGTH];

            file.seek(0);
            file.read(tagBytes, 0, HEADER_LENGTH);
            file.read(tagBytes, HEADER_LENGTH, size);

            return ID3V2Tag.newBuilder()
                    .setHeader(header)
                    .setFrames(frames)
                    .buildExisting(tagBytes);

        } catch (InvalidTagException e) {
            return null;
        }
    }
}
