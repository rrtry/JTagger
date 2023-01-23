package com.rrtry.ogg.vorbis;

import com.rrtry.StreamInfo;
import com.rrtry.utils.IntegerUtils;

public class VorbisIdentificationHeader extends VorbisHeader implements StreamInfo {

    private final int version;
    private final byte channels;
    private final int sampleRate;
    private final int maxBitrate;
    private final int nominalBitrate;
    private final int minBitrate;
    private final byte blockSize;

    private int duration;

    public VorbisIdentificationHeader(
            int version,
            byte channels,
            int sampleRate,
            int maxBitrate,
            int nominalBitrate,
            int minBitrate,
            byte blockSize,
            byte[] bytes)
    {
        this.version        = version;
        this.channels       = channels;
        this.sampleRate     = sampleRate;
        this.maxBitrate     = maxBitrate;
        this.nominalBitrate = nominalBitrate;
        this.minBitrate     = minBitrate;
        this.blockSize      = blockSize;
        this.bytes          = bytes;
    }

    public int getVersion() {
        return version;
    }

    public byte getChannels() {
        return channels;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public byte getChannelCount() {
        return getChannels();
    }

    @Override
    public int getBitrate() {
        return getNominalBitrate() / 1000;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void setBitrate(int bitrate) {

    }

    public int getMaxBitrate() {
        return maxBitrate;
    }

    public int getNominalBitrate() {
        return nominalBitrate;
    }

    public int getMinBitrate() {
        return minBitrate;
    }

    @Override
    public String toString() {
        return String.format(
                "---VORBIS IDENTIFICATION HEADER---\n" +
                        "\tVersion: %d\n\tSample rate: %d\n\tMax bitrate: %d\n\tNominal bitrate: %d\n\tMin bitrate: %d\n\tChannels: %d",
                version, sampleRate, maxBitrate, nominalBitrate, minBitrate, channels
        );
    }

    @Override
    public byte[] assemble(byte version) {

        super.assemble(version);

        byte[] common = getBytes();
        byte[] header = new byte[common.length + 22];

        int offset = 0;

        System.arraycopy(common, 0, header, offset, common.length); offset += common.length;
        System.arraycopy(IntegerUtils.fromUInt32LE(version), 0, header, offset, 4); offset += 4;

        header[offset++] = channels;

        System.arraycopy(IntegerUtils.fromUInt32LE(sampleRate), 0, header, offset, 4);     offset += 4;
        System.arraycopy(IntegerUtils.fromUInt32LE(maxBitrate), 0, header, offset, 4);     offset += 4;
        System.arraycopy(IntegerUtils.fromUInt32LE(nominalBitrate), 0, header, offset, 4); offset += 4;
        System.arraycopy(IntegerUtils.fromUInt32LE(minBitrate), 0, header, offset, 4);     offset += 4;

        header[offset++] = blockSize;
        header[offset]   = 0x1;

        this.bytes = header;
        return bytes;
    }

    @Override
    byte getHeaderType() {
        return HEADER_TYPE_IDENTIFICATION;
    }
}
