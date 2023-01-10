package com.rrtry.ogg.opus;

import com.rrtry.Component;

public class OpusIdentificationHeader implements Component {

    private final byte channelCount;
    private final byte channelMappingFamily;
    private byte streamCount;
    private byte coupledStreamCount;

    private final short preSkip;
    private final short outputGain;

    private final int sampleRate;

    private byte[] channelMappingTable;
    private byte[] bytes;

    public OpusIdentificationHeader(
            byte channelCount,
            byte channelMappingFamily,

            short preSkip,
            short outputGain,

            int sampleRate,
            byte[] header)
    {
        this.channelCount         = channelCount;
        this.channelMappingFamily = channelMappingFamily;
        this.preSkip              = preSkip;
        this.outputGain           = outputGain;
        this.sampleRate           = sampleRate;
        this.bytes                = header;
    }

    public OpusIdentificationHeader(
            byte channelCount,
            byte channelMappingFamily,
            byte streamCount,
            byte coupledStreamCount,

            short preSkip,
            short outputGain,

            int sampleRate,
            byte[] channelMappingTable,
            byte[] header)
    {
        this.channelCount         = channelCount;
        this.channelMappingFamily = channelMappingFamily;
        this.streamCount          = streamCount;
        this.coupledStreamCount   = coupledStreamCount;
        this.preSkip              = preSkip;
        this.outputGain           = outputGain;
        this.sampleRate           = sampleRate;
        this.channelMappingTable  = channelMappingTable;
        this.bytes                = header;
    }

    @Override
    public byte[] assemble(byte version) {
        return bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte getChannelCount() {
        return channelCount;
    }

    public byte channelMappingFamily() {
        return channelCount;
    }

    public short getPreSkip() {
        return preSkip;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    private byte[] getChannelMappingTable() {
        return channelMappingTable;
    }

    public byte getChannelMappingFamily() {
        return channelMappingFamily;
    }

    public byte getStreamCount() {
        return streamCount;
    }

    public byte getCoupledStreamCount() {
        return coupledStreamCount;
    }

    public short getOutputGain() {
        return outputGain;
    }
}
