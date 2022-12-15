package flac;

import com.rrtry.AbstractTagEditor;
import com.rrtry.id3.TagHeaderParser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import static flac.FlacTagParser.MAGIC;

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
            this.originalTagSize = tag.getSize();
        }
    }

    @Override
    protected String getFileMimeType() {
        return FLAC_MIME_TYPE;
    }

    @Override
    public void commit() throws IOException {

        final int bufferSize = 4096;
        final String suffix = ".tmp";

        File temp = File.createTempFile(MAGIC, suffix);

        byte[] tempBuffer = new byte[bufferSize];
        byte[] magicBytes = MAGIC.getBytes(StandardCharsets.US_ASCII);

        try (RandomAccessFile tempFile = new RandomAccessFile(temp.getAbsolutePath(), "rw")) {

            byte[] tagBuffer;
            tempFile.write(magicBytes, 0, magicBytes.length);

            if (tag != null) tagBuffer = tag.getBytes();
            else tagBuffer = streamInfo.getBytes();

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
