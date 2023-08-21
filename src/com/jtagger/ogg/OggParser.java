package com.jtagger.ogg;

import com.jtagger.AbstractTag;
import com.jtagger.StreamInfo;
import com.jtagger.TagParser;
import com.jtagger.ogg.vorbis.VorbisComments;
import com.jtagger.utils.IntegerUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

abstract public class OggParser implements TagParser<VorbisComments> {

    protected ArrayList<OggPage> pages;
    protected ArrayList<OggPacket> packets;
    private int PCMPageIndex = 3;

    public int getSerialNumber() {
        return pages.get(0).getHeader().getSerialNumber();
    }

    public int getPCMPageIndex() {
        return PCMPageIndex;
    }

    protected int getDuration(StreamInfo streamInfo) {

        OggPage lastPage = pages.get(pages.size() - 1);
        final long totalSamples = lastPage.getHeader().getGranulePosition();
        final int sampleRate    = streamInfo.getSampleRate();

        return (int) (totalSamples / sampleRate);
    }

    public ArrayList<OggPage> parsePages(RandomAccessFile file) {
        try {

            if (pages != null) return pages;
            ArrayList<OggPage> pages = new ArrayList<>();

            file.seek(0);
            while (file.getFilePointer() < file.length()) {

                int offset = 0;

                byte[] headerMagic;
                byte[] pageHeader  = new byte[OggPageHeader.OGG_HEADER_SIZE];
                file.read(pageHeader, 0, pageHeader.length);

                headerMagic = Arrays.copyOfRange(pageHeader, offset, offset + 4); offset += 4;
                if (!Arrays.equals(headerMagic, OggPageHeader.OGG_HEADER_MAGIC)) throw new IllegalStateException(new String(headerMagic));

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

                OggPage page = new OggPage(oggPageHeader, pageData);
                page.assemble();
                pages.add(page);

                if (isLastPage) break;
            }

            for (int i = 0; i < pages.size(); i++) {
                if (pages.get(i).getHeader().getGranulePosition() > 0) {
                    PCMPageIndex = i; break;
                }
            }

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

        for (OggPage page : pages) {

            OggPageHeader header = page.getHeader();

            boolean isFreshPage = header.isFreshPage();
            byte[] segmentTable = header.getSegmentTable();
            byte[] pageData     = page.getPageDataTruncated();

            OggPacket packet = !isFreshPage && !packets.isEmpty() ? packets.get(packets.size() - 1) : new OggPacket();
            int offset = 0;

            for (byte b : segmentTable) {

                int segmentSize = Byte.toUnsignedInt(b);

                if (segmentSize > 0) {
                    byte[] segment = Arrays.copyOfRange(pageData, offset, offset + segmentSize);
                    offset += segmentSize;
                    packet.appendPacketData(segment);
                }
                if (segmentSize < 255) {
                    if (!packets.contains(packet)) packets.add(packet);
                    packet = new OggPacket();
                }
            }

            int lastSegment = Byte.toUnsignedInt(segmentTable[segmentTable.length - 1]);
            if (lastSegment == 255 && !packets.contains(packet)) packets.add(packet);
        }

        this.packets = packets;
        return packets;
    }
}
