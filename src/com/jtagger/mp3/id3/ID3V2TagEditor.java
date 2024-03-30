package com.jtagger.mp3.id3;

import com.jtagger.AbstractTagEditor;
import com.jtagger.AbstractTag;
import com.jtagger.utils.BytesIO;

import java.io.*;

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
        hasTag = false;

        headerParser = new TagHeaderParser();
        frameParser  = new FrameParser();
        tagParser    = new ID3V2TagParser(headerParser, frameParser);

        if ((tag = tagParser.parseTag(file)) != null) {

            TagHeader header = tag.getTagHeader();
            hasTag       = true;
            originalSize = header.getTagSize();
            hasFooter    = header.hasFooter();

            if (frameParser.getPaddingOffset() != 0 && hasFooter) {
                throw new IllegalStateException("Tag cannot have both padding and footer");
            }
        }

        ID3V1TagParser parser = new ID3V1TagParser();
        ID3V1Tag id3V1Tag = parser.parseTag(file);

        hasID3v1 = id3V1Tag != null;
        if (hasID3v1) {
            if (tag != null) {
                tag.mergeWithID3V1(id3V1Tag);
            } else {
                tag = ID3V2Tag.fromID3V1Tag(id3V1Tag, ID3V2Tag.ID3V2_3);
            }
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
        int padding = BytesIO.PADDING_MIN;
        if (tag != null) {

            byte[] tagBuffer = tag.getBytes();
            if (hasTag && !hasFooter) {

                int tagData = tagBuffer.length - HEADER_LENGTH;
                int maxPad  = BytesIO.getPadding((int) file.length());
                padding = originalSize - tagData;

                if (padding > 0 && padding <= maxPad) {
                    BytesIO.writeBlock(file, tagBuffer, 0);
                    file.write(new byte[padding]);
                    BytesIO.writeBlock(
                            file, fromUInt32BE(toSynchSafeInteger(tagData + padding)),
                            SIZE_OFFSET
                    );
                    return;
                } else {
                    padding = BytesIO.PADDING_MIN;
                }
            }

            int headers = hasFooter ? HEADER_LENGTH * 2 : HEADER_LENGTH;
            int length  = hasTag ? tagBuffer.length - HEADER_LENGTH + padding : tagBuffer.length + padding;

            int diff = length - (originalSize + (hasFooter ? HEADER_LENGTH : 0));
            int from = hasTag ? originalSize + headers : 0;
            int to   = from + diff;

            BytesIO.moveBlock(file, from, to, diff, (int) file.length() - from);
            BytesIO.writeBlock(file, tagBuffer, 0);
            file.write(new byte[padding]);
            BytesIO.writeBlock(
                    file,
                    fromUInt32BE(toSynchSafeInteger(tagBuffer.length - HEADER_LENGTH + padding)),
                    SIZE_OFFSET
            );
        }
        else if (hasTag) {
            int totalSize = originalSize + HEADER_LENGTH * (hasFooter ? 2 : 1);
            BytesIO.moveBlock(
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
