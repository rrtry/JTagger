package com.rrtry;

import com.rrtry.id3.ID3V2TagEditor;
import flac.FlacTagEditor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractTagEditor<T extends Tag> {

    public static final String MPEG_MIME_TYPE = "audio/mpeg";
    public static final String FLAC_MIME_TYPE = "audio/flac";

    protected RandomAccessFile file;
    protected T tag;
    protected boolean isTagPresent = false;

    protected String path;

    abstract protected void parseTag() throws IOException;
    abstract protected String getFileMimeType();

    abstract public void commit() throws IOException;
    abstract public void setTag(T tag);

    public final void load(String path) throws IOException {
        this.path = path;
        if (file != null) release();
        if (Files.probeContentType(Paths.get(path)).equals(getFileMimeType())) {
            this.file = new RandomAccessFile(path, "rw");
            parseTag();
            return;
        }
        throw new IllegalArgumentException(path + " has invalid mime type");
    }

    public final T getTag() {
        return tag;
    }

    public void removeTag() {
        this.tag = null;
    }

    public final void release() throws IOException {
        file.close();
        tag = null;
    }
}
