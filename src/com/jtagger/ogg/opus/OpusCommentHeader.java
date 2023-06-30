package com.jtagger.ogg.opus;

import com.jtagger.ogg.CommentHeader;
import com.jtagger.ogg.vorbis.VorbisComments;

import static com.jtagger.ogg.opus.OggOpusParser.OPUS_IDENTIFICATION_HEADER_MAGIC;

public class OpusCommentHeader implements CommentHeader {

    private VorbisComments vorbisComments;
    private byte[] bytes;

    @Override
    public byte[] assemble(byte version) {

        byte[] comments = vorbisComments.assemble();
        byte[] header   = new byte[comments.length + OPUS_IDENTIFICATION_HEADER_MAGIC.length];

        System.arraycopy(OPUS_IDENTIFICATION_HEADER_MAGIC, 0, header, 0, OPUS_IDENTIFICATION_HEADER_MAGIC.length);
        System.arraycopy(comments, 0, header, OPUS_IDENTIFICATION_HEADER_MAGIC.length, comments.length);

        this.bytes = header;
        return header;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public void setVorbisComments(VorbisComments vorbisComments) {
        this.vorbisComments = vorbisComments;
    }

    @Override
    public VorbisComments getVorbisComments() {
        return vorbisComments;
    }
}
