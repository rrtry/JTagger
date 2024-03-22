package com.jtagger.mp3.id3;

import com.jtagger.AbstractTagEditor;
import com.jtagger.AbstractTag;
import com.jtagger.utils.FileIO;

import java.io.*;

import static com.jtagger.PaddingTag.MAX_PADDING;
import static com.jtagger.mp3.id3.ID3SynchSafeInteger.toSynchSafeInteger;
import static com.jtagger.mp3.id3.TagHeaderParser.HEADER_LENGTH;
import static com.jtagger.mp3.id3.TagHeaderParser.SIZE_OFFSET;
import static com.jtagger.utils.IntegerUtils.fromUInt32BE;

public class ID3V2TagEditor extends AbstractTagEditor<ID3V2Tag> {

    private int originalSize;
    private boolean hasID3v1;
    private boolean hasFooter;

    @Override
    protected final void parseTag() throws IOException {

        TagHeaderParser headerParser;
        FrameParser frameParser;
        ID3V2TagParser tagParser;

        originalSize = 0;
        hasID3v1     = false;
        isTagPresent = false;

        headerParser = new TagHeaderParser();
        frameParser  = new FrameParser();
        tagParser    = new ID3V2TagParser(headerParser, frameParser);

        if ((tag = tagParser.parseTag(file)) != null) {

            TagHeader header = tag.getTagHeader();
            isTagPresent = true;
            originalSize = header.getTagSize();
            hasFooter    = header.hasFooter();

            if (frameParser.getPaddingOffset() != 0 && hasFooter) {
                throw new IllegalStateException("Tag cannot have both padding and footer");
            }
            return;
        }

        ID3V1TagParser parser = new ID3V1TagParser();
        ID3V1Tag id3V1Tag = parser.parseTag(file);
        if (id3V1Tag != null) {
            hasID3v1 = true;
            tag = ID3V2Tag.fromID3V1Tag(id3V1Tag, ID3V2Tag.ID3V2_3);
        }
    }

    @Override
    public void setTag(AbstractTag tag) {

        if (tag instanceof ID3V2Tag) {

            ID3V2Tag id3V2Tag = (ID3V2Tag) tag;
            id3V2Tag.assemble(id3V2Tag.getVersion());
            this.tag = id3V2Tag;

            return;
        }

        ID3V2Tag id3V2Tag = new ID3V2Tag();
        id3V2Tag.setTagHeader(new TagHeader());

        convertTag(tag, id3V2Tag);
        id3V2Tag.assemble(id3V2Tag.getVersion());

        this.tag = id3V2Tag;
    }

    @Override
    public void commit() throws IOException {
        if (tag != null) {

            byte[] tagBuffer = tag.getBytes();
            if (isTagPresent && !hasFooter) {

                int tagData = tagBuffer.length - HEADER_LENGTH;
                int padding = originalSize - tagData;

                if (padding >= 0 && padding <= MAX_PADDING) {
                    FileIO.writeBlock(file, tagBuffer, 0);
                    file.write(new byte[padding]);
                    FileIO.writeBlock(
                            file, fromUInt32BE(toSynchSafeInteger(tagData + padding)),
                            SIZE_OFFSET
                    );
                    return;
                }
            }

            int headers = hasFooter ? HEADER_LENGTH * 2 : HEADER_LENGTH;
            int padding = FileIO.getPadding((int) file.length());
            int length  = isTagPresent ? tagBuffer.length - HEADER_LENGTH + padding : tagBuffer.length + padding;

            int diff = length - (originalSize + (hasFooter ? HEADER_LENGTH : 0));
            int from = isTagPresent ? originalSize + headers : 0;
            int to   = from + diff;

            FileIO.moveBlock(file, from, to, diff, (int) file.length() - from);
            FileIO.writeBlock(file, tagBuffer, 0);
            file.write(new byte[padding]);
            FileIO.writeBlock(
                    file,
                    fromUInt32BE(toSynchSafeInteger(tagBuffer.length - HEADER_LENGTH + padding)),
                    SIZE_OFFSET
            );
        }
        else if (isTagPresent) {
            int totalSize = originalSize + HEADER_LENGTH * (hasFooter ? 2 : 1);
            FileIO.moveBlock(
                    file,
                    totalSize,
                    0,
                    -totalSize,
                    (int) file.length() - totalSize
            );
        } else {
            return;
        }
        if (hasID3v1) {
            file.setLength(file.length() - 128);
        }
    }
}
