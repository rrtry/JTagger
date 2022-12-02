package com.rrtry;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import static com.rrtry.ID3SynchSafeInteger.fromSynchSafeIntegerBytes;
import static com.rrtry.ID3V2Tag.ID3V2_3;
import static com.rrtry.ID3V2Tag.ID3V2_4;

public class TagHeaderParser {

    public static final String ID = "ID3";

    public static final int ID_LENGTH = 3;
    public static final int HEADER_LENGTH = 10;
    public static final int SIZE_LENGTH = 4;

    public static final int MAJOR_VERSION_OFFSET = 3;
    public static final int REVISION_NUMBER_OFFSET = 4;
    public static final int FLAGS_OFFSET = 5;
    public static final int SIZE_OFFSET = 6;

    private byte[] tagHeader;
    private byte[] extendedHeaderSize;

    public void read(RandomAccessFile file) throws IOException {

        tagHeader = new byte[HEADER_LENGTH];
        file.seek(0);
        file.read(tagHeader, 0, HEADER_LENGTH);

        if (isExtendedHeaderPresent()) {
            extendedHeaderSize = new byte[4];
            file.seek(HEADER_LENGTH);
            file.read(extendedHeaderSize, 0, 4);
        }
    }

    public TagHeader parse() throws IOException, InvalidTagException {

        if (!isSupportedVersion()) throw new InvalidTagException("Tag is either invalid or not yet supported");

        final byte majorVersion = parseMajorVersion();
        final byte minorVersion = parseMinorVersion();

        return TagHeader.newBuilder(majorVersion)
                .setTagSize(parseTagSize())
                .setUnsynch(isUsynchronisationApplied())
                .setMinorVersion(minorVersion)
                .setIsExperimental(isTagExperimental())
                .setHasExtendedHeader(isFooterPresent())
                .setHasExtendedHeader(isExtendedHeaderPresent())
                .build(majorVersion);
    }

    private boolean isSupportedVersion() {

        byte[] id = Arrays.copyOfRange(tagHeader, 0, ID_LENGTH);
        byte majorVersion = tagHeader[MAJOR_VERSION_OFFSET];

        return new String(id).equals(ID) && (majorVersion == ID3V2_3 || majorVersion == ID3V2_4);
    }

    public int parseTagSize() {
        byte[] bytes = Arrays.copyOfRange(tagHeader, SIZE_OFFSET, SIZE_OFFSET + SIZE_LENGTH);
        return fromSynchSafeIntegerBytes(bytes);
    }

    public int parseExtendedHeaderSize() {
        if (extendedHeaderSize == null) return 0;
        return fromSynchSafeIntegerBytes(extendedHeaderSize);
    }

    public byte parseMajorVersion() { return tagHeader[MAJOR_VERSION_OFFSET]; }
    public byte parseMinorVersion() { return tagHeader[REVISION_NUMBER_OFFSET]; }

    public boolean isUsynchronisationApplied() { return (tagHeader[FLAGS_OFFSET] & TagHeader.FLAG_UNSYNCH) != 0; }
    public boolean isExtendedHeaderPresent()   { return (tagHeader[FLAGS_OFFSET] & TagHeader.FLAG_EXTENDED_HEADER) != 0; }
    public boolean isTagExperimental()         { return (tagHeader[FLAGS_OFFSET] & TagHeader.FLAG_EXPERIMENTAL) != 0; }
    public boolean isFooterPresent()           { return (tagHeader[FLAGS_OFFSET] & TagHeader.FLAG_FOOTER_PRESENT) != 0; }
}
