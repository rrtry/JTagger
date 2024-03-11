package com.jtagger.mp4;

import com.jtagger.AbstractTag;
import com.jtagger.AbstractTagEditor;
import com.jtagger.utils.FileIO;
import com.jtagger.utils.IntegerUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import static com.jtagger.utils.IntegerUtils.fromUInt32BE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MP4Editor extends AbstractTagEditor<MP4> {

    private static final int PADDING = 1024 * 100;
    private MP4Parser parser;

    public MP4Parser getParser() {
        return parser;
    }

    @Override
    protected void parseTag() {
        parser = new MP4Parser();
        this.tag = parser.parseTag(file);
    }

    private void updateOffsets(int delta) throws IOException {
        if (tag.isFragmented()) updateTfhd(delta); else updateStco(delta);
    }

    private void updateParents(MP4Atom parent, int delta) throws IOException {

        int size;
        while (parent != null) {
            file.seek(parent.getStart());
            size = file.readInt();
            file.seek(file.getFilePointer() - 4);
            file.write(fromUInt32BE(size + delta));
            parent = parent.getParent();
        }
    }

    private void writePadding(int paddingSize) throws IOException {

        byte[] paddingBuffer = new byte[PADDING];
        System.arraycopy(fromUInt32BE(paddingSize), 0, paddingBuffer, 0, 4);
        System.arraycopy("free".getBytes(ISO_8859_1), 0, paddingBuffer, 4, 4);

        file.write(paddingBuffer);
    }

    private void updateStco(int delta) throws IOException {

        StcoAtom stcoAtom;
        MP4Atom moovAtom;

        moovAtom = tag.getMoovAtom();
        if (tag.getMdatStart() > tag.getIlstStart()) {
            stcoAtom = (StcoAtom) tag.findAtom("stco", moovAtom);
            if (stcoAtom != null) {

                stcoAtom.updateOffsets(delta);
                file.seek(stcoAtom.getStart() + 16);

                for (int offset : stcoAtom.getOffsets()) {
                    file.write(fromUInt32BE(offset));
                }
            }
        }
    }

    private void updateTfhd(int delta) throws IOException {
        for (MP4Atom atom : tag.getAtoms()) {
            if (atom.getType().equals("moof") && atom.getStart() > tag.getIlstStart()) {

                MP4Atom tfhd = tag.findAtom("tfhd", atom);
                if (tfhd != null) {

                    file.seek(tfhd.getStart() + 8);
                    int flags = file.readInt();

                    if ((flags & 0x1) == 1) {
                        file.skipBytes(4); // track_id
                        long baseDataOffset = file.readLong();
                        file.seek(file.getFilePointer() - 8);
                        file.write(IntegerUtils.fromUInt64BE(baseDataOffset + delta));
                    }
                }
            }
        }
    }

    @Override
    public void commit() throws IOException {

        if (tag == null) {
            throw new IllegalStateException("Corrupted mp4 file");
        }

        final long fileLength = file.length();
        final int prevSize    = tag.getIlstSize();
        final int newSize     = tag.getBytes().length;

        int sizeDiff  = newSize - prevSize;
        int ilstStart = tag.getIlstStart();
        int ilstEnd   = tag.getIlstEnd();

        if (sizeDiff == 0) {
            FileIO.writeBlock(
                    file, tag.getBytes(), ilstStart
            );
            return;
        }

        MP4Atom ilstAtom   = tag.getIlstAtom();
        MP4Atom ilstParent = ilstAtom.getParent();

        if (ilstEnd == fileLength) {
            updateParents(ilstParent, sizeDiff);
            FileIO.writeBlock(file, tag.getBytes(), ilstStart);
            return;
        }

        ArrayList<MP4Atom> children = ilstParent.getChildren();
        int ilstIndex = children.indexOf(ilstAtom);

        if ((ilstIndex + 1) < children.size()) {

            MP4Atom free = children.get(ilstIndex + 1);
            if (free.getType().equals("free")) {
                int paddingSize = free.getSize() + (prevSize - newSize);
                if (paddingSize >= 8) {
                    FileIO.writeBlock(file, tag.getBytes(), ilstStart);
                    writePadding(paddingSize);
                    return;
                }
                sizeDiff += (PADDING - free.getSize());
                ilstEnd = free.getEnd();
            }
        } else {
            sizeDiff += PADDING;
        }

        updateOffsets(sizeDiff);
        updateParents(ilstParent, sizeDiff);

        /*
        FileIO.resize(file,
                ilstEnd,
                ilstEnd + sizeDiff,
                sizeDiff,
                (int) fileLength - ilstEnd
        );
        FileIO.writeBlock(file, tag.getBytes(), ilstStart);
        writePadding(PADDING); */

        byte[] paddingBuffer = new byte[PADDING];
        System.arraycopy(fromUInt32BE(paddingBuffer.length), 0, paddingBuffer, 0, Integer.BYTES);
        System.arraycopy("free".getBytes(ISO_8859_1), 0, paddingBuffer, 4, 4);

        FileIO.overwrite(
                file,
                tag.getBytes(),
                paddingBuffer,
                ilstEnd,
                ilstStart,
                sizeDiff,
                (int) file.length() - ilstEnd
        );
    }

    @Override
    public void removeTag() {
        tag.removeMetadataAtoms();
        tag.assemble();
    }

    @Override
    public void setTag(AbstractTag newTag) {

        if (tag == null) {
            throw new IllegalStateException("Corrupted mp4 file");
        }
        if (newTag instanceof MP4) {
            MP4 mp4 = (MP4) newTag;
            tag.setMetadataAtoms(mp4.getMetadataAtoms());
            tag.assemble();
            return;
        }

        tag.removeMetadataAtoms();
        convertTag(newTag, tag);
        tag.assemble();
    }
}
