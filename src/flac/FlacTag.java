package flac;

import com.rrtry.Tag;
import com.rrtry.AttachedPicture;
import com.rrtry.PaddingTag;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;

import static flac.AbstractMetadataBlock.*;

public class FlacTag extends Tag implements PaddingTag {

    public static final byte NUMBER_OF_BLOCKS = 7;
    public static final String MAGIC = "fLaC";

    private byte[] tag;
    private final ArrayList<AbstractMetadataBlock> metadataBlocks = new ArrayList<>();

    private int padding = MIN_PADDING;

    @SuppressWarnings("unchecked")
    <T extends AbstractMetadataBlock> T getBlock(int type) {
        for (AbstractMetadataBlock block : metadataBlocks) {
            if (block.getBlockType() == type) {
                return (T) block;
            }
        }
        return null;
    }

    void addBlock(AbstractMetadataBlock block) {

        if (block.getBlockType() == BLOCK_TYPE_PADDING) {
            setPaddingAmount(block.blockBody.length);
        }

        AbstractMetadataBlock metadataBlock = getBlock(block.getBlockType());
        if (metadataBlock != null) {
            metadataBlocks.set(metadataBlocks.indexOf(metadataBlock), block);
        } else {
            metadataBlocks.add(block);
        }
    }

    void removeBlock(byte type) {
        if (type == BLOCK_TYPE_STREAMINFO) throw new IllegalArgumentException("STREAMINFO block cannot be removed");
        if (type == BLOCK_TYPE_PADDING) this.padding = 0;
        metadataBlocks.removeIf((b) -> b.getBlockType() == type);
    }

    public boolean removeComment(String field) {
        VorbisCommentBlock vorbisComment = getBlock(BLOCK_TYPE_VORBIS_COMMENT);
        if (vorbisComment != null) {
            vorbisComment.removeComment(field);
            return true;
        }
        return false;
    }

    public void setComment(String field, String value) {
        VorbisCommentBlock vorbisComment = getBlock(BLOCK_TYPE_VORBIS_COMMENT);
        if (vorbisComment == null) {
            vorbisComment = new VorbisCommentBlock();
            metadataBlocks.add(vorbisComment);
        }
        vorbisComment.setComment(field, value);
    }

    private String getComment(String field) {
        VorbisCommentBlock vorbisComment = getBlock(BLOCK_TYPE_VORBIS_COMMENT);
        return vorbisComment == null ? "" : vorbisComment.getComment(field);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (AbstractMetadataBlock block : metadataBlocks) {
            sb.append(block).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int getPaddingAmount() {
        return padding;
    }

    @Override
    public void setPaddingAmount(int padding) {
        if (padding == 0) {
            removeBlock(BLOCK_TYPE_PADDING); return;
        }
        if (padding >= MIN_PADDING && padding <= MAX_PADDING) {
            this.padding = padding; return;
        }
        throw new IllegalArgumentException("Value is not within defined range");
    }

    public String getTitle() {
        return getComment(VorbisCommentBlock.TITLE);
    }

    public String getArtist() {
        return getComment(VorbisCommentBlock.ARTIST);
    }

    public String getAlbum() {
        return getComment(VorbisCommentBlock.ALBUM);
    }

    public String getYear() {
        return getComment(VorbisCommentBlock.DATE);
    }

    public void setTitle(String title) {
        setComment(VorbisCommentBlock.TITLE, title);
    }

    public void setArtist(String artist) {
        setComment(VorbisCommentBlock.ARTIST, artist);
    }

    public void setAlbum(String album) {
        setComment(VorbisCommentBlock.ALBUM, album);
    }

    public void setYear(String year) {
        setComment(VorbisCommentBlock.DATE, year);
    }

    public void setPicture(PictureBlock pictureBlock) {
        addBlock(pictureBlock);
    }

    public void setComments(VorbisCommentBlock commentBlock) {
        addBlock(commentBlock);
    }

    private void removePicture() {
        removeBlock(BLOCK_TYPE_PICTURE);
    }

    private void removeComments() {
        removeBlock(BLOCK_TYPE_VORBIS_COMMENT);
    }

    public boolean removeTitle() {
        return removeComment(VorbisCommentBlock.TITLE);
    }

    public boolean removeArtist() {
        return removeComment(VorbisCommentBlock.ARTIST);
    }

    public boolean removeAlbum() {
        return removeComment(VorbisCommentBlock.ALBUM);
    }

    public boolean removeYear() {
        return removeComment(VorbisCommentBlock.DATE);
    }

    public int getBlockDataSize() {

        int size = 0;

        for (AbstractMetadataBlock block : metadataBlocks) {
            if (metadataBlocks.indexOf(block) == metadataBlocks.size() - 1) block.isLastBlock = true;
            block.assemble();
            size += block.getBytes().length;
        }
        return size;
    }

    @Override
    public byte[] assemble(byte version) {

        // removing BLOCK_TYPE_PADDING sets padding to zero
        if (padding > 0) {

            byte[] paddingBytes = new byte[padding];
            UnknownMetadataBlock block = getBlock(BLOCK_TYPE_PADDING);

            if (block != null) {
                block.setBlockBody(paddingBytes);
            } else {
                metadataBlocks.add(new UnknownMetadataBlock(paddingBytes, 0x81));
            }
        }

        metadataBlocks.sort(Comparator.comparingInt(block -> BLOCKS.indexOf(block.getBlockType())));

        int tagSize = getBlockDataSize();
        int index   = MAGIC.length();

        byte[] tag = new byte[tagSize + MAGIC.length()];
        byte[] id  = MAGIC.getBytes(StandardCharsets.US_ASCII);

        System.arraycopy(id, 0, tag, 0, id.length);

        for (AbstractMetadataBlock block : metadataBlocks) {
            byte[] blockBytes = block.getBytes();
            System.arraycopy(blockBytes, 0, tag, index, blockBytes.length);
            index += blockBytes.length;
        }

        this.tag = tag;
        return tag;
    }

    @Override
    public byte[] getBytes() {
        return tag;
    }

    @Override
    protected <T> void setFieldValue(String fieldId, T value) {

        if (fieldId.equals(Tag.PICTURE)) {

            PictureBlock pictureBlock = getBlock(BLOCK_TYPE_PICTURE);
            if (pictureBlock == null) {
                pictureBlock = new PictureBlock();
                metadataBlocks.add(pictureBlock);
            }

            pictureBlock.setPicture((AttachedPicture) value);
            return;
        }

        VorbisCommentBlock commentBlock = getBlock(BLOCK_TYPE_VORBIS_COMMENT);
        if (commentBlock == null) {
            commentBlock = new VorbisCommentBlock();
            metadataBlocks.add(commentBlock);
        }
        commentBlock.setComment(fieldId, (String) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getFieldValue(String fieldId) {

        if (fieldId.equals(Tag.PICTURE)) {
            PictureBlock pictureBlock = getBlock(BLOCK_TYPE_PICTURE);
            return (T) pictureBlock.getPicture();
        }

        VorbisCommentBlock commentBlock = getBlock(BLOCK_TYPE_VORBIS_COMMENT);
        if (commentBlock != null) {
            return (T) commentBlock.getComment(fieldId);
        }
        return null;
    }
}
