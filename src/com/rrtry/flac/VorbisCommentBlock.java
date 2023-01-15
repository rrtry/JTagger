package com.rrtry.flac;

import com.rrtry.ogg.vorbis.VorbisComments;

public class VorbisCommentBlock extends AbstractMetadataBlock {

    private VorbisComments vorbisComments = new VorbisComments(false);

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
