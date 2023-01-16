package com.rrtry;

import com.rrtry.mpeg.id3.ID3V2TagEditor;
import com.rrtry.flac.FlacTagEditor;
import com.rrtry.ogg.opus.OggOpusTagEditor;
import com.rrtry.ogg.vorbis.OggVorbisTagEditor;
import com.rrtry.utils.FileContentTypeDetector;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TagEditorFactory {

    public static AbstractTagEditor getEditor(String mimeType) {
        if (mimeType.equals(FileContentTypeDetector.MPEG_MIME_TYPE))       return new ID3V2TagEditor();
        if (mimeType.equals(FileContentTypeDetector.FLAC_MIME_TYPE))       return new FlacTagEditor();
        if (mimeType.equals(FileContentTypeDetector.OGG_VORBIS_MIME_TYPE)) return new OggVorbisTagEditor();
        if (mimeType.equals(FileContentTypeDetector.OGG_OPUS_MIME_TYPE))   return new OggOpusTagEditor();
        throw new NotImplementedException();
    }
}
