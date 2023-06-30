package com.jtagger.ogg.opus;

import com.jtagger.MediaFile;
import com.jtagger.ogg.vorbis.VorbisComments;

import static com.jtagger.utils.FileContentTypeDetector.OGG_OPUS_MIME_TYPE;

public class OggOpusFile extends MediaFile<VorbisComments, OpusIdentificationHeader> {

    @Override
    protected OggOpusParser getParser(String mimeType) {
        if (!mimeType.equals(OGG_OPUS_MIME_TYPE)) throw new IllegalArgumentException("Not an Ogg Opus file");
        return (OggOpusParser) ((OggOpusTagEditor) tagEditor).getParser();
    }

    @Override
    protected OggOpusTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(OGG_OPUS_MIME_TYPE)) throw new IllegalArgumentException("Not an Ogg Opus file");
        return new OggOpusTagEditor();
    }
}
