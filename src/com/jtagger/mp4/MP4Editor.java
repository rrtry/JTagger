package com.jtagger.mp4;

import com.jtagger.AbstractTag;
import com.jtagger.AbstractTagEditor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.jtagger.utils.FileContentTypeDetector.M4A_MIME_TYPE;

public class MP4Editor extends AbstractTagEditor<MP4> {

    @Override
    protected void parseTag() {
        MP4Parser parser = new MP4Parser();
        this.tag = parser.parseTag(file);
    }

    @Override
    protected String getFileMimeType() {
        return M4A_MIME_TYPE;
    }

    // TODO: make use of padding ('free' atom)
    @Override
    public void commit() throws IOException {

        if (tag == null) {
            throw new IllegalStateException("Corrupted mp4 file");
        }

        byte[] mp4Buffer  = tag.getBytes();
        byte[] tempBuffer = new byte[1024];

        if (mp4Buffer.length == tag.getInitialSize()) {
            file.seek(0);
            file.write(mp4Buffer);
            return;
        }

        File temp = File.createTempFile("M4A", ".tmp");
        RandomAccessFile tempFile = new RandomAccessFile(temp.getAbsolutePath(), "rw");

        tempFile.write(mp4Buffer);
        file.seek(getTag().getInitialSize());

        while (file.read(tempBuffer) != -1) {
            tempFile.write(tempBuffer);
        }

        file.seek(0);
        tempFile.seek(0);

        while (tempFile.read(tempBuffer) != -1) {
            file.write(tempBuffer);
        }

        tempFile.close();
        if (!temp.delete()) {
            System.err.println("Could not delete temp file");
        }
    }

    @Override
    public void setTag(AbstractTag newTag) {

        if (tag == null) {
            throw new IllegalStateException("Corrupted mp4 file");
        }
        if (newTag instanceof MP4) {
            MP4 mp4 = (MP4) newTag;
            tag.setMetadataAtoms(mp4.getMetadataAtoms());
            tag.assemble();
            return;
        }

        tag.removeMetadataAtoms();
        convertTag(newTag, tag);
        tag.assemble();
    }
}
