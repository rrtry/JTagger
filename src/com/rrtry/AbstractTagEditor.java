package com.rrtry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractTagEditor<T extends Tag> {

    public static final String MPEG_MIME_TYPE = "audio/mpeg";
    public static final String FLAC_MIME_TYPE = "audio/flac";
    public static final String OGG_MIME_TYPE  = "audio/x-vorbis+ogg";

    protected RandomAccessFile file;
    protected T tag;
    protected boolean isTagPresent = false;

    protected String path;

    abstract protected void parseTag() throws IOException;
    abstract protected String getFileMimeType();

    abstract public void commit() throws IOException;
    abstract public void setTag(T tag);

    public void load(String path) throws IOException {
        this.path = path;
        if (file != null) release();
        if (Files.probeContentType(Paths.get(path)).equals(getFileMimeType())) {
            this.file = new RandomAccessFile(path, "rw");
            parseTag();
            return;
        }
        throw new IllegalArgumentException(path + " has invalid mime type");
    }

    void load(RandomAccessFile file) throws IOException {
        this.file = file;
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
