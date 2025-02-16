package com.jtagger.flac;

import com.jtagger.Component;
import com.jtagger.utils.IntegerUtils;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractMetadataBlock implements Component {

    public static final int BLOCK_MAX_SIZE = 16777215;
    public static final byte BLOCK_HEADER_LENGTH = 0x04;
    public static final byte BLOCK_HEADER_LENGTH_INDICATOR = 0x03;

    public static final byte BLOCK_TYPE_STREAMINFO     = 0x00;
    public static final byte BLOCK_TYPE_PADDING        = 0x01;
    public static final byte BLOCK_TYPE_APPLICATION    = 0x02;
    public static final byte BLOCK_TYPE_SEEKTABLE      = 0x03;
    public static final byte BLOCK_TYPE_VORBIS_COMMENT = 0x04;
    public static final byte BLOCK_TYPE_CUESHEET       = 0x05;
    public static final byte BLOCK_TYPE_PICTURE        = 0x06;
    public static final byte BLOCK_TYPE_INVALID        = 0x7F;

    public static final List<Integer> BLOCKS = Arrays.asList(
            0x00, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01
    );

    protected byte[] blockBody;
    protected boolean isLastBlock;

    abstract public int getBlockType();

    public void setBlockLast(boolean isLast) {
        this.isLastBlock = isLast;
    }

    @Override
    public byte[] getBytes() {

        byte headerByte = 0x00;
        byte headerLength = 4;

        byte[] blockLength;
        byte[] bytes = new byte[headerLength + blockBody.length];

        if (isLastBlock) headerByte = (byte) 0x80;
        headerByte |= getBlockType();

        bytes[0] = headerByte;
        blockLength = IntegerUtils.fromUInt24BE(blockBody.length);

        System.arraycopy(blockLength, 0, bytes, 1, blockLength.length);
        System.arraycopy(blockBody, 0, bytes, headerLength, blockBody.length);
        
        return bytes;
    }
}
