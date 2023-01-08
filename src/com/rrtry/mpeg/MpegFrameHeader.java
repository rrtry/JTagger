package com.rrtry.mpeg;

public class MpegFrameHeader {

    public static final byte MPEG_VERSION_2_5 = 0x00;
    public static final byte MPEG_VERSION_2   = 0x02;
    public static final byte MPEG_VERSION_1   = 0x03;

    public static final byte MPEG_LAYER_3 = 0x01;
    public static final byte MPEG_LAYER_2 = 0x02;
    public static final byte MPEG_LAYER_1 = 0x03;

    public static final byte CHANNEL_MODE_STEREO         = 0x00;
    public static final byte CHANNEL_MODE_JOIN_STEREO    = 0x01;
    public static final byte CHANNEL_MODE_DUAL_CHANNEL   = 0x02;
    public static final byte CHANNEL_MODE_SINGLE_CHANNEL = 0x03;

    public static final byte EMPHASIS_NONE     = 0x00;
    public static final byte EMPHASIS_50_15_MS = 0x01;
    public static final byte EMPHASIS_CCIT_J17 = 0x03;

    public static final byte BITRATE_VBR = 0x00;
    public static final byte BITRATE_BAD  = 0x01;

    private final byte version;
    private final byte layer;
    private final byte channelMode;
    private final byte emphasis;

    private final boolean isProtected;
    private final boolean isCopyrighted;
    private final boolean isOriginal;
    private final boolean isPadded;
    private final boolean isIntensityStereo;
    private final boolean isMidSideStereo;

    private final int bitrate;
    private final int sampleRate;

    public MpegFrameHeader(

            byte version,
            byte layer,
            byte channelMode,
            byte emphasis,

            boolean isProtected,
            boolean isCopyrighted,
            boolean isOriginal,
            boolean isPadded,
            boolean isIntensityStereo,
            boolean isMidSideStereo,

            int bitrate,
            int sampleRate)
    {
        this.version           = version;
        this.layer             = layer;
        this.channelMode       = channelMode;
        this.emphasis          = emphasis;
        this.isProtected       = isProtected;
        this.isCopyrighted     = isCopyrighted;
        this.isOriginal        = isOriginal;
        this.isPadded          = isPadded;
        this.isIntensityStereo = isIntensityStereo;
        this.isMidSideStereo   = isMidSideStereo;
        this.bitrate           = bitrate;
        this.sampleRate        = sampleRate;
    }

    public byte getVersion() {
        return version;
    }

    public byte getLayer() {
        return layer;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public boolean isPadded() {
        return isPadded;
    }

    public byte getChannelMode() {
        return channelMode;
    }

    private boolean isIntensityStereo() {
        return isIntensityStereo;
    }

    private boolean isMidSideStereo() {
        return isMidSideStereo;
    }

    private boolean isCopyrighted() {
        return isCopyrighted;
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public byte getEmphasis() {
        return emphasis;
    }
}
