package com.jtagger.mp4;

import com.jtagger.utils.IntegerUtils;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class TrackNumberAtom extends MP4Atom implements ItunesAtom<TrackNumber> {

    private TrackNumber trackNumber;
    private int atomType;

    public TrackNumberAtom(String type, byte[] atomBytes, int atomType) {
        super(type, atomBytes);
        this.atomType = atomType;
    }

    public TrackNumberAtom(String type) {
        super(type);
    }

    @Override
    public byte[] assemble(byte version) {

        int index       = 0;
        byte[] intBytes = getType().equals("trkn") ? new byte[8] : new byte[6];

        byte[] dataAtom   = new byte[8 + 8 + intBytes.length];
        byte[] itunesAtom = new byte[dataAtom.length + 8];

        System.arraycopy(IntegerUtils.fromUInt16BE(trackNumber.trackPos), 0, intBytes, 2, 2);
        System.arraycopy(IntegerUtils.fromUInt16BE(trackNumber.totalTracks), 0, intBytes, 4, 2);
        System.arraycopy(IntegerUtils.fromUInt32BE(dataAtom.length), 0, dataAtom, 0, 4); index += 4;
        System.arraycopy("data".getBytes(ISO_8859_1), 0, dataAtom, index, 4); index += 4;

        System.arraycopy(IntegerUtils.fromUInt32BE(TYPE_IMPLICIT), 0, dataAtom, index, 4); index += 4; // FLAG_BINARY
        System.arraycopy(IntegerUtils.fromUInt32BE(0x000000), 0, dataAtom, index, 4); index += 4;
        System.arraycopy(intBytes, 0, dataAtom, index, intBytes.length);

        System.arraycopy(IntegerUtils.fromUInt32BE(itunesAtom.length), 0, itunesAtom, 0, 4);
        System.arraycopy(getType().getBytes(ISO_8859_1), 0, itunesAtom, 4, 4);
        System.arraycopy(dataAtom, 0, itunesAtom, 8, dataAtom.length);

        this.data = itunesAtom;
        return itunesAtom;
    }

    @Override
    public TrackNumber getAtomData() {
        return trackNumber;
    }

    @Override
    public int getAtomType() {
        return atomType;
    }

    @Override
    public void setAtomData(byte[] data) {
        int posNum   = IntegerUtils.toUInt16BE(Arrays.copyOfRange(data, 2, 4));
        int trackNum = IntegerUtils.toUInt16BE(Arrays.copyOfRange(data, 4, 6));
        this.trackNumber = new TrackNumber(posNum, trackNum);
    }

    @Override
    public void setAtomData(TrackNumber data) {
        this.trackNumber = data;
    }
}
