package com.rrtry.mpeg.id3;

import com.rrtry.AbstractTagEditor;
import com.rrtry.AbstractTag;
import com.rrtry.utils.FileContentTypeDetector;

import java.io.IOException;

public class ID3V1TagEditor extends AbstractTagEditor<ID3V1Tag> {

    @Override
    protected final void parseTag() throws IOException {

        ID3V1TagParser parser = new ID3V1TagParser();

        this.tag          = parser.parseTag(file);
        this.isTagPresent = tag != null;
    }

    @Override
    protected String getFileMimeType() {
        return FileContentTypeDetector.MPEG_MIME_TYPE;
    }

    @Override
    public void setTag(AbstractTag tag) {
        if (tag instanceof ID3V1Tag) {
            ID3V1Tag id3V1Tag = (ID3V1Tag) tag;
            this.tag = ID3V1Tag.newBuilder(id3V1Tag)
                    .build(id3V1Tag.getVersion());
            return;
        }
        throw new RuntimeException();
    }

    @Override
    public void commit() throws IOException {
        if (tag == null && !isTagPresent) {
            return;
        }
        if (tag != null) {

            if (isTagPresent) file.seek(file.length() - 128);
            else file.seek(file.length());

            file.write(getTag().getBytes());
            return;
        }
        file.setLength(file.length() - 128); // truncate file
    }
}
