package com.jtagger.mp4;

public class StsdAtom extends MP4Atom {

    private int esStreamType = -1;
    private int esObjectType = -1;

    private int channels;
    private int sampleSize;
    private int sampleRate;
    private int bufferSize;
    private int maxBitrate;
    private int avgBitrate;

    public StsdAtom(String type, byte[] data) {
        super(type, data);
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

    public int getEsStreamType() {
        return esStreamType;
    }

    public int getEsObjectType() {
        return esObjectType;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setMaxBitrate(int maxBitrate) {
        this.maxBitrate = maxBitrate;
    }

    public void setAvgBitrate(int avgBitrate) {
        this.avgBitrate = avgBitrate;
    }

    public void setEsStreamType(int esStreamType) {
        this.esStreamType = esStreamType;
    }

    public void setEsObjectType(int esObjectType) {
        this.esObjectType = esObjectType;
    }
}
