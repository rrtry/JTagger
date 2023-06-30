package com.jtagger.mp4;

import com.jtagger.MediaFile;

import static com.jtagger.utils.FileContentTypeDetector.MPEG_MIME_TYPE;

import java.io.File;
import java.io.IOException;

public class MP4File extends MediaFile<MP4, MP4> {

    @Override
    protected MP4Editor getEditor(String mimeType) {
        if (!mimeType.equals(MPEG_MIME_TYPE)) throw new IllegalArgumentException("Not a MP4 file");
        return new MP4Editor();
    }

    @Override
    public void scan(File fileObj) throws IOException {
        super.scan(fileObj);
        streamInfo = tag; // MP4 implements StreamInfo and AbstractTag
    }
}
