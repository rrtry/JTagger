package com.jtagger.mp3.id3;

import com.jtagger.Component;

import static com.jtagger.utils.IntegerUtils.fromUInt32BE;
import static com.jtagger.mp3.id3.ID3SynchSafeInteger.toSynchSafeInteger;
import static com.jtagger.mp3.id3.TagHeaderParser.*;
import static com.jtagger.mp3.id3.ID3V2Tag.*;

public class TagHeader implements Component {

    public static final int FLAG_UNSYNCH         = 0x80;
    public static final int FLAG_EXTENDED_HEADER = 0x40;
    public static final int FLAG_EXPERIMENTAL    = 0x20;
    public static final int FLAG_FOOTER_PRESENT  = 0x10;

    private byte[] headerBytes = new byte[HEADER_LENGTH];

    private int tagSize;

    private byte majorVersion = ID3V2_4;
    private byte minorVersion = 0x00;
    private byte flags        = 0x00;

    private boolean isUnsynch         = false;
    private boolean hasExtendedHeader = false;
    private boolean isExperimental    = false;
    private boolean hasFooter         = false;

    public int getTagSize() { return tagSize; }

    public byte getMajorVersion() { return majorVersion; }
    public byte getMinorVersion() { return minorVersion; }
    public byte getFlags()        { return flags; }

    public boolean isUnsynch()         { return isUnsynch; }
    public boolean hasExtendedHeader() { return hasExtendedHeader; }
    public boolean isExperimental()    { return isExperimental; }
    public boolean hasFooter()         { return hasFooter; }

    public void setTagSize(int size) {
        if (size > 10) {
            this.tagSize = size;
            return;
        }
        throw new IllegalArgumentException("Tag size is too small");
    }

    public void setFlags(byte flags) {
        if (getMajorVersion() == ID3V2_4 && (flags & 0xF) != 0) throw new IllegalArgumentException("Unknown flag was set");
        if (getMajorVersion() == ID3V2_3 && (flags & 0x1F) != 0) throw new IllegalArgumentException("Unknown flag was set");
        this.flags = flags;
    }

    public void setUnsynch(boolean isUnsynch) {
        this.isUnsynch = isUnsynch;
        if (isUnsynch) flags |= FLAG_UNSYNCH;
        else flags &= ~FLAG_UNSYNCH;
    }

    public void setHasExtendedHeader(boolean extendedHeader) {
        this.hasExtendedHeader = extendedHeader;
        if (extendedHeader) flags |= FLAG_EXTENDED_HEADER;
        else flags &= ~FLAG_EXTENDED_HEADER;
    }

    public void setExperimental(boolean isExperimental) {
        this.isExperimental = isExperimental;
        if (isExperimental) flags |= FLAG_EXPERIMENTAL;
        else flags &= ~FLAG_EXPERIMENTAL;
    }

    public void setHasFooter(boolean footer) {
        if (getMajorVersion() == ID3V2_3) return;
        this.hasFooter = footer;
        if (footer) flags |= FLAG_FOOTER_PRESENT;
        else flags &= ~FLAG_FOOTER_PRESENT;
    }

    public void setMajorVersion(byte version) {
        if (version != ID3V2_3 && version != ID3V2_4) {
            throw new IllegalArgumentException("Invalid version number: " + version);
        }
        this.majorVersion = version;
    }

    public void setMinorVersion(byte revision) {
        this.minorVersion = revision;
    }

    public static Builder newBuilder(byte majorVersion) {

        Builder builder = new TagHeader().new Builder();
        builder = builder.setMajorVersion(majorVersion);

        return builder;
    }

    public static Builder newBuilder(TagHeader tagHeader) {
        return tagHeader.new Builder();
    }

    @Override
    public byte[] assemble(byte version) {

        byte[] header = new byte[HEADER_LENGTH];
        byte[] magic  = new byte[] { 0x49, 0x44, 0x33 };
        byte[] size   = fromUInt32BE(toSynchSafeInteger(tagSize));

        System.arraycopy(magic, 0, header, 0, magic.length);
        System.arraycopy(size, 0, header, SIZE_OFFSET, size.length);

        header[MAJOR_VERSION_OFFSET] = majorVersion;
        header[REVISION_NUMBER_OFFSET] = minorVersion;
        header[FLAGS_OFFSET] = flags;

        this.headerBytes = header;
        return header;
    }

    @Override
    public byte[] getBytes() {
        return headerBytes;
    }

    @Override
    public String toString() {
        return String.format(
                "Major version: %d, revision number %d, tag size: %d, unsynch: %b, flags: %d",
                majorVersion, minorVersion, tagSize, isUnsynch, ((int) flags) & 0xFF
        );
    }

    public class Builder {

        public Builder setTagSize(int size) {
            TagHeader.this.setTagSize(size);
            return this;
        }

        public Builder setFlags(byte flags) {
            TagHeader.this.setFlags(flags);
            return this;
        }

        public Builder setMajorVersion(byte version) {
            TagHeader.this.setMajorVersion(version);
            return this;
        }

        public Builder setMinorVersion(byte version) {
            TagHeader.this.setMinorVersion(version);
            return this;
        }

        public Builder setUnsynch(boolean unsynch) {
            TagHeader.this.setUnsynch(unsynch);
            return this;
        }

        public Builder setHasExtendedHeader(boolean hasExtendedHeader) {
            TagHeader.this.setHasExtendedHeader(hasExtendedHeader);
            return this;
        }

        public Builder setIsExperimental(boolean isExperimental) {
            TagHeader.this.setExperimental(isExperimental);
            return this;
        }

        public Builder setHasFooter(boolean hasFooter) {
            TagHeader.this.setHasFooter(hasFooter);
            return this;
        }

        public TagHeader build(byte version) {
            assemble(version);
            return TagHeader.this;
        }
    }
}
