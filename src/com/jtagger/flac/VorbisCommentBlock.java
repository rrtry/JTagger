package com.jtagger.flac;

import com.jtagger.ogg.vorbis.VorbisComments;

public class VorbisCommentBlock extends AbstractMetadataBlock {

    private VorbisComments vorbisComments = new VorbisComments(false);

    public VorbisComments getVorbisComments() {
        return vorbisComments;
    }

    public String getComment(String field) {
        return vorbisComments.getComment(field);
    }

    public void setVorbisComments(VorbisComments vorbisComments) {
        this.vorbisComments = vorbisComments;
    }

    public void setComment(String field, String value) {
        vorbisComments.setComment(field, value);
    }

    public void removeComment(String field) {
        vorbisComments.removeComment(field);
    }

    @Override
    public int getBlockType() {
        return BLOCK_TYPE_VORBIS_COMMENT;
    }

    @Override
    public byte[] assemble(byte version) {
        this.blockBody = vorbisComments.assemble();
        return blockBody;
    }
}
