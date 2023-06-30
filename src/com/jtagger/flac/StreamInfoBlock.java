package com.jtagger.flac;

import com.jtagger.StreamInfo;

public class StreamInfoBlock extends AbstractMetadataBlock implements StreamInfo {

    private StreamInfoBlock() {
        // private constructor
    }

    private short minBlockSize;
    private short maxBlockSize;

    private int minFrameSize;
    private int maxFrameSize;
    private int sampleRate;

    private byte numOfChannels;
    private byte bitsPerSample;

    private int totalSamples;
    private int bitrate;
    private String signature;

    public short getMinBlockSize() {
        return minBlockSize;
    }

    public short getMaxBlockSize() {
        return maxBlockSize;
    }

    public int getMinFrameSize() {
        return minFrameSize;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public byte getBitsPerSample() {
        return bitsPerSample;
    }

    private int getTotalSamples() {
        return totalSamples;
    }

    @Override
    public byte getChannelCount() {
        return numOfChannels;
    }

    @Override
    public int getDuration() {
        return (int) ((float) totalSamples / sampleRate);
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
        return;
    }

    @Override
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    @Override
    public byte[] assemble(byte version) {
        return blockBody;
    }

    @Override
    public int getBlockType() {
        return AbstractMetadataBlock.BLOCK_TYPE_STREAMINFO;
    }

    @Override
    public String toString() {
        return String.format(
                "METADATA BLOCK STREAMINFO, length: " + blockBody.length + "\n" +
                "minBlockSize: %d, maxBlockSize: %d, minFrameSize: %d, maxFrameSize: %d, sampleRate: %d, channels %d, bitsPerSample: %d, totalSamples: %d, md5: %s",
                minBlockSize, maxBlockSize, minFrameSize, maxFrameSize, sampleRate, numOfChannels, bitsPerSample, totalSamples, signature
        );
    }

    public static Builder createBuilder() {
        return new StreamInfoBlock().new Builder();
    }

    public class Builder {

        public StreamInfoBlock build(byte[] parsedData) {
            blockBody = parsedData;
            return StreamInfoBlock.this;
        }

        public Builder setMinBlockSize(short min) {
            minBlockSize = min;
            return this;
        }

        public Builder setMaxBlockSize(short max) {
            maxBlockSize = max;
            return this;
        }

        public Builder setMinFrameSize(int min) {
            minFrameSize = min;
            return this;
        }

        public Builder setMaxFrameSize(int max) {
            maxFrameSize = max;
            return this;
        }

        public Builder setSampleRate(int rate) {
            sampleRate = rate;
            return this;
        }

        public Builder setNumOfChannels(byte channels) {
            numOfChannels = channels;
            return this;
        }

        public Builder setBitsPerSample(byte bits) {
            bitsPerSample = bits;
            return this;
        }

        public Builder setTotalSamples(int samples) {
            totalSamples = samples;
            return this;
        }

        public Builder setSignature(String md5) {
            signature = md5;
            return this;
        }
    }
}
