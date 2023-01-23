package com.rrtry.mpeg;

public class VBRIHeader implements VBRHeader {

    public static final byte[] VBRI_MAGIC = new byte[] {
            'V', 'B', 'R', 'I'
    };

    private final short version;
    private final short quality;

    private final float delay;
    private final int totalBytes;
    private final int totalFrames;

    public VBRIHeader(
            short version,
            short quality,
            float delay,
            int totalBytes,
            int totalFrames)
    {
        this.version           = version;
        this.quality           = quality;
        this.delay             = delay;
        this.totalBytes        = totalBytes;
        this.totalFrames       = totalFrames;
    }

    public short getVersion() {
        return version;
    }

    public short getQuality() {
        return quality;
    }

    public float getDelay() {
        return delay;
    }

    @Override
    public int getTotalBytes() {
        return totalBytes;
    }

    @Override
    public int getTotalFrames() {
        return totalFrames;
    }
}
