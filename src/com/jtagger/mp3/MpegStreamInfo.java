package com.jtagger.mp3;
import com.jtagger.StreamInfo;

import static com.jtagger.mp3.MpegFrameHeader.CHANNEL_MODE_SINGLE_CHANNEL;

public class MpegStreamInfo implements StreamInfo {

    private MpegFrameHeader mpegHeader;
    private VBRIHeader vbriHeader;
    private XingHeader xingHeader;

    private int duration;

    private MpegStreamInfo() {
        /* empty constructor */
    }

    public MpegFrameHeader getMpegHeader() {
        return mpegHeader;
    }

    public VBRIHeader getVbriHeader() {
        return vbriHeader;
    }

    public XingHeader getXingHeader() {
        return xingHeader;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void setBitrate(int bitrate) {

    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public byte getChannelCount() {
        byte channelMode = mpegHeader.getChannelMode();
        if (channelMode == CHANNEL_MODE_SINGLE_CHANNEL) return 1;
        return 2;
    }

    @Override
    public int getBitrate() {
        if (isVBR()) {

            VBRHeader header = getVBRHeader();

            final int totalBytes        = header.getTotalBytes();
            final int totalFrames       = header.getTotalFrames();

            final int samplesPerFrame   = mpegHeader.getSamplesPerFrame();
            final int sampleRate        = mpegHeader.getSampleRate();
            final float singleFrameTime = (float) samplesPerFrame / sampleRate * 1000f;

            return (int) ((totalBytes * 8) / (singleFrameTime * totalFrames));
        }
        return mpegHeader.getBitrate();
    }

    @Override
    public int getSampleRate() {
        return mpegHeader.getSampleRate();
    }

    public boolean isVBR() {
        return (xingHeader != null && xingHeader.isVBR()) || vbriHeader != null;
    }

    private VBRHeader getVBRHeader() {
        return xingHeader != null ? xingHeader : vbriHeader;
    }

    public static Builder newBuilder() {
        return new MpegStreamInfo().new Builder();
    }

    public class Builder {

        public Builder setMpegHeader(MpegFrameHeader mpegHeader) {
            MpegStreamInfo.this.mpegHeader = mpegHeader;
            return this;
        }

        public Builder setXingHeader(XingHeader xingHeader) {
            MpegStreamInfo.this.xingHeader = xingHeader;
            return this;
        }

        public Builder setVBRIHeader(VBRIHeader vbriHeader) {
            MpegStreamInfo.this.vbriHeader = vbriHeader;
            return this;
        }

        public MpegStreamInfo build() {
            return MpegStreamInfo.this;
        }
    }
}
