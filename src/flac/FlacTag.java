package flac;

import com.rrtry.Component;
import com.rrtry.Tag;
import java.util.ArrayList;
import java.util.Comparator;

public class FlacTag implements Tag, Component {

    private byte[] tag;
    private final ArrayList<AbstractMetadataBlock> metadataBlocks = new ArrayList<>();

    public <T extends AbstractMetadataBlock> T getBlock(int type) {
        for (AbstractMetadataBlock block : metadataBlocks) {
            if (block.getBlockType() == type) {
                return (T) block;
            }
        }
        return null;
    }

    void addBlock(AbstractMetadataBlock block) {
        metadataBlocks.add(block);
    }

    public void removeBlock(byte type) {
        if (type == AbstractMetadataBlock.BLOCK_TYPE_STREAMINFO) {
            throw new IllegalArgumentException("STREAMINFO block cannot be removed");
        }
        metadataBlocks.removeIf((b) -> b.getBlockType() == type);
    }

    private boolean removeComment(String field) {
        VorbisCommentBlock vorbisComment = getVorbisCommentBlock();
        if (vorbisComment != null) {
            vorbisComment.removeComment(field);
            return true;
        }
        return false;
    }

    private void setComment(String field, String value) {
        VorbisCommentBlock vorbisComment = getVorbisCommentBlock();
        if (vorbisComment != null) vorbisComment.setComment(field, value);
    }

    private String getComment(String field) {
        VorbisCommentBlock vorbisComment = getVorbisCommentBlock();
        return vorbisComment == null ? "" : vorbisComment.getComment(field);
    }

    private VorbisCommentBlock getVorbisCommentBlock() {
        for (AbstractMetadataBlock block : metadataBlocks) {
            if (block instanceof VorbisCommentBlock) {
                return (VorbisCommentBlock) block;
            }
        }
        return null;
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
    public String getTitle() {
        return getComment(VorbisCommentBlock.TITLE);
    }

    @Override
    public String getArtist() {
        return getComment(VorbisCommentBlock.ARTIST);
    }

    @Override
    public String getAlbum() {
        return getComment(VorbisCommentBlock.ALBUM);
    }

    @Override
    public String getYear() {
        return getComment(VorbisCommentBlock.DATE);
    }

    @Override
    public void setTitle(String title) {
        setComment(VorbisCommentBlock.TITLE, title);
    }

    @Override
    public void setArtist(String artist) {
        setComment(VorbisCommentBlock.ARTIST, artist);
    }

    @Override
    public void setAlbum(String album) {
        setComment(VorbisCommentBlock.ALBUM, album);
    }

    @Override
    public void setYear(String year) {
        setComment(VorbisCommentBlock.DATE, year);
    }

    @Override
    public boolean removeTitle() {
        return removeComment(VorbisCommentBlock.TITLE);
    }

    @Override
    public boolean removeArtist() {
        return removeComment(VorbisCommentBlock.ARTIST);
    }

    @Override
    public boolean removeAlbum() {
        return removeComment(VorbisCommentBlock.ALBUM);
    }

    @Override
    public boolean removeYear() {
        return removeComment(VorbisCommentBlock.DATE);
    }

    public int getSize() {

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

        metadataBlocks.sort(Comparator.comparingInt(AbstractMetadataBlock::getBlockType));

        int tagSize = getSize();
        int index   = 0;

        byte[] tag = new byte[tagSize];

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
}
