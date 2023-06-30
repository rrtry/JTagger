package com.jtagger;

public interface StreamInfo {

    byte getChannelCount();

    int getDuration();
    int getBitrate();
    int getSampleRate();

    void setDuration(int duration);
    void setBitrate(int bitrate);
}
