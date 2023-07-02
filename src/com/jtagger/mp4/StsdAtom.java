package com.jtagger.mp4;

public class StsdAtom extends MP4Atom {

    private final int channels;
    private final int sampleSize;
    private final int sampleRate;
    private final int bufferSize;
    private final int maxBitrate;
    private final int avgBitrate;
    private final int streamType;
    private final int codec;

    public StsdAtom(
            String type, byte[] data,
            int channels,
            int sampleSize,
            int sampleRate,
            int bufferSize,
            int maxBitrate,
            int avgBitrate,
            int streamType,
            int codec
    ) {
        super(type, data);
        this.channels   = channels;
        this.sampleSize = sampleSize;
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.maxBitrate = maxBitrate;
        this.avgBitrate = avgBitrate;
        this.streamType = streamType;
        this.codec      = codec;
    }

    public StsdAtom(
            String type,
            short channels,
            short sampleSize,
            int sampleRate,
            int bufferSize,
            int maxBitrate,
            int avgBitrate,
            int streamType,
            int codec
    ) {
        super(type);
        this.channels   = channels;
        this.sampleSize = sampleSize;
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.maxBitrate = maxBitrate;
        this.avgBitrate = avgBitrate;
        this.streamType = streamType;
        this.codec      = codec;
    }

    public int getChannels() {
        return channels;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getMaxBitrate() {
        return maxBitrate;
    }

    public int getAvgBitrate() {
        return avgBitrate;
    }

    public int getCodec() {
        return codec;
    }

    public int getStreamType() {
        return streamType;
    }
}
