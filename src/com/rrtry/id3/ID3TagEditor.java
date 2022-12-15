package com.rrtry.id3;

import java.io.IOException;
import java.io.RandomAccessFile;

import static com.rrtry.id3.ID3V1Tag.*;
import static com.rrtry.id3.ID3V2Tag.*;

public class ID3TagEditor {

    private RandomAccessFile file;
    private ID3V2TagEditor id3V2TagEditor;
    private ID3V1TagEditor id3V1TagEditor;

    public boolean isTagPresent(byte version) {
        if (version == ID3V1) return id3V1TagEditor.isTagPresent();
        if (version == ID3V2) return id3V2TagEditor.isTagPresent();
        throw new IllegalStateException("Invalid version number: " + version);
    }

    public ID3V1Tag getID3V1Tag() {
        return id3V1TagEditor.getTag();
    }

    public ID3V2Tag getID3V2Tag() {
        return id3V2TagEditor.getTag();
    }

    public void setID3V1Tag(ID3V1Tag tag) {
        id3V1TagEditor.setTag(tag);
    }

    public void setID3V2Tag(ID3V2Tag tag) {
        id3V2TagEditor.setTag(tag);
    }

    public void removeTag(byte version) {
        if (version == ID3V1) {
            id3V1TagEditor.removeTag(); return;
        }
        if (version == ID3V2) {
            id3V2TagEditor.removeTag(); return;
        }
        throw new IllegalArgumentException("Invalid version number: " + version);
    }

    public void commit() throws IOException {
        id3V1TagEditor.commit();
        id3V2TagEditor.commit();
    }

    public void commit(byte version) throws IOException {
        if (version == ID3V1) {
            id3V1TagEditor.commit(); return;
        }
        if (version == ID3V2) {
            id3V2TagEditor.commit(); return;
        }
        throw new IllegalArgumentException("Invalid version number: " + version);
    }

    public void load(final String path) throws IOException {

        if (this.file != null) release();
        this.file = new RandomAccessFile(path, "rw");

        id3V1TagEditor = new ID3V1TagEditor();
        id3V2TagEditor = new ID3V2TagEditor();

        id3V1TagEditor.load(file);
        id3V2TagEditor.load(file);
    }

    public void release() throws IOException {

        file.close();
        id3V2TagEditor.release();
        id3V1TagEditor.release();

        id3V1TagEditor = null; id3V2TagEditor = null;
    }
}
