package com.jtagger.ogg.opus;

import com.jtagger.Component;
import com.jtagger.StreamInfo;

public class OpusIdentificationHeader implements Component, StreamInfo {

    private final byte channelCount;
    private final byte channelMappingFamily;
    private byte streamCount;
    private byte coupledStreamCount;

    private final short preSkip;
    private final short outputGain;

    private final int sampleRate;

    private byte[] channelMappingTable;
    private byte[] bytes;

    private int duration;
    private int bitrate;

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
    public int getDuration() {
        return duration;
    }

    @Override
    public byte getChannelCount() {
        return channelCount;
    }

    @Override
    public int getBitrate() {
        return bitrate;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte channelMappingFamily() {
        return channelCount;
    }

    public short getPreSkip() {
        return preSkip;
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
