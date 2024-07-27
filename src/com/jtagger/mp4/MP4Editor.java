package com.jtagger.mp4;

import com.jtagger.AbstractTag;
import com.jtagger.AbstractTagEditor;
import com.jtagger.utils.BytesIO;
import com.jtagger.utils.IntegerUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.jtagger.utils.IntegerUtils.fromUInt32BE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MP4Editor extends AbstractTagEditor<MP4> {

    private MP4Parser parser;

    public MP4Parser getParser() {
        return parser;
    }

    @Override
    protected void parseTag() throws IOException {
        parser = new MP4Parser();
        tag = parser.parseTag(file);
    }

    private void updateOffsets(int delta) throws IOException {
        if (parser.isMP4Fragmented()) updateTfhd(delta); else updateStco(delta);
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

    private void writePadding(int padding) throws IOException {

        byte[] paddingBuffer = new byte[padding];
        System.arraycopy(fromUInt32BE(padding), 0, paddingBuffer, 0, 4);
        System.arraycopy("free".getBytes(ISO_8859_1), 0, paddingBuffer, 4, 4);

        file.write(paddingBuffer);
    }

    private void updateStco(int delta) throws IOException {

        StcoAtom stcoAtom;
        MP4Atom moovAtom;

        moovAtom = tag.getMoovAtom();
        if (parser.getMdatStart() > parser.getMoovStart()) {
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
            if (atom.getType().equals("moof") && atom.getStart() > parser.getMoovStart()) {

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

    private void writeNewTag(ArrayList<MP4Atom> path) throws IOException {

        int delta;
        int size;

        int from;
        int to;
        int tagOffset;
        int padding = BytesIO.getPadding((int) file.length());

        ArrayList<MP4Atom> moovChildren = tag.getMoovAtom().getChildren();
        size  = tag.getBytes().length + 8 + 12 + 33 + padding;
        delta = size - (path.isEmpty() ? 0 : path.get(0).getSize());

        MP4Atom adjacent = path.isEmpty() ? moovChildren.get(moovChildren.size() - 1) : path.get(0);
        tagOffset = path.isEmpty() ? adjacent.getEnd() : adjacent.getStart();
        from = adjacent.getEnd();
        to   = from + delta;

        byte[] reserved = new byte[4];
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        out.write(IntegerUtils.fromUInt32BE(size));
        out.write("udta".getBytes(ISO_8859_1));
        out.write(IntegerUtils.fromUInt32BE(size - 8));
        out.write("meta".getBytes(ISO_8859_1));
        out.write(reserved);
        out.write(IntegerUtils.fromUInt32BE(33));
        out.write("hdlr".getBytes(ISO_8859_1));
        out.write(reserved);
        out.write(reserved);
        out.write("mdir".getBytes(ISO_8859_1));
        out.write("appl".getBytes(ISO_8859_1));
        out.write(reserved);
        out.write(reserved);
        out.write(0x0);
        out.write(tag.getBytes());
        out.write(IntegerUtils.fromUInt32BE(padding));
        out.write("free".getBytes(ISO_8859_1));

        byte[] tagBuffer = out.toByteArray();
        MP4Atom parent = path.isEmpty() ? tag.getMoovAtom() :
                (path.size() == 3 ? path.get(1) : path.get(path.size() - 1));

        updateOffsets(delta);
        updateParents(parent, delta);

        if (tag.getAtoms().indexOf(tag.getMoovAtom()) != tag.getAtoms().size() - 1) {
            BytesIO.moveBlock(file,
                    from,
                    to,
                    delta,
                    (int) file.length() - from
            );
        }
        BytesIO.writeBlock(file, tagBuffer, tagOffset);
    }

    private void updateTag() throws IOException {

        final long fileLength = file.length();
        final int prevSize    = parser.getIlstSize();
        final int newSize     = tag.getBytes().length;

        int sizeDiff  = newSize - prevSize;
        int ilstStart = parser.getIlstStart();
        int ilstEnd   = parser.getIlstEnd();

        if (sizeDiff == 0) {
            BytesIO.writeBlock(
                    file, tag.getBytes(), ilstStart
            );
            return;
        }

        MP4Atom ilstAtom   = tag.getIlstAtom();
        MP4Atom ilstParent = ilstAtom.getParent();
        if (ilstEnd == fileLength) {
            updateParents(ilstParent, sizeDiff);
            BytesIO.writeBlock(file, tag.getBytes(), ilstStart);
            file.setLength(fileLength + sizeDiff);
            return;
        }

        ArrayList<MP4Atom> children = ilstParent.getChildren();
        int ilstIndex  = children.indexOf(ilstAtom);
        int maxPadding = BytesIO.getPadding((int) file.length());
        int padding    = maxPadding;

        if ((ilstIndex + 1) < children.size()) {

            MP4Atom free = children.get(ilstIndex + 1);
            if (free.getType().equals("free")) {

                int paddingDiff = prevSize - newSize;
                int paddingSize = free.getSize() + paddingDiff;

                if (paddingSize == 0) {
                    BytesIO.writeBlock(file, tag.getBytes(), ilstStart);
                    return;
                }
                if (paddingSize >= 8 && paddingSize <= maxPadding) {
                    BytesIO.writeBlock(file, tag.getBytes(), ilstStart);
                    file.write(IntegerUtils.fromUInt32BE(paddingSize));
                    file.write("free".getBytes(ISO_8859_1));
                    if (paddingDiff > 0) {
                        file.write(new byte[paddingDiff]);
                    }
                    return;
                }
                padding = BytesIO.PADDING_MIN;
                sizeDiff += (padding - free.getSize());
                ilstEnd = free.getEnd();
            }
        } else {
            sizeDiff += padding;
        }

        updateOffsets(sizeDiff);
        updateParents(ilstParent, sizeDiff);

        BytesIO.moveBlock(file,
                ilstEnd,
                ilstEnd + sizeDiff,
                sizeDiff,
                (int) fileLength - ilstEnd
        );
        BytesIO.writeBlock(file, tag.getBytes(), ilstStart);
        writePadding(padding);
    }

    @Override
    public void commit() throws IOException {

        super.commit();
        if (tag == null) {
            throw new IllegalStateException("Corrupted mp4 file");
        }

        ArrayList<MP4Atom> path = new ArrayList<>(3);
        tag.findAtoms(tag.getMoovAtom(), path, "udta", "meta", "ilst");
        if (path.size() != 3) {
            writeNewTag(path);
            return;
        }
        updateTag();
    }

    @Override
    public void removeTag() {
        tag.removeMetadataAtoms();
        tag.assemble();
    }

    @Override
    public void setTag(AbstractTag newTag) {

        if (newTag instanceof MP4) {
            MP4 mp4 = (MP4) newTag;
            tag.setMetadataAtoms(mp4.getMetadataAtoms());
            return;
        }

        tag.removeMetadataAtoms();
        convertTag(newTag, tag);
    }
}
