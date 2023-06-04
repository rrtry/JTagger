package com.rrtry;

import com.rrtry.utils.FileContentTypeDetector;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MediaFile<T extends AbstractTag, I extends StreamInfo> {

    protected RandomAccessFile file;

    protected AbstractTagEditor<T> tagEditor;
    protected StreamInfoParser<I> streamInfoParser;

    protected T tag;
    protected I streamInfo;

    public void scan(File fileObj) throws IOException {

        if (file != null) file.close();
        if (!fileObj.exists()) return;

        file = new RandomAccessFile(fileObj.getAbsolutePath(), "rw");
        String mimeType = FileContentTypeDetector.getFileContentType(file);

        if (mimeType == null) {
            file.close();
            return;
        }

        tagEditor = getEditor(mimeType);
        if (tagEditor == null) {
            return;
        }

        tagEditor.load(file, mimeType);
        tag              = tagEditor.getTag();
        streamInfoParser = getParser(mimeType);
        streamInfo       = streamInfoParser.parseStreamInfo(file);
    }

    @SuppressWarnings("unchecked")
    protected AbstractTagEditor<T> getEditor(String mimeType) {
        return TagEditorFactory.getEditor(mimeType);
    }

    @SuppressWarnings("unchecked")
    protected StreamInfoParser<I> getParser(String mimeType) {
        return StreamInfoParserFactory.getStreamInfoParser(mimeType, tag);
    }

    public void setTag(T tag) {
        tagEditor.setTag(tag);
    }

    public void removeTag() {
        tagEditor.removeTag();
    }

    public T getTag() {
        return tag;
    }

    public I getStreamInfo() {
        return streamInfo;
    }

    public void save() throws IOException {
        tagEditor.commit();
    }

    public void close() throws IOException {
        tagEditor.release();
    }
}
