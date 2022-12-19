package flac;

public interface BlockBodyParser<T extends AbstractMetadataBlock> {

    T parse(byte[] block);
}
