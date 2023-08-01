package com.jtagger.mp3.id3;

import com.jtagger.AbstractTagEditor;
import com.jtagger.AbstractTag;
import com.jtagger.MediaFile;

import java.io.*;

import static com.jtagger.PaddingTag.MAX_PADDING;
import static com.jtagger.PaddingTag.MIN_PADDING;
import static com.jtagger.mp3.id3.TagHeaderParser.HEADER_LENGTH;

public class ID3V2TagEditor extends AbstractTagEditor<ID3V2Tag> {

    private int originalTagSize;
    private boolean isID3V1Present;

    @Override
    protected final void parseTag() throws IOException {

        originalTagSize = 0;
        isID3V1Present  = false;
        isTagPresent    = false;

        TagHeaderParser tagHeaderParser = new TagHeaderParser();
        FrameParser frameParser         = new FrameParser();

        ID3V2TagParser id3V2TagParser = new ID3V2TagParser(tagHeaderParser, frameParser);
        tag = id3V2TagParser.parseTag(file);

        if (tag != null) {

            isTagPresent    = true;
            originalTagSize = tag.getTagHeader().getTagSize();

            if (tag.getTagHeader().hasFooter()) {
                originalTagSize += HEADER_LENGTH;
            }
            return;
        }

        ID3V1TagParser parser = new ID3V1TagParser();
        ID3V1Tag id3V1Tag = parser.parseTag(file);

        if (id3V1Tag != null) {
            isID3V1Present = true;
            this.tag = ID3V2Tag.fromID3V1Tag(id3V1Tag, ID3V2Tag.ID3V2_3);
        }
    }

    @Override
    protected String getFileMimeType() {
        return MediaFile.FileContentTypeDetector.MPEG_MIME_TYPE;
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

        if (tag == null && !isTagPresent) return; // there's nothing to commit

        /* experimental */
        if (tag != null && isTagPresent) {

            final int frameDataSize = tag.getFrameDataSize(tag.getVersion());
            final int padding       = originalTagSize - frameDataSize;

            if (padding >= MIN_PADDING && padding <= MAX_PADDING) {

                tag.setPaddingAmount(padding);
                tag.assemble(tag.getVersion());

                byte[] tagBytes = tag.getBytes();
                file.seek(0);
                file.write(tagBytes, 0, tagBytes.length);
                return;
            }
        }

        final long initialLength = file.length();
        final int bufferSize     = 4096;

        final String prefix  = "ID3";
        final String suffix  = ".tmp";

        File temp = File.createTempFile(prefix, suffix);
        byte[] tempBuffer = new byte[bufferSize];

        try (RandomAccessFile tempFile = new RandomAccessFile(temp.getAbsolutePath(), "rw")) {

            if (tag != null) {
                byte[] tagBuffer = tag.getBytes();
                tempFile.write(tagBuffer, 0, tagBuffer.length);
            }

            int streamOffset = 0;
            if (originalTagSize > 0) {
                streamOffset = originalTagSize + HEADER_LENGTH; // footer is already included in originalTagSize, append header
            }
            if (isID3V1Present) {
                file.setLength(file.length() - 128);
            }

            file.seek(streamOffset);

            while (file.read(tempBuffer) != -1) {
                tempFile.write(tempBuffer);
            }

            file.seek(0);
            tempFile.seek(0);

            while (tempFile.read(tempBuffer) != -1) {
                file.write(tempBuffer);
            }

            long tempLength = tempFile.length();
            if (tempLength < initialLength) {
                file.setLength(tempLength);
            }

        } finally {
            temp.delete();
        }
    }
}
