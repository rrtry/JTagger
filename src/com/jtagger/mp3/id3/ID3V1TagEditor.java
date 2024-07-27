package com.jtagger.mp3.id3;

import com.jtagger.AbstractTagEditor;
import com.jtagger.AbstractTag;

import java.io.IOException;
import static com.jtagger.mp3.id3.ID3V1Tag.ID3V1_1;

public class ID3V1TagEditor extends AbstractTagEditor<ID3V1Tag> {

    @Override
    protected final void parseTag() throws IOException {
        ID3V1TagParser parser = new ID3V1TagParser();
        tag = parser.parseTag(file);
        hasTag = tag != null;
    }

    @Override
    public void setTag(AbstractTag tag) {
        if (tag instanceof ID3V1Tag) {
            this.tag = (ID3V1Tag) tag;
            return;
        }
        ID3V1Tag id3v1 = new ID3V1Tag();
        convertTag(tag, id3v1);
        this.tag = id3v1;
    }

    @Override
    public void commit() throws IOException {
        super.commit();
        if (tag == null && !hasTag) {
            return;
        }
        if (tag != null) {
            file.seek(hasTag ? file.length() - 128 : file.length());
            file.write(getTag().getBytes());
            return;
        }
        file.setLength(file.length() - 128); // truncate file
    }
}
