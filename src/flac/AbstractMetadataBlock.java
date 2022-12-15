package flac;

import com.rrtry.Component;
import utils.IntegerUtils;

public abstract class AbstractMetadataBlock implements Component {

    public static final byte BLOCK_HEADER_LENGTH_INDICATOR = 0x03;

    public static final byte BLOCK_TYPE_STREAMINFO     = 0x00;
    public static final byte BLOCK_TYPE_PADDING        = 0x01;
    public static final byte BLOCK_TYPE_APPLICATION    = 0x02;
    public static final byte BLOCK_TYPE_SEEKTABLE      = 0x03;
    public static final byte BLOCK_TYPE_VORBIS_COMMENT = 0x04;
    public static final byte BLOCK_TYPE_CUESHEET       = 0x05;
    public static final byte BLOCK_TYPE_PICTURE        = 0x06;
    public static final byte BLOCK_TYPE_INVALID        = 0x7F;

    protected byte[] blockBody;
    protected boolean isLastBlock;

    abstract int getBlockType();

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
