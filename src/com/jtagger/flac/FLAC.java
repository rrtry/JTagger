package com.jtagger.flac;

import com.jtagger.AbstractTag;
import com.jtagger.AttachedPicture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import static com.jtagger.flac.AbstractMetadataBlock.*;

public class FLAC extends AbstractTag {

    public static final String MAGIC = "fLaC";

    private byte[] bytes;
    private final ArrayList<AbstractMetadataBlock> metadataBlocks = new ArrayList<>();

    private VorbisCommentBlock vorbisComment;
    private PictureBlock pictureBlock;

    ArrayList<AbstractMetadataBlock> getBlocks() {
        return metadataBlocks;
    }

    @SuppressWarnings("unchecked")
    <T extends AbstractMetadataBlock> T getBlock(int type) {
        for (AbstractMetadataBlock block : metadataBlocks) {
            if (block.getBlockType() == type) {
                return (T) block;
            }
        }
        return null;
    }

    private boolean getVorbisCommentBlock() {
        if (vorbisComment == null) {
            vorbisComment = getBlock(BLOCK_TYPE_VORBIS_COMMENT);
            return vorbisComment != null;
        }
        return true;
    }

    private boolean getPictureBlock() {
        if (pictureBlock == null) {
            pictureBlock = getBlock(BLOCK_TYPE_PICTURE);
            return pictureBlock != null;
        }
        return true;
    }

    private void setVorbisCommentBlock() {
        if (!getVorbisCommentBlock()) {
            vorbisComment = new VorbisCommentBlock();
            metadataBlocks.add(vorbisComment);
        }
    }

    private void setPictureBlock() {
        if (!getPictureBlock()) {
            pictureBlock = new PictureBlock();
            metadataBlocks.add(pictureBlock);
        }
    }

    void addBlock(AbstractMetadataBlock block) {
        AbstractMetadataBlock metadataBlock = getBlock(block.getBlockType());
        if (metadataBlock != null) {
            metadataBlocks.set(metadataBlocks.indexOf(metadataBlock), block);
        } else {
            metadataBlocks.add(block);
        }
    }

    void removeBlock(byte type) {
        AbstractMetadataBlock toRemove = null;
        for (AbstractMetadataBlock block : metadataBlocks) {
            if (block.getBlockType() == type) {
                toRemove = block;
                break;
            }
        }
        if (toRemove != null) metadataBlocks.remove(toRemove);
    }

    public LinkedHashMap<String, String> getCommentsMap() {
        return getVorbisCommentBlock() ? vorbisComment.getVorbisComments().getCommentsMap() : null;
    }

    public String getComment(String field) {
        return getVorbisCommentBlock() ? vorbisComment.getComment(field) : "";
    }

    public AttachedPicture getPicture() {
        return getPictureBlock() ? pictureBlock.getPicture() : null;
    }

    public void setComment(String field, String value) {
        setVorbisCommentBlock();
        vorbisComment.setComment(field, value);
    }

    public void setPicture(AttachedPicture picture) {
        setPictureBlock();
        pictureBlock.setPicture(picture);
    }

    public void removeComment(String field) {
        if (getVorbisCommentBlock()) {
            vorbisComment.removeComment(field);
        }
    }

    public void removePicture() {
        removeBlock(BLOCK_TYPE_PICTURE);
    }

    int getBlockDataSize() {
        int size = 0;
        for (AbstractMetadataBlock block : metadataBlocks) {
            block.isLastBlock = false; // last block would be BLOCK_TYPE_PADDING
            block.assemble();
            size += block.getBytes().length;
        }
        return size;
    }

    private int copyBlock(AbstractMetadataBlock block, int dstOffset) {
        byte[] blockBuffer = block.getBytes();
        System.arraycopy(blockBuffer, 0, bytes, dstOffset, blockBuffer.length);
        return blockBuffer.length;
    }

    @Override
    public byte[] assemble(byte version) {

        int offset = 0;
        int size   = getBlockDataSize();

        bytes = new byte[size];
        AbstractMetadataBlock[] blocks = new AbstractMetadataBlock[] {
                getBlock(BLOCK_TYPE_STREAMINFO),
                getBlock(BLOCK_TYPE_VORBIS_COMMENT),
                getBlock(BLOCK_TYPE_PICTURE)
        };

        for (AbstractMetadataBlock block : blocks) {
            if (block != null) {
                offset += copyBlock(block, offset);
            }
        }
        for (AbstractMetadataBlock block : metadataBlocks) {
            if (block instanceof UnknownMetadataBlock) {
                offset += copyBlock(block, offset);
            }
        }
        return bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    protected <T> void setFieldValue(String fieldId, T value) {
        if (fieldId.equals(AbstractTag.PICTURE)) {
            setPicture((AttachedPicture) value);
            return;
        }
        setComment(fieldId, (String) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getFieldValue(String fieldId) {
        if (fieldId.equals(AbstractTag.PICTURE)) {
            return (T) getPicture();
        }
        return (T) getComment(fieldId);
    }

    @Override
    public void removeField(String fieldId) {
        if (fieldId.equals(PICTURE)) {
            removePicture();
            return;
        }
        removeComment(fieldId);
    }
}
