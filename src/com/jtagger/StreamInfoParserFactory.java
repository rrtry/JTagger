package com.jtagger;

import com.jtagger.flac.FlacParser;
import com.jtagger.flac.FlacTag;
import com.jtagger.mp3.MpegStreamInfoParser;
import com.jtagger.mp3.id3.ID3V2Tag;
import com.jtagger.mp4.MP4;
import com.jtagger.mp4.MP4Parser;
import com.jtagger.ogg.opus.OggOpusParser;
import com.jtagger.ogg.vorbis.OggVorbisParser;
import com.jtagger.ogg.vorbis.VorbisComments;

import static com.jtagger.utils.FileContentTypeDetector.*;
import static com.jtagger.utils.FileContentTypeDetector.OGG_OPUS_MIME_TYPE;

public class StreamInfoParserFactory {

    @SuppressWarnings("rawtypes")
    public static StreamInfoParser getStreamInfoParser(String mimeType, AbstractTag tag) {
        if (mimeType.equals(MPEG_MIME_TYPE))       return new MpegStreamInfoParser((ID3V2Tag) tag);
        if (mimeType.equals(FLAC_MIME_TYPE))       return new FlacParser((FlacTag) tag);
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return new OggVorbisParser((VorbisComments) tag);
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return new OggOpusParser((VorbisComments) tag);
        if (mimeType.equals(M4A_MIME_TYPE))        return new MP4Parser((MP4) tag);
        return null;
    }
}
