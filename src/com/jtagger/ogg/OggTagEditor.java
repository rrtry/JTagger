package com.jtagger.ogg;

import com.jtagger.AbstractTagEditor;
import com.jtagger.AbstractTag;
import com.jtagger.ogg.flac.OggFlacParser;
import com.jtagger.ogg.opus.OggOpusParser;
import com.jtagger.ogg.vorbis.OggVorbisParser;
import com.jtagger.ogg.vorbis.VorbisComments;
import com.jtagger.utils.BytesIO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.jtagger.MediaFile.FileContentTypeDetector.*;

abstract public class OggTagEditor extends AbstractTagEditor<VorbisComments> {

    private OggParser parser;

    protected ArrayList<OggPage> pages;
    protected ArrayList<OggPacket> packets;

    abstract protected void setHeaderPackets();

    private static OggParser getOggParser(String mimeType) {
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return new OggOpusParser();
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return new OggVorbisParser();
        if (mimeType.equals(OGG_FLAC_MIME_TYPE))   return new OggFlacParser();
        throw new IllegalStateException();
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
        this.parser  = getOggParser(mimeType);
        this.tag     = parser.parseTag(file);
        this.pages   = parser.parsePages(file);
        this.packets = parser.parsePackets(pages);
    }

    @Override
    public void commit() throws IOException {

        super.commit();
        setHeaderPackets();
        ArrayList<OggPage> newPages = new ArrayList<>();
        newPages.add(pages.get(0));
        newPages.addAll(OggPage.fromPackets(
                packets,
                parser.getSerialNumber(),
                1
        ));

        int size = 0;
        for (OggPage page : newPages) {
            byte[] buffer = page.assemble();
            size += buffer.length;
        }

        if (newPages.size() != parser.headerPages()) {

            int diff = newPages.size() - parser.headerPages();
            file.seek(parser.getStreamOffset());

            long position;
            long fLength = file.length();

            while (file.getFilePointer() < fLength) {

                position = file.getFilePointer();
                OggPage page = parser.parsePage(file, false);

                OggPageHeader header = page.getHeader();
                header.setSequenceNumber(header.getPageSequenceNumber() + diff);

                file.seek(position);
                file.write(page.assemble());

                if (header.isLastPage())
                    break;
            }
        }
        int diff = size - parser.getStreamOffset();
        if (diff != 0) {

            int from = parser.getStreamOffset();
            int to   = from + diff;

            BytesIO.moveBlock(file, from, to, diff, (int) file.length() - from);
        }
        file.seek(0);
        for (OggPage page : newPages) {
            file.write(page.getBytes());
        }
    }

    @Override
    public void removeTag() {
        tag.setCommentsMap(new LinkedHashMap<>());
        tag.assemble();
    }

    @Override
    public void setTag(AbstractTag tag) {

        if (tag instanceof VorbisComments) {
            this.tag = (VorbisComments) tag;
            this.tag.setFramingBit(mimeType.equals(OGG_VORBIS_MIME_TYPE));
            return;
        }

        VorbisComments vorbisComments = new VorbisComments(
                mimeType.equals(OGG_VORBIS_MIME_TYPE)
        );
        convertTag(tag, vorbisComments);
        this.tag = vorbisComments;
    }
}
