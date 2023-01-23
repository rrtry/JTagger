package com.rrtry.mpeg;

import java.util.Arrays;

public class XingHeader implements VBRHeader {

    public static final byte[] XING_VBR_MAGIC = new byte[] {
            0x58, 0x69, 0x6e, 0x67
    };

    public static final byte[] XING_CBR_MAGIC = new byte[] {
            0x49, 0x6e, 0x66, 0x6f
    };

    public static final byte FLAG_FRAMES  = 0x0001;
    public static final byte FLAG_BYTES   = 0x0002;
    public static final byte FLAG_TOC     = 0x0004;
    public static final byte FLAG_QUALITY = 0x0008;

    private byte[] magic;
    private int flags = -1;
    private int totalFrames;
    private int totalBytes;

    private byte[] tableOfContents;
    private int qualityIndicator;

    private XingHeader() {

    }

    public int getFlags() {
        return flags;
    }

    @Override
    public int getTotalFrames() {
        return totalFrames;
    }

    @Override
    public int getTotalBytes() {
        return totalBytes;
    }

    public byte[] getTableOfContents() {
        return tableOfContents;
    }

    public int getQuality() {
        return qualityIndicator;
    }

    public boolean isVBR() {
        return Arrays.equals(XING_VBR_MAGIC, magic);
    }

    public static Builder newBuilder() {
        return new XingHeader().new Builder();
    }

    public class Builder {

        public Builder setMagic(byte[] magic) {
            XingHeader.this.magic = magic;
            return this;
        }

        public Builder setFlags(int flags) {
            XingHeader.this.flags = flags;
            return this;
        }

        public Builder setTotalFrames(int totalFrames) {
            XingHeader.this.totalFrames = totalFrames;
            return this;
        }

        public Builder setTotalBytes(int totalBytes) {
            XingHeader.this.totalBytes = totalBytes;
            return this;
        }

        public Builder setTableOfContents(byte[] table) {
            XingHeader.this.tableOfContents = table;
            return this;
        }

        public Builder setQualityIndicator(int qualityIndicator) {
            XingHeader.this.qualityIndicator = qualityIndicator;
            return this;
        }

        public XingHeader build() {
            if (flags == -1) throw new IllegalArgumentException(
                    "'Flags' field is mandatory"
            );
            return XingHeader.this;
        }
    }
}
