package com.jtagger.mp4;

import com.jtagger.AbstractTag;
import com.jtagger.AttachedPicture;
import com.jtagger.StreamInfo;

import java.util.*;

public class MP4 extends AbstractTag implements StreamInfo {

    private final ArrayList<MP4Atom> atoms;
    private final int initialSize;
    private byte[] bytes;

    private byte channels;
    private int bitRate;
    private int sampleRate;
    private int duration;

    public static String[] ATOMS = new String[] {
            "moov", "udta", "meta", "ilst", "trak", "mdia",
            "minf", "dinf", "stbl"
    };

    public static final String TITLE             = "©nam";
    public static final String WORK              = "©wrk";
    public static final String SORT_ALBUM        = "soal";
    public static final String SORT_ALBUM_ARTIST = "soaa";
    public static final String SORT_ARTIST       = "soar";
    public static final String SORT_COMPOSER     = "soco";
    public static final String SORT_NAME         = "sonm";
    public static final String RATING            = "rate";
    public static final String DESCRIPTION       = "desc";
    public static final String COVER             = "covr";
    public static final String ARTIST            = "©ART";
    public static final String ALBUM             = "©alb";
    public static final String COMMENT           = "©cmt";
    public static final String GENRE             = "©gen";
    public static final String DATE              = "©day";
    public static final String WRITER            = "©wrt";
    public static final String GROUPING          = "©grp";
    public static final String ALBUM_ARTIST      = "aART";
    public static final String TRACK_NUMBER      = "trkn";
    public static final String DISC_NUMBER       = "disk";
    public static final String COMPILATION       = "cpil";
    public static final String BPM               = "tmpo";
    public static final String COPYRIGHT         = "©cpy";
    public static final String LYRICS            = "©lyr";
    public static final String ENCODED_BY        = "©enc";
    public static final String ENCODER           = "©too";
    public static final String ARRANGER          = "©arg";
    public static final String COMPOSER          = "©com";
    public static final String DIRECTOR          = "©dir";
    public static final String FORMAT            = "©fmt";
    public static final String INFO              = "©inf";
    public static final String ISRC              = "©isr";
    public static final String LABEL             = "©lab";
    public static final String LABEL_URL         = "©lal";
    public static final String CREATOR           = "©mak";
    public static final String REC_COPYRIGHT     = "©phg";
    public static final String PRODUCER          = "©prd";
    public static final String PERFORMERS        = "©prf";
    public static final String ARTIST_URL        = "©prl";
    public static final String REQUIREMENTS      = "©req";
    public static final String SUBTITLE          = "©snm";
    public static final String SOURCE            = "©src";
    public static final String SONGWRITER        = "©swf";
    public static final String SOFTWARE          = "©swr";

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>();

    private static final List<String> INT_PAIR_ATOMS = Arrays.asList("trkn", "disk");
    private static final List<String> BYTE_ATOMS     = Arrays.asList("cpil", "pgap", "pcst", "hdvd", "shwm", "stik", "rtng", "akID");
    public static final List<String> INT_ATOMS       = Arrays.asList("tmpo", "©mvi", "©mvc", "tvsn", "tves", "cnID", "sfID", "atID", "geID", "cmID");

    static {
        FIELD_MAP.put(AbstractTag.TITLE            ,TITLE);
        FIELD_MAP.put(AbstractTag.ARTIST           ,ARTIST);
        FIELD_MAP.put(AbstractTag.ALBUM            ,ALBUM);
        FIELD_MAP.put(AbstractTag.COMMENT          ,COMMENT);
        FIELD_MAP.put(AbstractTag.YEAR             ,DATE);
        FIELD_MAP.put(AbstractTag.TRACK_NUMBER     ,TRACK_NUMBER);
        FIELD_MAP.put(AbstractTag.GENRE            ,GENRE);
        FIELD_MAP.put(AbstractTag.ALBUM_ARTIST     ,ALBUM_ARTIST);
        FIELD_MAP.put(AbstractTag.ARRANGER         ,ARRANGER);
        FIELD_MAP.put(AbstractTag.BPM              ,BPM);
        FIELD_MAP.put(AbstractTag.COMPILATION      ,COMPILATION);
        FIELD_MAP.put(AbstractTag.COMPOSER         ,WRITER);
        FIELD_MAP.put(AbstractTag.COPYRIGHT        ,COPYRIGHT);
        FIELD_MAP.put(AbstractTag.DESCRIPTION      ,DESCRIPTION);
        FIELD_MAP.put(AbstractTag.DISC_NUMBER      ,DISC_NUMBER);
        FIELD_MAP.put(AbstractTag.ENCODED_BY       ,ENCODED_BY);
        FIELD_MAP.put(AbstractTag.ENCODER_SETTINGS ,ENCODER);
        FIELD_MAP.put(AbstractTag.GROUPING         ,GROUPING);
        FIELD_MAP.put(AbstractTag.ISRC             ,ISRC);
        FIELD_MAP.put(AbstractTag.LYRICIST         ,SONGWRITER);
        FIELD_MAP.put(AbstractTag.LYRICS           ,LYRICS);
        FIELD_MAP.put(AbstractTag.PICTURE          ,COVER);
        FIELD_MAP.put(AbstractTag.RATING           ,RATING);
        FIELD_MAP.put(AbstractTag.SORT_ALBUM       ,SORT_ALBUM);
        FIELD_MAP.put(AbstractTag.SORT_ALBUM_ARTIST,SORT_ALBUM_ARTIST);
        FIELD_MAP.put(AbstractTag.SORT_ARTIST      ,SORT_ARTIST);
        FIELD_MAP.put(AbstractTag.SORT_COMPOSER    ,SORT_COMPOSER);
        FIELD_MAP.put(AbstractTag.SORT_NAME        ,SORT_NAME);
        FIELD_MAP.put(AbstractTag.SUBTITLE         ,SUBTITLE);
        FIELD_MAP.put(AbstractTag.WORK             ,WORK);
    }

    public MP4(ArrayList<MP4Atom> atoms, int initialSize) {
        this.initialSize = initialSize;
        this.atoms       = atoms;
    }

    public MP4Atom findMetadataAtom(String type, MP4Atom currentAtom) {
        for (MP4Atom atom : currentAtom.getChildAtoms()) {
            if (atom.getType().equals(type)) {
                return atom;
            } else if (atom.hasChildAtoms()) {
                MP4Atom childAtom = findMetadataAtom(type, atom);
                if (childAtom == null) continue; else return childAtom;
            }
        }
        return null;
    }

    public ArrayList<MP4Atom> getMetadataAtoms() {
        return findMetadataAtom("ilst", atoms.get(1)).getChildAtoms();
    }

    public void setMetadataAtoms(ArrayList<MP4Atom> metadataAtoms) {
        MP4Atom ilstAtom = findMetadataAtom("ilst", atoms.get(1));
        ilstAtom.setChildAtoms(metadataAtoms);
    }

    public void removeMetadataAtoms() {
        MP4Atom ilstAtom = findMetadataAtom("ilst", atoms.get(1));
        ilstAtom.removeAllChildAtoms();
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    protected <T> void setFieldValue(String fieldId, T value) {

        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return;

        MP4Atom ilstAtom = findMetadataAtom("ilst", atoms.get(1));
        ArrayList<MP4Atom> itunesAtoms = ilstAtom.getChildAtoms();

        boolean addAtom       = true;
        ItunesAtom itunesAtom = null;

        for (MP4Atom atom : itunesAtoms) {
            if (atom.getType().equals(atomType)) {
                addAtom    = false;
                itunesAtom = (ItunesAtom) atom;
                break;
            }
        }

        if (atomType.equals(COVER)) {

            PictureAtom pictureAtom = addAtom ? new PictureAtom(atomType) : (PictureAtom) itunesAtom;
            pictureAtom.setAtomData((AttachedPicture) value);
            pictureAtom.assemble();

            if (addAtom) {
                ilstAtom.appendChildAtom(pictureAtom);
            }
            return;
        }

        assert value instanceof String;
        String string = (String) value;

        boolean isByteAtom    = BYTE_ATOMS.contains(atomType);
        boolean isIntAtom     = INT_ATOMS.contains(atomType);
        boolean isIntPairAtom = INT_PAIR_ATOMS.contains(atomType);

        if (isIntPairAtom) {

            String[] ints     = string.split("/");
            short trackNum    = Short.parseShort(ints[0]);
            short totalTracks = Short.parseShort(ints[1]);

            TrackNumberAtom trackNumberAtom = addAtom ? new TrackNumberAtom(atomType) : (TrackNumberAtom) itunesAtom;
            trackNumberAtom.setAtomData(new TrackNumber(trackNum, totalTracks));
            itunesAtom = trackNumberAtom;
        }
        else if (isByteAtom || isIntAtom) {
            NumberAtom numberAtom = addAtom ? new NumberAtom(atomType) : (NumberAtom) itunesAtom;
            numberAtom.setAtomData(isByteAtom ? Byte.parseByte(string) : Integer.parseInt(string));
            numberAtom.setNumberLength((byte) (isByteAtom ? Byte.BYTES : Integer.BYTES));
            itunesAtom = numberAtom;
        }
        else {
            TextAtom textAtom = addAtom ? new TextAtom(atomType) : (TextAtom) itunesAtom;
            textAtom.setAtomData(string);
            itunesAtom = textAtom;
        }

        ((MP4Atom) itunesAtom).assemble();
        if (addAtom) {
            ilstAtom.appendChildAtom((MP4Atom) itunesAtom);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> T getFieldValue(String fieldId) {

        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return null;

        MP4Atom ilstAtom = findMetadataAtom("ilst", atoms.get(1));
        ArrayList<MP4Atom> itunesAtoms = ilstAtom.getChildAtoms();

        boolean isCover = fieldId.equals(AbstractTag.PICTURE);
        for (MP4Atom atom : itunesAtoms) {
            if (atom.getType().equals(atomType)) {
                return isCover ? (T) ((ItunesAtom) atom).getAtomData() :
                        (T) ((ItunesAtom) atom).getAtomData().toString();
            }
        }
        return null;
    }

    @Override
    public void removeField(String fieldId) {

        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return;

        MP4Atom ilstAtom = findMetadataAtom("ilst", atoms.get(1));
        ArrayList<MP4Atom> itunesAtoms = ilstAtom.getChildAtoms();
        Iterator<MP4Atom> iterator     = itunesAtoms.iterator();

        MP4Atom atom;
        while (iterator.hasNext()) {
            atom = iterator.next();
            if (atom.getType().equals(atomType)) {
                itunesAtoms.remove(atom);
                break;
            }
        }
    }

    // TODO: make usage of 'free' atom
    @Override
    public byte[] assemble(byte version) {

        MP4Atom ftypAtom = atoms.get(0);
        MP4Atom moovAtom = atoms.get(1);

        moovAtom.assemble();
        final int size  = ftypAtom.getData().length + moovAtom.getData().length;
        final int delta = size - initialSize;

        StcoAtom stcoAtom = (StcoAtom) findMetadataAtom("stco", moovAtom);
        stcoAtom.updateOffsets(delta);

        moovAtom.assemble();
        final int ftypLength = ftypAtom.getData().length;
        final int moovLength = moovAtom.getData().length;

        bytes = new byte[size];
        System.arraycopy(ftypAtom.getData(), 0, bytes, 0, ftypLength);
        System.arraycopy(moovAtom.getData(), 0, bytes, ftypLength, moovLength);
        return bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    public void addAtom(MP4Atom atom) {
        if (!atoms.contains(atom)) atoms.add(atom);
    }

    public ArrayList<MP4Atom> getAtoms() {
        return atoms;
    }

    public int getInitialSize() {
        return initialSize;
    }

    @Override
    public byte getChannelCount() {
        return channels;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public int getBitrate() {
        return bitRate;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    // TODO: implement
    @Override
    public void setDuration(int duration) {
        throw new IllegalStateException();
    }

    // TODO: implement
    @Override
    public void setBitrate(int bitrate) {
        throw new IllegalStateException();
    }
}
