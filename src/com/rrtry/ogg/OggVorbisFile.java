package com.rrtry.ogg;

import com.rrtry.MediaFile;

public class OggVorbisFile extends MediaFile<VorbisCommentHeader> {

    public VorbisIdentificationHeader getIdentificationHeader() {
        return ((OggVorbisTagEditor) editor).getParser().parseVorbisIdentificationHeader(file);
    }

    @Override
    protected OggVorbisTagEditor getEditor(String mimeType) {
        return new OggVorbisTagEditor();
    }
}
