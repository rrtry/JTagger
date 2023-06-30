package com.jtagger;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class AbstractTagEditor<T extends AbstractTag> {

    protected RandomAccessFile file;
    protected T tag;
    protected boolean isTagPresent = false;

    protected String mimeType;

    abstract protected void parseTag() throws IOException;
    abstract protected String getFileMimeType();

    abstract public void commit() throws IOException;
    abstract public void setTag(AbstractTag tag);

    protected final void convertTag(AbstractTag from, T to) {

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

    public void removeTag() {
        this.tag = null;
    }

    public void release() throws IOException {
        file.close();
        tag = null;
    }
}
