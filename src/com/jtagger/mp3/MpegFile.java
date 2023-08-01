package com.jtagger.mp3;

import com.jtagger.MediaFile;
import com.jtagger.mp3.id3.ID3V2Tag;
import com.jtagger.mp3.id3.ID3V2TagEditor;

import static com.jtagger.MediaFile.FileContentTypeDetector.MPEG_MIME_TYPE;

public class MpegFile extends MediaFile<ID3V2Tag, MpegStreamInfo> {

    @Override
    protected MpegStreamInfoParser getParser(String mimeType) {
        if (!mimeType.equals(MPEG_MIME_TYPE)) throw new IllegalArgumentException("Not a MP3 file");
        return new MpegStreamInfoParser(tagEditor.getTag());
    }

    @Override
    protected ID3V2TagEditor getEditor(String mimeType) {
        if (!mimeType.equals(MPEG_MIME_TYPE)) throw new IllegalArgumentException("Not a MPEG file");
        return new ID3V2TagEditor();
    }
}
