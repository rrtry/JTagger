package com.jtagger.ogg;

import com.jtagger.Component;
import com.jtagger.utils.IntegerUtils;
import java.util.Arrays;

public class OggPageHeader implements Component {

    public static final byte OGG_HEADER_SIZE = 27;
    public static final byte[] OGG_HEADER_MAGIC = new byte[] { 0x4f, 0x67, 0x67, 0x53 };

    private boolean isFreshPage = true;
    private boolean isFirstPage = false;
    private boolean isLastPage  = false;

    private long granulePosition = 0;

    private int serial       = 0;
    private int checksum     = 0;
    private int pageSequence = 0;
    private int pageSegments = 0;

    private byte[] bytes;
    private byte[] segmentTable = new byte[255];

    void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public boolean isFreshPage() {
        return isFreshPage;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public long getGranulePosition() {
        return granulePosition;
    }

    public int getSerialNumber() {
        return serial;
    }

    public int getChecksum() {
        return checksum;
    }

    public int getPageSequenceNumber() {
        return pageSequence;
    }

    public int getPageSegments() {
        return pageSegments;
    }

    public byte[] getSegmentTable() {
        return pageSegments < segmentTable.length ?
                Arrays.copyOf(segmentTable, pageSegments) :
                segmentTable;
    }

    public void setFreshPage(boolean isFreshPage) {
        this.isFreshPage = isFreshPage;
    }

    public void setFirstPage(boolean isFirstPage) {
        this.isFirstPage = isFirstPage;
    }

    public void setLastPage(boolean isLastPage) {
        this.isLastPage = isLastPage;
    }

    public void setGranulePosition(long granulePosition) {
        this.granulePosition = granulePosition;
    }

    public void setSerialNumber(int serial) {
        this.serial = serial;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public void setSequenceNumber(int pageSequence) {
        this.pageSequence = pageSequence;
    }

    public void setPageSegments(int pageSegments) {
        this.pageSegments = pageSegments;
    }

    public void setSegmentTable(byte[] segmentTable) {
        this.segmentTable = segmentTable;
    }

    boolean hasAvailableSegments() {
        return pageSegments < 255;
    }

    boolean addSegment(int segment) {
        if (hasAvailableSegments()) {
            segmentTable[pageSegments++] = (byte) segment;
            return true;
        }
        return false;
    }

    @Override
    public byte[] assemble(byte version) {

        int offset = 0;

        bytes = new byte[OGG_HEADER_SIZE + pageSegments];
        System.arraycopy(OGG_HEADER_MAGIC, 0, bytes, offset, OGG_HEADER_MAGIC.length); offset += OGG_HEADER_MAGIC.length;

        bytes[offset++] = 0x00;

        byte flags = 0x00;
        if (!isFreshPage) flags |= 0x01;
        if (isFirstPage)  flags |= 0x02;
        if (isLastPage)   flags |= 0x04;

        bytes[offset++] = flags;

        System.arraycopy(IntegerUtils.fromUInt64LE(granulePosition), 0, bytes, offset, Long.BYTES); offset += Long.BYTES;
        System.arraycopy(IntegerUtils.fromUInt32LE(serial), 0, bytes, offset, Integer.BYTES);       offset += Integer.BYTES;
        System.arraycopy(IntegerUtils.fromUInt32LE(pageSequence), 0, bytes, offset, Integer.BYTES); offset += Integer.BYTES;
        System.arraycopy(IntegerUtils.fromUInt32LE(checksum), 0, bytes, offset, Integer.BYTES);     offset += Integer.BYTES;

        bytes[offset++] = (byte) pageSegments;
        System.arraycopy(segmentTable, 0, bytes, offset, pageSegments);

        return bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
