package com.rrtry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MediaFile {

    private AbstractTagEditor<Tag> editor;
    private Tag tag;

    public void scan(File file) {

        try {

            String path     = file.getAbsolutePath();
            String mimeType = Files.probeContentType(Paths.get(path));

            editor = TagEditorFactory.getEditor(mimeType);
            editor.load(path);

            tag = editor.getTag();

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void setTag(Tag tag) {
        editor.setTag(tag);
    }

    public Tag getTag() {
        return tag;
    }

    public void save() throws IOException {
        editor.commit();
    }

    public void close() throws IOException {
        editor.release();
    }
}
