package com.jtagger.flac;

import com.jtagger.MediaFile;

import static com.jtagger.utils.FileContentTypeDetector.FLAC_MIME_TYPE;

public class FlacFile extends MediaFile<FlacTag, StreamInfoBlock> {

    @Override
    public FlacParser getParser(String mimeType) {
        if (!mimeType.equals(FLAC_MIME_TYPE)) throw new IllegalArgumentException("Not a FLAC file");
        return ((FlacTagEditor) tagEditor).getParser();
    }

    @Override
    public FlacTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(FLAC_MIME_TYPE)) throw new IllegalArgumentException("Not a FLAC file");
        return new FlacTagEditor();
    }
}