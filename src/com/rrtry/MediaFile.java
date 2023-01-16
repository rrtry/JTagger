package com.rrtry;

import com.rrtry.utils.FileContentTypeDetector;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MediaFile<T extends Tag> {

    protected RandomAccessFile file;
    protected AbstractTagEditor<T> editor;
    protected T tag;

    public void scan(File f) {
        try {

            String path     = f.getAbsolutePath();
            String mimeType = FileContentTypeDetector.getFileContentType(f);

            if (mimeType == null) {
                throw new IllegalStateException("Could not determine content type for: " + f.getName());
            }

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
