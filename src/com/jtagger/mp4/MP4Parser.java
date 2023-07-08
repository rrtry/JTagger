package com.jtagger.mp4;

import com.jtagger.StreamInfoParser;
import com.jtagger.TagParser;
import com.jtagger.utils.IntegerUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MP4Parser implements TagParser<MP4>, StreamInfoParser<MP4> {

    private MP4 mp4;

    private MdhdAtom parseMdhdAtom(byte[] atom) {

        int i = 8;
        byte version = atom[i]; i += 4;

        final long dateCreated;
        final long dateModified;
        final long duration;
        final long timescale;

        if (version == 1) {
            dateCreated  = IntegerUtils.toUInt64BE(Arrays.copyOfRange(atom, i, i + 8)); i += 8;
            dateModified = IntegerUtils.toUInt64BE(Arrays.copyOfRange(atom, i, i + 8)); i += 8;
            timescale    = Integer.toUnsignedLong(IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4))); i += 4;
            duration     = IntegerUtils.toUInt64BE(Arrays.copyOfRange(atom, i, i + 8)); i += 8;
        } else {
            dateCreated  = Integer.toUnsignedLong(IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4))); i += 4;
            dateModified = Integer.toUnsignedLong(IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4))); i += 4;
            timescale    = Integer.toUnsignedLong(IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4))); i += 4;
            duration     = Integer.toUnsignedLong(IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4))); i += 4;
        }

        return new MdhdAtom("mdhd", atom, dateCreated, dateModified, duration, timescale, version);
    }

    private StsdAtom parseStsdAtom(byte[] atom) throws InvalidAtomException {

        final int channels;
        final int sampleSize;

        final String audioFormat;
        final int sampleRate;

        List<String> audioFormats = Arrays.asList(
                "mp4a", "enca", "samr", "sawb"
        );

        byte[] startTag = new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80};
        byte[] endTag   = new byte[] {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE};

        int i = 12;
        int descCount = IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4)); i += 8;

        if (descCount < 1) {
            throw new InvalidAtomException("Invalid number of descriptions");
        }

        audioFormat = new String(Arrays.copyOfRange(atom, i, i + 4), ISO_8859_1);  i += 20;
        if (!audioFormats.contains(audioFormat)) {
            throw new InvalidAtomException("Unexpected audio format");
        }

        channels   = Short.toUnsignedInt(IntegerUtils.toUInt16BE(Arrays.copyOfRange(atom, i, i + 2))); i += 2;
        sampleSize = Short.toUnsignedInt(IntegerUtils.toUInt16BE(Arrays.copyOfRange(atom, i, i + 2))); i += 6;
        sampleRate = Short.toUnsignedInt(IntegerUtils.toUInt16BE(Arrays.copyOfRange(atom, i, i + 2))); i += 16;

        byte[] typeTag;
        if (atom[i++] != 0x03) throw new InvalidAtomException("Invalid ES descriptor tag type");

        typeTag = Arrays.copyOfRange(atom, i, i + 3);
        if (Arrays.equals(typeTag, startTag)) i += 3;
        if (Arrays.equals(typeTag, endTag))   i += 3;

        i += 4;
        if (atom[i++] != 0x04) throw new InvalidAtomException("Invalid decoder config type tag");

        typeTag = Arrays.copyOfRange(atom, i, i + 3);
        if (Arrays.equals(typeTag, startTag)) i += 3;
        if (Arrays.equals(typeTag, endTag))   i += 3;
        i++;

        int codec      = Byte.toUnsignedInt(atom[i++]);
        int flags      = Byte.toUnsignedInt(atom[i++]);
        int streamType = flags >> 2;
        int bufSize    = IntegerUtils.toUInt24BE(Arrays.copyOfRange(atom, i, i + 3)); i += 3;
        int maxBitrate = IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4)); i += 4;
        int avgBitrate = IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4)); i += 4;

        return new StsdAtom(
                "stsd", atom, channels, sampleSize, sampleRate,
                bufSize, maxBitrate, avgBitrate, streamType, codec
        );
    }

    private FreeFormAtom parseFreeFormAtom(byte[] atom) throws InvalidAtomException {

        int i = 9;
        while (atom[i] != ':') i++;

        String mean = new String(Arrays.copyOfRange(atom, 9, i), ISO_8859_1);
        String name = new String(Arrays.copyOfRange(atom, ++i, i + 4)); i += 4;

        int size    = IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4)); i += 4;
        String type = new String(Arrays.copyOfRange(atom, i, i + 4)); i += 4;

        if (size < 8) {
            throw new InvalidAtomException("Invalid atom size");
        }
        if (!type.equals("data")) {
            throw new InvalidAtomException("Unexpected atom: " + type);
        }

        int flags   = IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, i, i + 4)); i += 4;
        String text = new String(Arrays.copyOfRange(atom, i, i + size - 8 - 4));

        FreeFormAtom freeFormAtom = new FreeFormAtom(mean, name, atom);
        freeFormAtom.setAtomData(text);
        return freeFormAtom;
    }

    private StcoAtom parseStco(byte[] atom) {

        StcoAtom stco = new StcoAtom("stco", atom);

        int entryCount = IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, 12, 16));
        int[] offsets  = new int[entryCount];
        byte[] entries = Arrays.copyOfRange(atom, 16, atom.length);

        for (int i = 0; i < entries.length; i += 4) {
            offsets[i / 4] = IntegerUtils.toUInt32BE(Arrays.copyOfRange(entries, i, i + 4));
        }

        stco.setOffsets(offsets);
        return stco;
    }

    @SuppressWarnings({"rawtypes"})
    private ItunesAtom parseItunesAtom(byte[] atom) throws InvalidAtomException {

        final int size;
        final int dataType;
        final String type;
        final String dataAtomType;

        byte[] sizeBytes         = Arrays.copyOfRange(atom, 8, 12);
        byte[] dataAtomTypeBytes = Arrays.copyOfRange(atom, 12, 16);
        byte[] typeBytes         = Arrays.copyOfRange(atom, 4, 8);

        dataAtomType = new String(dataAtomTypeBytes, ISO_8859_1);
        type         = new String(typeBytes, ISO_8859_1);
        size         = IntegerUtils.toUInt32BE(sizeBytes);
        dataType     = IntegerUtils.toUInt32BE(Arrays.copyOfRange(atom, 16, 20));

        if (size < 8) {
            throw new InvalidAtomException("Invalid atom size: " + size);
        }
        if (!dataAtomType.equals("data")) {
            throw new InvalidAtomException("Unexpected atom: " + dataAtomType);
        }

        ItunesAtom itunesAtom = null;
        if (dataType == ItunesAtom.TYPE_UTF8)     itunesAtom = new TextAtom(type, atom);
        if (dataType == ItunesAtom.TYPE_IMPLICIT) itunesAtom = new TrackNumberAtom(type, atom);
        if (dataType == ItunesAtom.TYPE_JPEG)     itunesAtom = new PictureAtom(type, atom);
        if (dataType == ItunesAtom.TYPE_INTEGER)  itunesAtom = new NumberAtom(type, atom);

        if (itunesAtom == null) itunesAtom = new TextAtom(type, atom);
        itunesAtom.setAtomData(Arrays.copyOfRange(atom, 24, 8 + size));
        return itunesAtom;
    }

    private void parseAtom(
            MP4Atom parentAtom,
            byte[] childAtom,
            boolean hasVersionInt,
            ArrayList<MP4Atom> atoms) throws InvalidAtomException
    {
        int index = hasVersionInt ? 12 : 8;
        while (index < childAtom.length) {

            final int startOffset = index;
            final int endOffset;
            final int atomSize;

            String atomType;
            byte[] sizeBytes = Arrays.copyOfRange(childAtom, index, index + 4); index += 4;
            byte[] typeBytes = Arrays.copyOfRange(childAtom, index, index + 4); index += 4;

            atomSize  = IntegerUtils.toUInt32BE(sizeBytes);
            endOffset = startOffset + atomSize;
            atomType  = new String(typeBytes, ISO_8859_1);

            if (atomSize < 8) {
                throw new InvalidAtomException("Invalid atom size: " + atomSize);
            }

            byte[] atomData = Arrays.copyOfRange(childAtom, startOffset, endOffset);
            MP4Atom atom    = new MP4Atom(atomType, atomData);

            if (atomType.equals("----")) {
                MP4Atom freeFormAtom = parseFreeFormAtom(atomData);
                parentAtom.appendChildAtom(freeFormAtom);
                atoms.add(freeFormAtom);
            }
            else if (parentAtom.getType().equals("ilst")) {
                MP4Atom itunesAtom = (MP4Atom) parseItunesAtom(atomData);
                parentAtom.appendChildAtom(itunesAtom);
                atoms.add(itunesAtom);
            }
            else if (atomType.equals("stco")) {
                StcoAtom stcoAtom = parseStco(atomData);
                parentAtom.appendChildAtom(stcoAtom);
                atoms.add(stcoAtom);
            }
            else if (atomType.equals("stsd")) {
                StsdAtom stsdAtom = parseStsdAtom(atomData);
                parentAtom.appendChildAtom(stsdAtom);
                atoms.add(stsdAtom);
            }
            else if (atomType.equals("mdhd")) {
                MdhdAtom mdhdAtom = parseMdhdAtom(atomData);
                parentAtom.appendChildAtom(mdhdAtom);
                atoms.add(mdhdAtom);
            }
            else if (Arrays.asList(MP4.ATOMS).contains(atomType)) {
                parentAtom.appendChildAtom(atom);
                atoms.add(atom);
                parseAtom(
                        atom,
                        atomData,
                        atomType.equals("hdlr") || atomType.equals("meta"),
                        atoms
                );
            }
            else if (!atoms.contains(atom)) {
                parentAtom.appendChildAtom(atom);
                atoms.add(atom);
            }
            index = endOffset;
        }
    }

    @Override
    public MP4 parseTag(RandomAccessFile file) {
        try {

            ArrayList<MP4Atom> atoms = new ArrayList<>();
            int mdatStart   = 0;
            int mdatEnd     = 0;

            file.seek(0);
            while (file.getFilePointer() < file.length()) {

                byte[] sizeBytes = new byte[4];
                byte[] typeBytes = new byte[4];

                if (file.read(sizeBytes, 0, 4) == -1) break;
                if (file.read(typeBytes, 0, 4) == -1) break;

                int atomSize    = IntegerUtils.toUInt32BE(sizeBytes);
                String atomType = new String(typeBytes, ISO_8859_1);

                if (atomType.equals("mdat")) {
                    mdatStart = (int) file.getFilePointer() - 8;
                    mdatEnd   = mdatStart + atomSize;
                    file.skipBytes(atomSize - 8);
                } else {

                    byte[] atomData = new byte[atomSize];
                    System.arraycopy(sizeBytes, 0, atomData, 0, 4);
                    System.arraycopy(typeBytes, 0, atomData, 4, 4);

                    file.read(atomData, 8, atomSize - 8);
                    MP4Atom mp4Atom = new MP4Atom(atomType, atomData);
                    mp4Atom.setTopLevelAtom(true);
                    atoms.add(mp4Atom);

                    if (atomType.equals("moov")) {
                        parseAtom(mp4Atom, atomData, false, atoms);
                    }
                }
            }

            atoms.removeIf(atom -> !atom.isTopLevelAtom());
            this.mp4 = new MP4(atoms, mdatStart, mdatEnd);
            return mp4;

        } catch (IOException | InvalidAtomException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MP4 parseStreamInfo(RandomAccessFile file) {
        return mp4;
    }
}
