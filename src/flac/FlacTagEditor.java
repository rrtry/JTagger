package flac;

import com.rrtry.AbstractTagEditor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.rrtry.PaddingTag.MAX_PADDING;
import static com.rrtry.PaddingTag.MIN_PADDING;
import static flac.AbstractMetadataBlock.BLOCK_HEADER_LENGTH;
import static flac.AbstractMetadataBlock.BLOCK_TYPE_PADDING;
import static flac.FlacTag.MAGIC;

public class FlacTagEditor extends AbstractTagEditor<FlacTag> {

    private static final String FLAC_MIME_TYPE = "audio/flac";

    private StreamInfoBlock streamInfo;
    private int originalTagSize;

    @Override
    protected void parseTag() throws IOException {

        FlacTagParser parser = new FlacTagParser();

        this.tag = parser.parse(file);
        this.streamInfo = tag.getBlock(AbstractMetadataBlock.BLOCK_TYPE_STREAMINFO);

        if (tag != null) {

            this.isTagPresent = true;
            this.originalTagSize = tag.getBlockDataSize();
        }
    }

    @Override
    public void removeTag() {

        FlacTag tag = new FlacTag();
        tag.addBlock(streamInfo); // remove all blocks except STREAMINFO
        tag.assemble();

        this.tag = tag;
    }

    @Override
    protected String getFileMimeType() {
        return FLAC_MIME_TYPE;
    }

    @Override
    public void commit() throws IOException {

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

        final int bufferSize = 4096;
        final String suffix = ".tmp";

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

        } finally {
            temp.delete();
        }
    }

    @Override
    public void setTag(FlacTag tag) {
        tag.assemble();
        this.tag = tag;
    }
}
