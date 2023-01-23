package com.rrtry.mpeg;

import com.rrtry.MediaFile;
import com.rrtry.mpeg.id3.ID3V2Tag;
import com.rrtry.mpeg.id3.ID3V2TagEditor;

import static com.rrtry.utils.FileContentTypeDetector.MPEG_MIME_TYPE;

public class MpegFile extends MediaFile<ID3V2Tag, MpegStreamInfo> {

    @Override
    protected MpegStreamInfoParser getParser(String mimeType) {
        if (!mimeType.equals(MPEG_MIME_TYPE)) throw new IllegalArgumentException("Not a MP3 file");
        return new MpegStreamInfoParser(tag);
    }

    @Override
    protected ID3V2TagEditor getEditor(String mimeType) {
        if (!mimeType.equals(MPEG_MIME_TYPE)) throw new IllegalArgumentException("Not a MPEG file");
        return new ID3V2TagEditor();
    }
}
