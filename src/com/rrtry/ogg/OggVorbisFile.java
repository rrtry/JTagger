package com.rrtry.ogg;

import com.rrtry.AbstractTagEditor;
import com.rrtry.MediaFile;

public class OggVorbisFile extends MediaFile<VorbisCommentHeader> {

    public VorbisIdentificationHeader getIdentificationHeader() {
        return ((OggVorbisTagEditor) editor).getParser().parseVorbisIdentificationHeader(file);
    }

    @Override
    protected OggVorbisTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(AbstractTagEditor.OGG_MIME_TYPE)) {
            throw new IllegalArgumentException("Not an Ogg Vorbis file");
        }
        return new OggVorbisTagEditor();
    }
}
