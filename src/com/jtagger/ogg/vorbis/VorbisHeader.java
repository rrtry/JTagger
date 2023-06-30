package com.jtagger.ogg.vorbis;

import com.jtagger.Component;

abstract public class VorbisHeader implements Component {

    public static final byte[] VORBIS_HEADER_MAGIC = new byte[] { 0x76, 0x6f, 0x72, 0x62, 0x69, 0x73 };

    public static final byte HEADER_TYPE_IDENTIFICATION = 0x1;
    public static final byte HEADER_TYPE_COMMENT        = 0x3;
    public static final byte HEADER_TYPE_SETUP          = 0x5;

    protected byte[] bytes;
    abstract byte getHeaderType();

    @Override
    public byte[] assemble(byte version) {

        bytes    = new byte[1 + VORBIS_HEADER_MAGIC.length];
        bytes[0] = getHeaderType();

        System.arraycopy(VORBIS_HEADER_MAGIC, 0, bytes, 1, VORBIS_HEADER_MAGIC.length);
        return bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
