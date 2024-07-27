package com.jtagger;

import com.jtagger.mp3.id3.ID3Tag;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class AbstractTagEditor<T extends AbstractTag> {

    protected RandomAccessFile file;
    protected T tag;
    protected boolean hasTag = false;

    protected String mimeType;

    abstract protected void parseTag() throws IOException;
    abstract public void setTag(AbstractTag tag);

    public void commit() throws IOException {
        if (tag != null) {
            if (tag instanceof ID3Tag) {
                tag.assemble(((ID3Tag) tag).getVersion());
            } else {
                tag.assemble();
            }
        }
    }

    protected void convertTag(AbstractTag from, T to) {

        AttachedPicture picture = from.getPictureField();
        if (picture != null) to.setPictureField(picture);

        for (String field : AbstractTag.FIELDS) {

            if (field.equals(AbstractTag.PICTURE)) {
                continue;
            }

            String value = from.getStringField(field);
            if (value != null && !value.isEmpty()) {
                to.setStringField(field, value);
            }
        }
    }

    void load(RandomAccessFile file, String mimeType) throws IOException {

        if (this.file != null) release();
        this.mimeType = mimeType;
        this.file     = file;

        parseTag();
    }

    public T getTag() {
        return tag;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void removeTag() {
        this.tag = null;
    }

    public void release() throws IOException {
        file.close();
        tag      = null;
        mimeType = null;
    }
}
