package com.rrtry;

import com.rrtry.flac.FlacParser;
import com.rrtry.flac.FlacTag;
import com.rrtry.mpeg.MpegStreamInfoParser;
import com.rrtry.mpeg.id3.ID3V2Tag;
import com.rrtry.ogg.opus.OggOpusParser;
import com.rrtry.ogg.vorbis.OggVorbisParser;
import com.rrtry.ogg.vorbis.VorbisComments;

import static com.rrtry.utils.FileContentTypeDetector.*;
import static com.rrtry.utils.FileContentTypeDetector.OGG_OPUS_MIME_TYPE;

public class StreamInfoParserFactory {

    @SuppressWarnings("rawtypes")
    public static StreamInfoParser getStreamInfoParser(String mimeType, AbstractTag tag) {
        if (mimeType.equals(MPEG_MIME_TYPE))       return new MpegStreamInfoParser((ID3V2Tag) tag);
        if (mimeType.equals(FLAC_MIME_TYPE))       return new FlacParser((FlacTag) tag);
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return new OggVorbisParser((VorbisComments) tag);
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return new OggOpusParser((VorbisComments) tag);
        return null;
    }
}
