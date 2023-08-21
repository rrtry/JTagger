package com.jtagger.flac;

import com.jtagger.AbstractTagEditor;
import com.jtagger.AbstractTag;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.jtagger.PaddingTag.MAX_PADDING;
import static com.jtagger.PaddingTag.MIN_PADDING;
import static com.jtagger.flac.AbstractMetadataBlock.*;
import static com.jtagger.flac.FlacTag.MAGIC;

public class FlacTagEditor extends AbstractTagEditor<FlacTag> {

    private FlacParser parser;
    private StreamInfoBlock streamInfo;
    private int originalTagSize;

    @Override
    protected void parseTag() throws IOException {

        parser       = new FlacParser();
        tag          = parser.parseTag(file);
        isTagPresent = tag != null;

        if (!isTagPresent) return;
        streamInfo = tag.getBlock(BLOCK_TYPE_STREAMINFO);

        if (streamInfo == null) throw new IllegalStateException("Missing STREAMINFO block");
        originalTagSize = tag.getBlockDataSize();
    }

    public FlacParser getParser() {
        return parser;
    }

    @Override
    public void removeTag() {
        tag.removeBlock(BLOCK_TYPE_VORBIS_COMMENT);
        tag.removeBlock(BLOCK_TYPE_PICTURE);
    }

    @Override
    public void commit() throws IOException {

        if (tag == null) return;

        int padding;
        int paddingBlockSize = 0;

        UnknownMetadataBlock paddingBlock = tag.getBlock(BLOCK_TYPE_PADDING);
        if (paddingBlock != null) {
            paddingBlockSize = paddingBlock.getBytes().length - BLOCK_HEADER_LENGTH;
        }

        padding = originalTagSize - (tag.getBlockDataSize() - paddingBlockSize);
        if (padding >= MIN_PADDING && padding <= MAX_PADDING) {

            tag.setPaddingAmount(padding);
            tag.assemble();

            file.seek(0);
            file.write(tag.getBytes()); // fit tag in padding space
            return;
        }

        final long initialLength = file.length();
        final int bufferSize     = 4096;
        final String suffix      = ".tmp";

        File temp = File.createTempFile(MAGIC, suffix);
        byte[] tempBuffer = new byte[bufferSize];

        try (RandomAccessFile tempFile = new RandomAccessFile(temp.getAbsolutePath(), "rw")) {

            byte[] tagBuffer = tag.getBytes();
            tempFile.write(tagBuffer, 0, tagBuffer.length);

            file.seek(originalTagSize + MAGIC.length());

            while (file.read(tempBuffer, 0, tempBuffer.length) != -1) {
                tempFile.write(tempBuffer, 0, tempBuffer.length);
            }

            file.seek(0);
            tempFile.seek(0);

            while (tempFile.read(tempBuffer, 0, tempBuffer.length) != -1) {
                file.write(tempBuffer, 0, tempBuffer.length);
            }

            long tempLength = tempFile.length();
            if (tempLength < initialLength) {
                file.setLength(tempLength);
            }

        } finally {
            temp.delete();
        }
    }

    @Override
    public void setTag(AbstractTag tag) {

        if (tag instanceof FlacTag) {
            tag.assemble();
            this.tag = (FlacTag) tag;
            return;
        }

        FlacTag flacTag = new FlacTag();
        flacTag.addBlock(streamInfo);

        convertTag(tag, this.tag);
        this.tag.assemble();
    }
}
