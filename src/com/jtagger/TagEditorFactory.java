package com.jtagger;

import com.jtagger.mp3.id3.ID3V2TagEditor;
import com.jtagger.flac.FlacTagEditor;
import com.jtagger.mp4.MP4Editor;
import com.jtagger.ogg.opus.OggOpusTagEditor;
import com.jtagger.ogg.vorbis.OggVorbisTagEditor;

import static com.jtagger.utils.FileContentTypeDetector.*;

public class TagEditorFactory {

    public static AbstractTagEditor getEditor(String mimeType) {
        if (mimeType.equals(MPEG_MIME_TYPE))       return new ID3V2TagEditor();
        if (mimeType.equals(FLAC_MIME_TYPE))       return new FlacTagEditor();
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return new OggVorbisTagEditor();
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return new OggOpusTagEditor();
        if (mimeType.equals(M4A_MIME_TYPE))        return new MP4Editor();
        return null;
    }
}
