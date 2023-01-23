package com.rrtry.ogg.vorbis;

import com.rrtry.MediaFile;
import static com.rrtry.utils.FileContentTypeDetector.OGG_VORBIS_MIME_TYPE;

public class OggVorbisFile extends MediaFile<VorbisComments, VorbisIdentificationHeader> {

    @Override
    protected OggVorbisParser getParser(String mimeType) {
        if (!mimeType.equals(OGG_VORBIS_MIME_TYPE)) throw new IllegalArgumentException("Not an Ogg Vorbis file");
        return (OggVorbisParser) (((OggVorbisTagEditor) tagEditor).getParser());
    }

    @Override
    protected OggVorbisTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(OGG_VORBIS_MIME_TYPE)) throw new IllegalArgumentException("Not an Ogg Vorbis file");
        return new OggVorbisTagEditor();
    }
}
