package com.rrtry;

import com.rrtry.mpeg.id3.ID3V2Tag;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MediaFile<T extends Tag> {

    protected RandomAccessFile file;
    protected AbstractTagEditor<T> editor;
    protected T tag;

    public void scan(File f) {
        try {

            String path     = f.getAbsolutePath();
            String mimeType = Files.probeContentType(Paths.get(path));

            editor = getEditor(mimeType);
            file   = new RandomAccessFile(path, "rw");
            editor.load(file, mimeType);

            tag = editor.getTag();

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @SuppressWarnings("unchecked")
    protected AbstractTagEditor<T> getEditor(String mimeType) {
        return TagEditorFactory.getEditor(mimeType);
    }

    public void setTag(T tag) {
        editor.setTag(tag);
    }

    public T getTag() {
        return tag;
    }

    public void save() throws IOException {
        editor.commit();
    }

    public void close() throws IOException {
        editor.release();
    }
}
