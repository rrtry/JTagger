package com.rrtry.ogg;

import com.rrtry.AbstractTagEditor;
import com.rrtry.AbstractTag;
import com.rrtry.ogg.opus.OggOpusParser;
import com.rrtry.ogg.vorbis.OggVorbisParser;
import com.rrtry.ogg.vorbis.VorbisComments;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;

import static com.rrtry.utils.FileContentTypeDetector.OGG_OPUS_MIME_TYPE;
import static com.rrtry.utils.FileContentTypeDetector.OGG_VORBIS_MIME_TYPE;

abstract public class OggTagEditor extends AbstractTagEditor<VorbisComments> {

    private OggParser parser;

    protected ArrayList<OggPage> pages;
    protected ArrayList<OggPacket> packets;

    abstract protected CommentHeader getCommentHeader();
    abstract protected ArrayList<OggPacket> getHeaderPackets();

    private static OggParser getOggParser(String mimeType) {
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return new OggOpusParser();
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return new OggVorbisParser();
        throw new NotImplementedException();
    }

    public OggParser getParser() {
        return parser;
    }

    public ArrayList<OggPage> getPages() {
        return pages;
    }

    public ArrayList<OggPacket> getPackets() {
        return packets;
    }

    @Override
    protected void parseTag() throws IOException {

        this.parser = getOggParser(mimeType);
        this.tag    = parser.parseTag(file);

        this.pages   = parser.parsePages(file);
        this.packets = parser.parsePackets(pages);
    }

    @Override
    public void commit() throws IOException {

        final long originalFileSize = file.length();
        final int PCMPageIndex      = parser.getPCMPageIndex();
        final int serialNumber      = parser.getSerialNumber();
        final int startingSeqNum    = 1;

        ArrayList<OggPage> paginatedPackets;
        ArrayList<OggPage> oggPages  = new ArrayList<>();

        paginatedPackets = OggPage.paginatePackets(getHeaderPackets(), serialNumber, startingSeqNum);

        oggPages.add(pages.get(0));
        oggPages.addAll(paginatedPackets);
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
    public void setTag(AbstractTag tag) {

        if (tag instanceof VorbisComments) {
            tag.assemble();
            this.tag = (VorbisComments) tag;
            return;
        }

        VorbisComments vorbisComments = new VorbisComments(
                mimeType.equals(OGG_VORBIS_MIME_TYPE)
        );

        convertTag(tag, vorbisComments);
        vorbisComments.assemble();
        this.tag = vorbisComments;
    }
}
