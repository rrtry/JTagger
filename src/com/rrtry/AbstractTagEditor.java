package com.rrtry;

import com.rrtry.utils.FileContentTypeDetector;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;

public abstract class AbstractTagEditor<T extends Tag> {

    protected RandomAccessFile file;
    protected T tag;
    protected boolean isTagPresent = false;

    protected String mimeType;

    abstract protected void parseTag() throws IOException;
    abstract protected String getFileMimeType();

    abstract public void commit() throws IOException;
    abstract public void setTag(Tag tag);

    protected final void convertTag(Tag from, T to) {

        AttachedPicture picture = from.getPictureField();
        if (picture != null) to.setPictureField(picture);

        for (String field : Tag.FIELDS) {

            if (field.equals(Tag.PICTURE)) {
                continue;
            }

            String value = from.getStringField(field);
            if (value != null && !value.isEmpty()) {
                to.setStringField(field, value);
            }
        }
    }

    public void load(File file) throws IOException {

        if (this.file != null) {
            release();
        }

        String path     = file.getAbsolutePath();
        String mimeType = FileContentTypeDetector.getFileContentType(file);

        if (mimeType == null) return;
        if (mimeType.equals(getFileMimeType())) {

            this.file     = new RandomAccessFile(path, "rw");
            this.mimeType = mimeType;

            parseTag();
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
