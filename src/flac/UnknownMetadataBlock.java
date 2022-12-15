package flac;

public class UnknownMetadataBlock extends AbstractMetadataBlock {

    private final int headerByte;

    public UnknownMetadataBlock(byte[] block, int headerByte) {
        this.blockBody = block;
        this.headerByte = headerByte;
    }

    @Override
    public String toString() {
        return String.format("Unknown metadata block, type %d", getBlockType());
    }

    @Override
    public byte[] assemble(byte version) {
        return blockBody;
    }

    @Override
    public int getBlockType() {
        return (headerByte & 0x7F);
    }
}
