package com.rrtry.ogg.opus;

import com.rrtry.MediaFile;
import com.rrtry.ogg.vorbis.VorbisComments;

import static com.rrtry.AbstractTagEditor.OGG_OPUS_MIME_TYPE;

public class OggOpusFile extends MediaFile<VorbisComments> {

    public OpusIdentificationHeader getOpusIdentificationHeader() {
        OggOpusParser opusParser = (OggOpusParser) ((OggOpusTagEditor) editor).getParser();
        return opusParser.parseOpusIdentificationHeader(file);
    }

    @Override
    protected OggOpusTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(OGG_OPUS_MIME_TYPE)) {
            throw new IllegalArgumentException("Not an Ogg Opus file");
        }
        return new OggOpusTagEditor();
    }
}
