package com.rrtry.ogg.opus;

import com.rrtry.ogg.CommentHeader;
import com.rrtry.ogg.vorbis.VorbisComments;

import static com.rrtry.ogg.opus.OggOpusParser.OPUS_HEADER_MAGIC;

public class OpusCommentHeader implements CommentHeader {

    private VorbisComments vorbisComments;
    private byte[] bytes;

    @Override
    public byte[] assemble(byte version) {

        byte[] comments = vorbisComments.assemble();
        byte[] header   = new byte[comments.length + OPUS_HEADER_MAGIC.length];

        System.arraycopy(OPUS_HEADER_MAGIC, 0, header, 0, OPUS_HEADER_MAGIC.length);
        System.arraycopy(comments, 0, header, OPUS_HEADER_MAGIC.length, comments.length);

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
