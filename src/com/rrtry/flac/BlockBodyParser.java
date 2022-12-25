package com.rrtry.flac;

public interface BlockBodyParser<T extends AbstractMetadataBlock> {

    T parse(byte[] block);
}
