package com.rrtry.id3;

import com.rrtry.AbstractTagEditor;

abstract class AbstractID3TagEditor <T extends ID3Tag> extends AbstractTagEditor<T> {

    private static final String MPEG_MIME_TYPE = "audio/mpeg";

    @Override
    protected String getFileMimeType() {
        return MPEG_MIME_TYPE;
    }
}
