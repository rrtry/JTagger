package com.jtagger.ogg.flac;

import com.jtagger.MediaFile;
import com.jtagger.flac.StreamInfoBlock;
import com.jtagger.ogg.vorbis.VorbisComments;

import static com.jtagger.MediaFile.FileContentTypeDetector.OGG_FLAC_MIME_TYPE;

public class OggFlacFile extends MediaFile<VorbisComments, StreamInfoBlock> {

    @Override
    protected OggFlacParser getParser(String mimeType) {
        if (!mimeType.equals(OGG_FLAC_MIME_TYPE)) throw new IllegalArgumentException("Not an Ogg Flac file");
        return (OggFlacParser) ((OggFlacTagEditor) tagEditor).getParser();
    }

    @Override
    protected OggFlacTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(OGG_FLAC_MIME_TYPE)) throw new IllegalArgumentException("Not an Ogg Flac file");
        return new OggFlacTagEditor();
    }
}
