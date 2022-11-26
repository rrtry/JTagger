package com.rrtry;

import java.io.*;

public class ID3V2TagEditor extends AbstractTagEditor<ID3V2Tag> {

    private int originalTagSize;

    @Override
    protected final void parseTag() throws IOException {

        TagHeaderParser tagHeaderParser = new TagHeaderParser();
        FrameParser frameParser = new FrameParser(tagHeaderParser);

        ID3V2TagParser id3V2TagParser = new ID3V2TagParser(tagHeaderParser, frameParser);
        tag = id3V2TagParser.parse(file);

        if (tag != null) {

            isTagPresent = true;
            originalTagSize = tag.getTagHeader().getTagSize();
        }
    }

    @Override
    public void setTag(ID3V2Tag tag) {
        this.tag = ID3V2Tag.newBuilder(tag).build(tag.getVersion());
    }

    @Override
    public void commit() throws IOException {

        if (tag == null && !isTagPresent) return; // there's nothing to commit

        final int bufferSize = 4096;
        final String prefix = "ID3";
        final String suffix = ".tmp";

        File temp = File.createTempFile(prefix, suffix);
        byte[] tempBuffer = new byte[bufferSize];

        try (RandomAccessFile tempFile = new RandomAccessFile(temp.getAbsolutePath(), "rw")) {

            if (tag != null) {
                byte[] tagBuffer = tag.getBytes();
                tempFile.write(tagBuffer, 0, tagBuffer.length);
            }

            file.seek(originalTagSize + TagHeaderParser.HEADER_LENGTH);

            while (file.read(tempBuffer, 0, tempBuffer.length) != -1) {
                tempFile.write(tempBuffer, 0, tempBuffer.length);
            }

            file.seek(0);
            tempFile.seek(0);

            while (tempFile.read(tempBuffer, 0, tempBuffer.length) != -1) {
                file.write(tempBuffer, 0, tempBuffer.length);
            }
        } finally {
            temp.delete();
        }
    }
}
