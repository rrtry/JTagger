package com.jtagger.ogg.vorbis;

import com.jtagger.ogg.CommentHeader;

public class VorbisCommentHeader extends VorbisHeader implements CommentHeader {

    private VorbisComments vorbisComments;

    @Override
    public VorbisComments getVorbisComments() {
        return vorbisComments;
    }

    @Override
    public void setVorbisComments(VorbisComments vorbisComments) {
        this.vorbisComments = vorbisComments;
    }

    @Override
    public byte[] assemble(byte version) {

        super.assemble(version);

        byte[] comments = vorbisComments.assemble();
        byte[] common   = getBytes();
        byte[] header   = new byte[common.length + comments.length];

        System.arraycopy(common, 0, header, 0, common.length);
        System.arraycopy(comments, 0, header, common.length, comments.length);

        this.bytes = header;
        return bytes;
    }

    @Override
    byte getHeaderType() {
        return HEADER_TYPE_COMMENT;
    }
}
