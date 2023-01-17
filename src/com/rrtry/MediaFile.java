package com.rrtry;

import com.rrtry.utils.FileContentTypeDetector;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MediaFile<T extends Tag> {

    protected RandomAccessFile file;
    protected AbstractTagEditor<T> editor;
    protected T tag;

    public void scan(File f) throws IOException {

        String path     = f.getAbsolutePath();
        String mimeType = FileContentTypeDetector.getFileContentType(f);

        if (mimeType == null) return;

        editor = getEditor(mimeType);
        if (editor == null) return;

        file   = new RandomAccessFile(path, "rw");
        editor.load(file, mimeType);
        tag = editor.getTag();
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
