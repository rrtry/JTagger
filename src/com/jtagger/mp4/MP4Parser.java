package com.jtagger.mp4;

import com.jtagger.StreamInfoParser;
import com.jtagger.TagParser;
import com.jtagger.utils.IntegerUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import static com.jtagger.mp4.ItunesAtom.*;
import static com.jtagger.utils.IntegerUtils.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MP4Parser implements TagParser<MP4>, StreamInfoParser<MP4> {

    private static final String AS_ENTRY_ALAC = "alac";
    private static final String AS_ENTRY_AC3  = "ac-3";
    private static final String AS_ENTRY_MP4A = "mp4a";

    private static final String SPEC_BOX_ALAC = "alac";
    private static final String SPEC_BOX_AC3  = "dac3";
    private static final String SPEC_BOX_MP4A = "esds";

    private MP4 mp4;
    private long globalOffset;

    private MdhdAtom parseMdhdAtom(byte[] atom) {

        int i = 8;
        byte version = atom[i]; i += 4;

        final long dateCreated;
        final long dateModified;
        final long duration;
        final long timescale;

        if (version == 1) {
            dateCreated  = IntegerUtils.toUInt64BE(Arrays.copyOfRange(atom, i, i += 8));
            dateModified = IntegerUtils.toUInt64BE(Arrays.copyOfRange(atom, i, i += 8));
            timescale    = Integer.toUnsignedLong(toUInt32BE(Arrays.copyOfRange(atom, i, i += 4)));
            duration     = IntegerUtils.toUInt64BE(Arrays.copyOfRange(atom, i, i += 8));
        } else {
            dateCreated  = Integer.toUnsignedLong(toUInt32BE(Arrays.copyOfRange(atom, i, i += 4)));
            dateModified = Integer.toUnsignedLong(toUInt32BE(Arrays.copyOfRange(atom, i, i += 4)));
            timescale    = Integer.toUnsignedLong(toUInt32BE(Arrays.copyOfRange(atom, i, i += 4)));
            duration     = Integer.toUnsignedLong(toUInt32BE(Arrays.copyOfRange(atom, i, i += 4)));
        }

        return new MdhdAtom("mdhd", atom, dateCreated, dateModified, duration, timescale, version);
    }

    private void parseALAC(StsdAtom stsd, byte[] parentAtom) {

        final short sampleSize;
        final byte  channels;
        final int   bitrate;
        final int   sampleRate;

        int pointer = 4; // skip frameLength (uint32_t);
        if (parentAtom[pointer++] != 0) {
            throw new IllegalStateException("ALAC: incompatible version");
        }

        pointer += 4;
        sampleSize = parentAtom[pointer++]; pointer += 3;
        channels   = parentAtom[pointer++]; pointer += 2 + 4;
        bitrate    = toUInt32BE(Arrays.copyOfRange(parentAtom, pointer, pointer += 4));
        sampleRate = toUInt32BE(Arrays.copyOfRange(parentAtom, pointer, pointer + 4));

        stsd.setSampleSize(sampleSize);
        stsd.setChannels(channels);
        stsd.setMaxBitrate(bitrate);
        stsd.setAvgBitrate(bitrate);
        stsd.setSampleRate(sampleRate);
    }

    private void parseAC3(StsdAtom stsd, byte[] atom) {

        final int[] ac3Bps = new int[] {
                32, 40, 48, 56, 64, 80, 96, 112, 128, 160,
                192, 224, 256, 320, 384, 448, 512, 576, 640
        };

        int ac3BoxBits;
        int ac3Bitrate;

        ac3BoxBits = IntegerUtils.toUInt24BE(Arrays.copyOfRange(atom, 0, 3));
        ac3Bitrate = ac3Bps[ac3BoxBits >> 5 & 0x1f] * 1000;

        stsd.setMaxBitrate(ac3Bitrate);
        stsd.setAvgBitrate(ac3Bitrate);
    }

    private void parseESDS(StsdAtom stsd, byte[] atom) throws InvalidAtomException {

        byte[] esStart = new byte[] { (byte) 0x80, (byte) 0x80, (byte) 0x80 };
        byte[] esEnd   = new byte[] { (byte) 0xFE, (byte) 0xFE, (byte) 0xFE };
        byte[] esTag;

        int pointer;
        int ESLength;

        final int avgBitrate;
        final int maxBitrate;
        final int esObjType;
        final int streamType;
        final int bufferSize;

        pointer = 4;
        if (atom[pointer++] != 0x03) {
            throw new InvalidAtomException("Invalid ES descriptor tag");
        }

        esTag = Arrays.copyOfRange(atom, pointer, pointer += 3);
        if (!Arrays.equals(esTag, esStart) && !Arrays.equals(esTag, esEnd)) {
            System.err.println("Missing ES start tag");
            pointer -= 3;
        }

        ESLength = Byte.toUnsignedInt(atom[pointer++]);
        pointer += 3;

        if (atom[pointer++] != 0x04) {
            throw new IllegalStateException("Invalid ES descriptor tag");
        }

        esTag = Arrays.copyOfRange(atom, pointer, pointer += 3);
        if (!Arrays.equals(esTag, esStart) && Arrays.equals(esTag, esEnd)) {
            System.err.println("Missing ES tag");
            pointer -= 3;
        }

        ESLength   = Byte.toUnsignedInt(atom[pointer++]);
        esObjType  = Byte.toUnsignedInt(atom[pointer++]);
        streamType = Byte.toUnsignedInt(atom[pointer++]);
        bufferSize = toUInt24BE(Arrays.copyOfRange(atom, pointer, pointer += 3));
        maxBitrate = toUInt32BE(Arrays.copyOfRange(atom, pointer, pointer += 4));
        avgBitrate = toUInt32BE(Arrays.copyOfRange(atom, pointer, pointer += 4));

        stsd.setEsObjectType(esObjType);
        stsd.setEsStreamType(streamType);
        stsd.setBufferSize(bufferSize);
        stsd.setMaxBitrate(maxBitrate);
        stsd.setAvgBitrate(avgBitrate);
    }

    private StsdAtom parseStsdAtom(byte[] atom) throws InvalidAtomException {

        final int descCount;
        final int channelCount;
        final int sampleSize;
        final int samplingRate;
        byte[] specBox;

        String aSampleEntryType;
        String specBoxType;

        int size;
        int pointer;

        pointer = 12; // Skip header + 4 hex bytes
        descCount = toUInt32BE(Arrays.copyOfRange(atom, pointer, pointer += 4));
        if (descCount == 0) {
            throw new InvalidAtomException("Invalid number of descriptions");
        }

        pointer += 4; // skip description size
        aSampleEntryType = new String(Arrays.copyOfRange(atom, pointer, pointer += 4));
        pointer += 6; // Reserved [6] uint8_t

        if (!aSampleEntryType.equals(AS_ENTRY_MP4A) &&
            !aSampleEntryType.equals(AS_ENTRY_AC3) &&
            !aSampleEntryType.equals(AS_ENTRY_ALAC))
        {
            return new StsdAtom("stsd", atom);
        }

        pointer += 10; // Reserved [2] 32 uimsbf + skip dref index
        channelCount = Short.toUnsignedInt(toUInt16BE(Arrays.copyOfRange(atom, pointer, pointer += 2)));
        sampleSize   = Short.toUnsignedInt(toUInt16BE(Arrays.copyOfRange(atom, pointer, pointer += 2)));
        pointer += 4; // Reserved 32 uimsbf
        samplingRate = toUInt32BE(Arrays.copyOfRange(atom, pointer, pointer += 4)) >> 16 & 0xffff;

        size = toUInt32BE(Arrays.copyOfRange(atom, pointer, pointer += 4));
        specBoxType = new String(Arrays.copyOfRange(atom, pointer, pointer += 4));
        specBox = Arrays.copyOfRange(atom, pointer, pointer + (size - 8));

        StsdAtom stsdAtom = new StsdAtom("stsd", atom);
        stsdAtom.setCodec(aSampleEntryType);
        stsdAtom.setChannels(channelCount);
        stsdAtom.setSampleSize(sampleSize);
        stsdAtom.setSampleRate(samplingRate);

        switch (specBoxType) {
            case SPEC_BOX_ALAC:
                parseALAC(stsdAtom, specBox);
                break;
            case SPEC_BOX_AC3:
                parseAC3(stsdAtom, specBox);
                break;
            case SPEC_BOX_MP4A:
                parseESDS(stsdAtom, specBox);
                break;
        }
        return stsdAtom;
    }

    private FreeFormAtom parseFreeFormAtom(byte[] atom) throws InvalidAtomException {

        int length;
        int index;
        final int dataOffset;

        String atomType;
        String mean;
        String name;

        index    = 8;
        length   = toUInt32BE(Arrays.copyOfRange(atom, index, index += 4));
        atomType = new String(Arrays.copyOfRange(atom, index, index += 4));

        if (length < 8) {
            throw new InvalidAtomException("Invalid length for 'mean' atom");
        }
        if (!atomType.equals("mean")) {
            throw new InvalidAtomException("Expected atom type: 'mean'");
        }

        mean     = new String(Arrays.copyOfRange(atom, index + 4, index + length - 8)); index += length - 8;
        length   = toUInt32BE(Arrays.copyOfRange(atom, index, index += 4));
        atomType = new String(Arrays.copyOfRange(atom, index, index += 4));

        if (length < 8) {
            throw new InvalidAtomException("Invalid length for 'name' atom");
        }
        if (!atomType.equals("name")) {
            throw new InvalidAtomException("Expected atom type: 'name'");
        }

        name       = new String(Arrays.copyOfRange(atom, index + 4, index + length - 8)); index += length - 8;
        dataOffset = index;
        length     = toUInt32BE(Arrays.copyOfRange(atom, index, index += 4));
        atomType   = new String(Arrays.copyOfRange(atom, index, index += 4));

        if (length < 8) {
            throw new InvalidAtomException("Invalid length for 'data' atom");
        }
        if (!atomType.equals("data")) {
            throw new InvalidAtomException("Expected type 'data', got " + atomType);
        }

        byte[] atomData = Arrays.copyOfRange(atom, index + 8, index + length - 8);
        int dataType    = toUInt32BE(Arrays.copyOfRange(atom, index, index + 4));

        FreeFormAtom freeFormAtom = new FreeFormAtom(mean, name, atom, dataType, dataOffset);
        freeFormAtom.setAtomData(atomData);
        return freeFormAtom;
    }

    private StcoAtom parseStco(byte[] atom) {

        StcoAtom stco  = new StcoAtom("stco", atom);
        int entryCount = toUInt32BE(Arrays.copyOfRange(atom, 12, 16));
        int[] offsets  = new int[entryCount];
        byte[] entries = Arrays.copyOfRange(atom, 16, atom.length);

        for (int i = 0; i < entries.length; i += 4) {
            offsets[i / 4] = toUInt32BE(Arrays.copyOfRange(entries, i, i + 4));
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
        size         = toUInt32BE(sizeBytes);
        dataType     = toUInt32BE(Arrays.copyOfRange(atom, 16, 20));

        if (size < 8) {
            throw new InvalidAtomException(String.format("Atom %s: invalid size: %d", type, size));
        }
        if (!dataAtomType.equals("data")) {
            throw new InvalidAtomException("Unexpected atom: " + dataAtomType);
        }

        ItunesAtom itunesAtom = null;
        if (MP4.BYTE_ATOMS.contains(type))     itunesAtom = new NumberAtom(type, atom);
        if (MP4.INT_ATOMS.contains(type))      itunesAtom = new NumberAtom(type, atom);
        if (MP4.INT_PAIR_ATOMS.contains(type)) itunesAtom = new TrackNumberAtom(type, atom, dataType);

        if (itunesAtom == null) {
            switch (dataType) {

                case TYPE_IMPLICIT:
                    itunesAtom = new TrackNumberAtom(type, atom, dataType);
                    break;
                case TYPE_INTEGER:
                    itunesAtom = new NumberAtom(type, atom);
                    break;

                case TYPE_UTF8:
                case TYPE_UTF16:
                    itunesAtom = new TextAtom(type, atom, dataType);
                    break;

                case TYPE_JPEG:
                case TYPE_BMP:
                case TYPE_PNG:
                    itunesAtom = new PictureAtom(type, atom, dataType);
                    break;

                default:
                    itunesAtom = new UnknownAtom(type, atom, dataType);
                    break;
            }
        }
        itunesAtom.setAtomData(Arrays.copyOfRange(atom, 24, 8 + size));
        return itunesAtom;
    }

    private void parseAtom(
            MP4Atom parentAtom,
            byte[] childAtom,
            boolean hasVersionInt) throws InvalidAtomException
    {
        int index = hasVersionInt ? 12 : 8;
        globalOffset += index;
        while (index < childAtom.length) {

            final String atomType;
            final int startOffset = index;
            final int endOffset;
            final int atomSize;

            byte[] sizeBytes = Arrays.copyOfRange(childAtom, index, index += 4);
            byte[] typeBytes = Arrays.copyOfRange(childAtom, index, index + 4);

            atomSize  = toUInt32BE(sizeBytes);
            endOffset = startOffset + atomSize;
            atomType  = new String(typeBytes, ISO_8859_1);

            if (atomSize < 8) {
                throw new InvalidAtomException("Invalid atom size: " + atomSize);
            }

            byte[] atomData = Arrays.copyOfRange(childAtom, startOffset, endOffset);
            MP4Atom atom = new MP4Atom(atomType, atomData);

            boolean isContainer = false;
            if (parentAtom.getType().equals("ilst") && !atom.getType().equals("----")) {

                atom = parseItunesAtom(atomData);
                atom.setAtomStart(globalOffset);
                atom.setAtomEnd(globalOffset + atomSize);

                atom.setParentAtom(parentAtom);
                parentAtom.appendChildAtom(atom);
            }
            else {
                switch (atomType) {

                    case "----":
                        atom = parseFreeFormAtom(atomData);
                        break;
                    case "stco":
                        atom = parseStco(atomData);
                        break;
                    case "stsd":
                        atom = parseStsdAtom(atomData);
                        break;
                    case "mdhd":
                        atom = parseMdhdAtom(atomData);
                        break;

                    case "moov":
                    case "moof":
                    case "ilst":
                    case "traf":
                    case "udta":
                    case "meta":
                    case "trak":
                    case "mdia":
                    case "minf":
                    case "dinf":
                    case "stbl":
                        isContainer = true;
                }

                atom.setAtomStart(globalOffset);
                atom.setAtomEnd(globalOffset + atomSize);

                atom.setParentAtom(parentAtom);
                parentAtom.appendChildAtom(atom);

                if (isContainer) {
                    parseAtom(
                            atom,
                            atomData,
                            atomType.equals("meta")
                    );
                }
            }
            if (!isContainer) {
                globalOffset += atomSize;
            }
            index = endOffset;
        }
    }

    @Override
    public MP4 parseTag(RandomAccessFile file) {
        try {

            MP4Atom mp4Atom;
            ArrayList<MP4Atom> atoms = new ArrayList<>();
            file.seek(0);

            boolean isFragmented = false;
            while (file.getFilePointer() < file.length()) {

                byte[] sizeBytes = new byte[4];
                byte[] typeBytes = new byte[4];

                if (file.read(sizeBytes, 0, 4) == -1) break;
                if (file.read(typeBytes, 0, 4) == -1) break;

                int atomSize    = toUInt32BE(sizeBytes);
                String atomType = new String(typeBytes, ISO_8859_1);

                if (atomSize < 8) {
                    System.err.println("MP4Parser: " + atomType + " has invalid size");
                    break;
                }

                if (!atomType.equals("moov") && !atomType.equals("moof")) {
                    mp4Atom = new MP4Atom(atomType, new byte[0]);
                    mp4Atom.setAtomStart(file.getFilePointer() - 8);
                    mp4Atom.setAtomEnd(mp4Atom.getAtomStart() + atomSize);
                    atoms.add(mp4Atom);
                    file.skipBytes(atomSize - 8);
                } else {

                    final long atomStart;
                    final long atomEnd;

                    atomStart    = file.getFilePointer() - 8;
                    globalOffset = atomStart;

                    byte[] atomData = new byte[atomSize];
                    System.arraycopy(sizeBytes, 0, atomData, 0, 4);
                    System.arraycopy(typeBytes, 0, atomData, 4, 4);

                    file.read(atomData, 8, atomSize - 8);
                    atomEnd = file.getFilePointer();

                    mp4Atom = new MP4Atom(atomType, atomData);
                    mp4Atom.setAtomStart(atomStart);
                    mp4Atom.setAtomEnd(atomEnd);
                    atoms.add(mp4Atom);

                    if (atomType.equals("moof")) {
                        isFragmented = true;
                    }
                    parseAtom(mp4Atom, atomData, false);
                }
            }

            this.mp4 = new MP4(atoms, isFragmented);
            return mp4;

        } catch (IOException | InvalidAtomException e) {
            System.err.println("Exception in MP4Parser: " + e.getMessage());
            return null;
        }
    }

    @Override
    public MP4 parseStreamInfo(RandomAccessFile file) {
        return mp4;
    }
}
