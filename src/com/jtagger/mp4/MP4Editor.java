package com.jtagger.mp4;

import com.jtagger.AbstractTag;
import com.jtagger.AbstractTagEditor;
import com.jtagger.utils.IntegerUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class MP4Editor extends AbstractTagEditor<MP4> {

    private MP4Parser parser;

    public MP4Parser getParser() {
        return parser;
    }

    @Override
    protected void parseTag() {
        parser = new MP4Parser();
        this.tag = parser.parseTag(file);
    }

    private void updateStco(int delta) {

        StcoAtom stcoAtom;
        MP4Atom moovAtom;

        moovAtom = tag.getMoovAtom();
        if (tag.getMdatStart() > tag.getMoovStart()) {

            stcoAtom = (StcoAtom) tag.findAtom("stco", moovAtom);
            assert stcoAtom != null;
            stcoAtom.updateOffsets(delta);

            int j = (int) ((stcoAtom.getStart() - tag.getMoovStart()) + 16);
            for (int offset : stcoAtom.getOffsets()) {
                System.arraycopy(IntegerUtils.fromUInt32BE(offset), 0, tag.getBytes(), j, 4);
                j += 4;
            }
        }
    }

    private void updateTfhd(int delta) throws IOException {
        for (MP4Atom atom : tag.getAtoms()) {
            if (atom.getType().equals("moof") && atom.getStart() > tag.getMoovStart()) {

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

    // TODO: make use of padding ('free' atom)
    @Override
    public void commit() throws IOException {

        if (tag == null) {
            throw new IllegalStateException("Corrupted mp4 file");
        }

        final int fileLength = (int) file.length();
        final int newSize    = tag.getBytes().length;
        final int prevSize   = tag.getMoovSize();

        final int moovStart = tag.getMoovStart();
        final int moovEnd   = tag.getMoovEnd();
        final int delta     = newSize - prevSize;

        if (tag.isFragmented()) {
            System.out.println("MP4Editor.commit: MP4 is fragmented updating tfhd atom");
            updateTfhd(delta);
        } else {
            System.out.println("MP4Editor.commit: MP4 is not fragmented updating stco atom");
            updateStco(delta);
        }

        if (!tag.isMoovLast() && delta != 0) {

            System.out.println("MP4Editor.commit: moov size changed, rewriting file");
            byte[] tempBuffer = new byte[1024];
            File temp = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            RandomAccessFile tempFile = new RandomAccessFile(temp.getAbsolutePath(), "rw");

            file.seek(moovEnd);
            while (file.getFilePointer() < fileLength) {
                int size = (int) Math.min(tempBuffer.length, fileLength - file.getFilePointer());
                if (file.read(tempBuffer, 0, size) == -1) {
                    break;
                }
                tempFile.write(tempBuffer, 0, size);
            }

            file.seek(moovStart);
            file.write(tag.getBytes());
            tempFile.seek(0);

            while (tempFile.read(tempBuffer) != -1) {
                file.write(tempBuffer);
            }

            file.setLength(moovStart + newSize + tempFile.length());
            tempFile.close();

            if (!temp.delete()) {
                System.err.println("Could not delete temp file");
            }
            return;
        }

        System.out.println("MP4Editor.commit: using padding space to write tag");
        file.seek(moovStart);
        file.write(tag.getBytes());

        if (tag.isMoovLast()) {
            System.out.println("MP4Editor.commit: truncating file");
            file.setLength(file.getFilePointer());
        }
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
