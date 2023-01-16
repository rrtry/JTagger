package com.rrtry.mpeg;

import com.rrtry.MediaFile;
import com.rrtry.mpeg.id3.ID3V2Tag;
import com.rrtry.mpeg.id3.ID3V2TagEditor;

import java.io.File;
import static com.rrtry.utils.FileContentTypeDetector.MPEG_MIME_TYPE;

public class MpegFile extends MediaFile<ID3V2Tag> {

    private MpegFrameHeader mpegHeader;

    @Override
    public ID3V2TagEditor getEditor(String mimeType) {
        if (!mimeType.equals(MPEG_MIME_TYPE)) {
            throw new IllegalArgumentException("Not a MPEG file");
        }
        return new ID3V2TagEditor();
    }

    @Override
    public void scan(File file) {
        super.scan(file);
        parseMpegFrameHeader();
    }

    private void parseMpegFrameHeader() {
        MpegFrameHeaderParser parser = new MpegFrameHeaderParser();
        mpegHeader = parser.parse(file);
    }

    public MpegFrameHeader getMpegHeader() {
        return mpegHeader;
    }
}
