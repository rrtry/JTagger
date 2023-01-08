package com.rrtry.ogg;

import com.rrtry.AttachedPicture;
import com.rrtry.flac.PictureBlock;
import com.rrtry.flac.PictureBlockParser;

import java.util.Base64;

public class VorbisCommentHeader extends VorbisHeader {

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

    public void setComment(String field, String value) {
        vorbisComments.setComment(field, value);
    }

    public void removeComment(String field) {
        vorbisComments.removeComment(field);
    }

    @Override
    public byte[] assemble(byte version) {

        super.assemble(version);

        byte[] comments = vorbisComments.assemble();
        byte[] common   = getBytes();
        byte[] header   = new byte[common.length + comments.length + 1];

        System.arraycopy(common, 0, header, 0, common.length);
        System.arraycopy(comments, 0, header, common.length, comments.length);
        header[header.length - 1] = 0x1;

        this.bytes = header;
        return bytes;
    }

    @Override
    byte getHeaderType() {
        return HEADER_TYPE_COMMENT;
    }

    @Override
    protected <T> void setFieldValue(String fieldId, T value) {
        if (fieldId.equals(PICTURE)) {

            final String commentField = "METADATA_BLOCK_PICTURE";

            PictureBlock pictureBlock = new PictureBlock();
            pictureBlock.setPicture((AttachedPicture) value);

            vorbisComments.setComment(
                    commentField,
                    Base64.getEncoder().encodeToString(pictureBlock.assemble())
            );
            return;
        }
        vorbisComments.setComment(fieldId, (String) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getFieldValue(String fieldId) {
        if (fieldId.equals(PICTURE)) {

            final String commentField = "METADATA_BLOCK_PICTURE";

            String value = vorbisComments.getComment(commentField);
            if (value == null) return null;

            PictureBlockParser parser = new PictureBlockParser();
            PictureBlock pictureBlock = parser.parse(Base64.getDecoder().decode(value));

            return (T) pictureBlock.getPicture();
        }
        return (T) vorbisComments.getComment(fieldId);
    }
}
