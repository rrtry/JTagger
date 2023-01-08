package com.rrtry.ogg;

import com.rrtry.AbstractTagEditor;

import java.io.IOException;
import java.util.ArrayList;

public class OggVorbisTagEditor extends AbstractTagEditor<VorbisCommentHeader> {

    private OggVorbisParser parser;

    private ArrayList<OggPage> pages;
    private ArrayList<OggPacket> packets;

    public OggVorbisParser getParser() {
        return parser;
    }

    @Override
    protected void parseTag() {

        this.parser = new OggVorbisParser();
        this.tag    = parser.parseVorbisCommentHeader(file);

        this.pages   = parser.parsePages(file);
        this.packets = parser.parsePackets(pages);
    }

    @Override
    protected String getFileMimeType() {
        return OGG_MIME_TYPE;
    }

    @Override
    public void commit() throws IOException {

        final long originalFileSize = file.length();
        final int PCMPageIndex      = parser.getPCMPageIndex();
        final int serialNumber      = parser.getSerialNumber();
        final int startingSeqNum    = 1;

        OggPacket commentHeaderPacket = new OggPacket(tag.getBytes());
        OggPacket setupHeaderPacket   = packets.get(2);

        ArrayList<OggPage> vorbisPages;
        ArrayList<OggPage> oggPages        = new ArrayList<>();
        ArrayList<OggPacket> vorbisPackets = new ArrayList<>();

        vorbisPackets.add(commentHeaderPacket);
        vorbisPackets.add(setupHeaderPacket);

        vorbisPages = OggPage.paginatePackets(vorbisPackets, serialNumber, startingSeqNum);

        oggPages.add(pages.get(0));
        oggPages.addAll(vorbisPages);
        oggPages.addAll(pages.subList(PCMPageIndex, pages.size()));

        file.seek(0);

        int totalSize = 0;
        for (int i = 0; i < oggPages.size(); i++) {

            OggPage page = oggPages.get(i);
            page.getHeader().setSequenceNumber(i);
            page.assemble();

            totalSize += page.getBytes().length;
            file.write(page.getBytes());
        }
        if (totalSize < originalFileSize) file.setLength(totalSize);
    }

    @Override
    public void setTag(VorbisCommentHeader tag) {
        tag.assemble(); this.tag = tag;
    }
}
