package com.rrtry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractTagEditor<T extends Tag> {

    protected RandomAccessFile file;
    protected T tag;
    protected boolean isTagPresent = false;

    abstract protected void parseTag() throws IOException;
    abstract protected String getFileMimeType() throws IOException;

    abstract public void commit() throws IOException;
    abstract public void setTag(T tag);

    public final boolean isTagPresent() {
        return isTagPresent;
    }

    public final void load(String path) throws IOException {
        if (file != null) {
            release();
        }
        if (Files.probeContentType(Paths.get(path)).equals(getFileMimeType())) {
            this.file = new RandomAccessFile(path, "rw");
            parseTag();
            return;
        }
        throw new IllegalArgumentException(path + " has invalid mime type");
    }

    public final void load(RandomAccessFile file) throws IOException {
        this.file = file;
        parseTag();
    }

    public final T getTag() {
        return tag;
    }
    public final void removeTag() {
        this.tag = null;
    }

    public final void release() throws IOException {
        file.close(); tag = null;
    }
}
