package flac;

public interface MetadataBlockBodyParser<T extends AbstractMetadataBlock> {

    T parse(byte[] block);
}
