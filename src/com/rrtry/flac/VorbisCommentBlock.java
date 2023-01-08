package com.rrtry.flac;

import com.rrtry.ogg.VorbisComments;
import java.util.LinkedHashMap;

public class VorbisCommentBlock extends AbstractMetadataBlock {

    private VorbisComments vorbisComments = new VorbisComments();

    public String getVendorString() {
        return vorbisComments.getVendorString();
    }

    public String getComment(String field) {
        return vorbisComments.getComment(field);
    }

    public void setVorbisComments(VorbisComments vorbisComments) {
        this.vorbisComments = vorbisComments;
    }

    public void setVendorString(String vendorString) {
        vorbisComments.setVendorString(vendorString);
    }

    public void setCommentsMap(LinkedHashMap<String, String> commentsMap) {
        vorbisComments.setCommentsMap(commentsMap);
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
