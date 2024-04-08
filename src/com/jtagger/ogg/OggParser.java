package com.jtagger.ogg;

import com.jtagger.StreamInfo;
import com.jtagger.TagParser;
import com.jtagger.ogg.vorbis.VorbisComments;
import com.jtagger.utils.IntegerUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import static com.jtagger.ogg.OggPage.SEGMENT_MAX_SIZE;

abstract public class OggParser implements TagParser<VorbisComments> {

    protected ArrayList<OggPage> pages;
    protected ArrayList<OggPacket> packets;

    private int PCMPageIndex = 3;
    private int streamOffset = 0;
    private int headerPages  = 0;

    int headerPages() {
        return headerPages;
    }

    int getStreamOffset() {
        return streamOffset;
    }

    int getSerialNumber() {
        return pages.get(0).getHeader().getSerialNumber();
    }

    int getPCMPageIndex() {
        return PCMPageIndex;
    }

    protected int getDuration(StreamInfo streamInfo) {

        OggPage lastPage = pages.get(pages.size() - 1);
        final long totalSamples = lastPage.getHeader().getGranulePosition();
        final int sampleRate    = streamInfo.getSampleRate();

        return (int) (totalSamples / sampleRate);
    }

    public OggPage parsePage(RandomAccessFile file, boolean meta) throws IOException {

        int position = (int) file.getFilePointer();
        int offset   = 0;

        byte[] headerMagic;
        byte[] pageHeader  = new byte[OggPageHeader.OGG_HEADER_SIZE];
        file.read(pageHeader, 0, pageHeader.length);

        headerMagic = Arrays.copyOfRange(pageHeader, offset, offset + 4); offset += 4;
        if (!Arrays.equals(headerMagic, OggPageHeader.OGG_HEADER_MAGIC)) {
            throw new IllegalStateException(new String(headerMagic));
        }

        byte flags = pageHeader[++offset];
        boolean isFreshPage = (flags & 0x01) == 0;
        boolean isFirstPage = (flags & 0x02) != 0;
        boolean isLastPage  = (flags & 0x04) != 0;
        offset++;

        long granulePosition = IntegerUtils.toUInt64LE(Arrays.copyOfRange(pageHeader, offset, offset + 8)); offset += 8;
        int serialNumber     = IntegerUtils.toUInt32LE(Arrays.copyOfRange(pageHeader, offset, offset + 4)); offset += 4;
        int pageSequence     = IntegerUtils.toUInt32LE(Arrays.copyOfRange(pageHeader, offset, offset + 4)); offset += 4;
        int checksum         = IntegerUtils.toUInt32LE(Arrays.copyOfRange(pageHeader, offset, offset + 4)); offset += 4;
        int pageSegments     = Byte.toUnsignedInt(pageHeader[offset++]);
        int pageDataSize     = 0;

        if (meta && granulePosition > 0) {
            file.seek(position);
            return null;
        }

        byte[] pageData;
        byte[] segmentTable = new byte[pageSegments];
        file.read(segmentTable, 0, pageSegments);

        for (byte b : segmentTable) {
            pageDataSize += Byte.toUnsignedInt(b);
        }

        pageData = new byte[pageDataSize];
        file.read(pageData, 0, pageData.length);

        OggPageHeader oggPageHeader = new OggPageHeader();
        oggPageHeader.setFreshPage(isFreshPage);
        oggPageHeader.setFirstPage(isFirstPage);
        oggPageHeader.setLastPage(isLastPage);
        oggPageHeader.setGranulePosition(granulePosition);
        oggPageHeader.setSerialNumber(serialNumber);
        oggPageHeader.setSequenceNumber(pageSequence);
        oggPageHeader.setChecksum(checksum);
        oggPageHeader.setPageSegments(pageSegments);
        oggPageHeader.setSegmentTable(segmentTable);

        return new OggPage(oggPageHeader, pageData);
    }

    public ArrayList<OggPage> parsePages(RandomAccessFile file) {
        try {

            if (pages != null) return pages;
            ArrayList<OggPage> pages = new ArrayList<>();

            file.seek(0);
            OggPage page;
            long fLength = file.length();

            while (file.getFilePointer() < fLength) {
                page = parsePage(file, true);
                if (page == null) {
                    streamOffset = (int) file.getFilePointer();
                    break;
                }
                pages.add(page);
                if (page.getHeader().isLastPage()) {
                    break;
                }
            }

            headerPages = pages.size();
            this.pages = pages;
            return pages;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<OggPacket> parsePackets(ArrayList<OggPage> pages) {

        if (packets != null) return packets;
        ArrayList<OggPacket> packets = new ArrayList<>();

        OggPacket packet = new OggPacket();
        for (OggPage page : pages) {

            byte[] segmentTable = page.getHeader().getSegmentTable();
            byte[] pageData = page.getData();

            int offset = 0;
            int length = 0;

            for (int i = 0; i < segmentTable.length; i++) {

                int segment = Byte.toUnsignedInt(segmentTable[i]);
                length += segment;

                if (segment < SEGMENT_MAX_SIZE) {
                    if (segment != 0) {
                        packet.write(pageData, offset, length);
                    }
                    packets.add(packet);
                    offset = length;
                    length = 0;
                    packet = new OggPacket();
                }
                else if (i == (segmentTable.length - 1)) {
                    packet.write(pageData, offset, length);
                }
            }
        }
        this.packets = packets;
        return packets;
    }
}
