package com.rrtry;

import com.rrtry.mpeg.id3.ID3V2TagEditor;
import com.rrtry.flac.FlacTagEditor;
import com.rrtry.ogg.opus.OggOpusTagEditor;
import com.rrtry.ogg.vorbis.OggVorbisTagEditor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.rrtry.AbstractTagEditor.*;

public class TagEditorFactory {

    public static AbstractTagEditor getEditor(String mimeType) {
        if (mimeType.equals(MPEG_MIME_TYPE))       return new ID3V2TagEditor();
        if (mimeType.equals(FLAC_MIME_TYPE))       return new FlacTagEditor();
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return new OggVorbisTagEditor();
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return new OggOpusTagEditor();
        throw new NotImplementedException();
    }
}
