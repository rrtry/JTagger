package com.rrtry.ogg.vorbis;

import com.rrtry.MediaFile;
import static com.rrtry.utils.FileContentTypeDetector.OGG_VORBIS_MIME_TYPE;

public class OggVorbisFile extends MediaFile<VorbisComments> {

    public VorbisIdentificationHeader getVorbisIdentificationHeader() {
        OggVorbisParser vorbisParser = (OggVorbisParser) ((OggVorbisTagEditor) editor).getParser();
        return vorbisParser.parseVorbisIdentificationHeader(file);
    }

    @Override
    protected OggVorbisTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(OGG_VORBIS_MIME_TYPE)) {
            throw new IllegalArgumentException("Not an Ogg Vorbis file");
        }
        return new OggVorbisTagEditor();
    }
}
