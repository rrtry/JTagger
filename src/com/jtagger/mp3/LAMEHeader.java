package com.jtagger.mp3;

import java.util.Arrays;

public class LAMEHeader {

    private final String version;
    private final int tagVersion;
    private final int vbrMethod;
    private final int lowpassFilter;
    private final long replayGain;
    private final int encodingFlags;
    private final int athType;
    private final int bitrate;
    private final int[] encoderDelays;
    private final int misc;
    private final int gain;
    private final int surroundInfo;
    private final long lengthBytes;
    private final int musicCRC;
    private final int tagCRC;

    public LAMEHeader(String version,
                      int tagVersion,
                      int vbrMethod,
                      int lowpassFilter,
                      long replayGain,
                      int encodingFlags,
                      int athType,
                      int bitrate,
                      int[] encoderDelays,
                      int misc,
                      int gain,
                      int surroundInfo,
                      long lengthBytes,
                      int musicCRC,
                      int tagCRC)
    {
        this.version = version;
        this.tagVersion = tagVersion;
        this.vbrMethod = vbrMethod;
        this.lowpassFilter = lowpassFilter;
        this.replayGain = replayGain;
        this.encodingFlags = encodingFlags;
        this.athType = athType;
        this.bitrate = bitrate;
        this.encoderDelays = encoderDelays;
        this.misc = misc;
        this.gain = gain;
        this.surroundInfo = surroundInfo;
        this.lengthBytes = lengthBytes;
        this.musicCRC = musicCRC;
        this.tagCRC = tagCRC;
    }

    public String getVersion() {
        return version;
    }

    public int getTagVersion() {
        return tagVersion;
    }

    public int getVbrMethod() {
        return vbrMethod;
    }

    public int getLowpassFilter() {
        return lowpassFilter;
    }

    public long getReplayGain() {
        return replayGain;
    }

    public int getEncodingFlags() {
        return encodingFlags;
    }

    public int getAthType() {
        return athType;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int[] getEncoderDelays() {
        return encoderDelays;
    }

    public int getMisc() {
        return misc;
    }

    public int getGain() {
        return gain;
    }

    public int getSurroundInfo() {
        return surroundInfo;
    }

    public long getLengthBytes() {
        return lengthBytes;
    }

    public int getMusicCRC() {
        return musicCRC;
    }

    public int getTagCRC() {
        return tagCRC;
    }

    @Override
    public String toString() {
        return "LAMEHeader{" +
                "version='" + version + '\'' +
                ", tagVersion=" + tagVersion +
                ", vbrMethod=" + vbrMethod +
                ", lowpassFilter=" + lowpassFilter +
                ", replayGain=" + replayGain +
                ", encodingFlags=" + encodingFlags +
                ", athType=" + athType +
                ", bitrate=" + bitrate +
                ", encoderDelays=" + Arrays.toString(encoderDelays) +
                ", misc=" + misc +
                ", gain=" + gain +
                ", surroundInfo=" + surroundInfo +
                ", lengthBytes=" + lengthBytes +
                ", musicCRC=" + musicCRC +
                ", tagCRC=" + tagCRC +
                '}';
    }
}
