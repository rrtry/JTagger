package com.jtagger.mp4;

import com.jtagger.AbstractTag;
import com.jtagger.AttachedPicture;
import com.jtagger.StreamInfo;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MP4 extends AbstractTag implements StreamInfo {

    private final ArrayList<MP4Atom> atoms;
    private byte[] bytes;

    private final int mdatStart;
    private final int mdatEnd;

    private StsdAtom stsdAtom;
    private MdhdAtom mdhdAtom;
    private MP4Atom ilstAtom;
    private MP4Atom moovAtom;

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
    public static final String ID3_GENRE         = "gnre";
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

    public static final List<String> INT_PAIR_ATOMS = Arrays.asList("trkn", "disk");
    public static final List<String> BYTE_ATOMS     = Arrays.asList("cpil", "pgap", "pcst", "hdvd", "shwm", "stik", "rtng", "akID");
    public static final List<String> INT_ATOMS      = Arrays.asList("tmpo", "©mvi", "©mvc", "tvsn", "tves", "cnID", "sfID", "atID", "geID", "cmID", "gnre");

    static {
        FIELD_MAP.put(AbstractTag.TITLE            ,TITLE);
        FIELD_MAP.put(AbstractTag.ARTIST           ,ARTIST);
        FIELD_MAP.put(AbstractTag.ALBUM            ,ALBUM);
        FIELD_MAP.put(AbstractTag.COMMENT          ,COMMENT);
        FIELD_MAP.put(AbstractTag.YEAR             ,DATE);
        FIELD_MAP.put(AbstractTag.TRACK_NUMBER     ,TRACK_NUMBER);
        FIELD_MAP.put(AbstractTag.GENRE            ,GENRE);
        FIELD_MAP.put(AbstractTag.ID3_GENRE        ,ID3_GENRE);
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

    public MP4(ArrayList<MP4Atom> atoms, int mdatStart, int mdatEnd) {
        this.atoms       = atoms;
        this.mdatStart   = mdatStart;
        this.mdatEnd     = mdatEnd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MP4Atom atom : atoms) {
            sb.append(atom.toString()).append("\n");
            if (atom.hasChildAtoms()) {
                iterateOverAtomTree(sb, atom, 1);
            }
        }
        return sb.toString();
    }

    private static void iterateOverAtomTree(StringBuilder sb, MP4Atom parent, int depth) {
        for (MP4Atom atom : parent.getChildAtoms()) {
            sb.append("---".repeat(depth)).append(atom.toString()).append("\n");
            if (atom.hasChildAtoms()) {
                iterateOverAtomTree(sb, atom, depth + 1);
            }
        }
    }

    private MP4Atom getMoovAtom() {
        if (moovAtom != null) {
            return moovAtom;
        }
        for (MP4Atom atom : atoms) {
            if (atom.getType().equals("moov")) {
                moovAtom = atom;
                return atom;
            }
        }
        throw new IllegalStateException("MP4: moov atom is missing");
    }

    private MP4Atom findMetadataAtom(String type, MP4Atom currentAtom) {
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

    private MP4Atom getIlstAtom() {
        if (ilstAtom != null) {
            return ilstAtom;
        }
        ilstAtom = findMetadataAtom("ilst", getMoovAtom());
        if (ilstAtom == null) throw new IllegalStateException("MP4: ilst atom is missing");
        return ilstAtom;
    }

    public ArrayList<ItunesAtom> getMetadataAtoms() {
        return getIlstAtom().getChildAtoms();
    }

    public <T extends ItunesAtom> T getMetadataAtom(String type) {
        return (T) findMetadataAtom(type, getIlstAtom());
    }

    public void addMetadataAtom(ItunesAtom atom) {
        getIlstAtom().appendChildAtom(atom);
    }

    public void setMetadataAtoms(ArrayList<ItunesAtom> metadataAtoms) {
        getIlstAtom().setChildAtoms(metadataAtoms);
    }

    public void removeMetadataAtom(String type) {

        ArrayList<ItunesAtom> atoms = getMetadataAtoms();
        ItunesAtom atom = null;

        for (ItunesAtom childAtom : atoms) {
            if (childAtom.getType().equals(type)) {
                atom = childAtom;
                break;
            }
        }
        if (atom != null) {
            atoms.remove(atom);
        }
    }

    public void removeMetadataAtoms() {
        getIlstAtom().removeAllChildAtoms();
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    protected <T> void setFieldValue(String fieldId, T value) {

        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return;

        boolean addAtom       = true;
        ItunesAtom itunesAtom = null;

        ArrayList<ItunesAtom> itunesAtoms = getIlstAtom().getChildAtoms();
        for (ItunesAtom atom : itunesAtoms) {
            if (atom.getType().equals(atomType)) {
                addAtom    = false;
                itunesAtom = atom;
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
            short trackNum    = ints.length == 2 ? Short.parseShort(ints[0]) : Short.parseShort(string);
            short totalTracks = ints.length == 2 ? Short.parseShort(ints[1]) : 0;

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

        itunesAtom.assemble();
        if (addAtom) {
            ilstAtom.appendChildAtom(itunesAtom);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> T getFieldValue(String fieldId) {

        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return null;

        ArrayList<ItunesAtom> itunesAtoms = getIlstAtom().getChildAtoms();
        boolean isCover = fieldId.equals(AbstractTag.PICTURE);

        for (ItunesAtom atom : itunesAtoms) {
            if (atom.getType().equals(atomType)) {
                return isCover ? (T) atom.getAtomData() :
                        (T) atom.getAtomData().toString();
            }
        }
        return null;
    }

    @Override
    public void removeField(String fieldId) {
        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return;
        removeMetadataAtom(atomType);
    }

    private int assembleAtoms() {

        int size = 0;
        for (MP4Atom atom : atoms) {
            size += atom.getType().equals("moov") ? atom.assemble().length : atom.getData().length;
        }

        return size;
    }

    // TODO: make usage of 'free' atom
    @Override
    public byte[] assemble(byte version) {

        int size  = assembleAtoms();
        int delta = size - mdatStart;

        MP4Atom moovAtom  = getMoovAtom();
        StcoAtom stcoAtom = (StcoAtom) findMetadataAtom("stco", moovAtom);
        stcoAtom.updateOffsets(delta);

        size  = assembleAtoms();
        bytes = new byte[size];

        int i = 0;
        for (MP4Atom atom : atoms) {
            System.arraycopy(atom.getData(), 0, bytes, i, atom.getData().length);
            i += atom.getData().length;
        }
        return bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    int getMdatStart() {
        return mdatStart;
    }

    int getMdatEnd() {
        return mdatEnd;
    }

    @Override
    public byte getChannelCount() {
        if (stsdAtom == null) stsdAtom = (StsdAtom) findMetadataAtom("stsd", getMoovAtom());
        return (byte) stsdAtom.getChannels();
    }

    @Override
    public int getDuration() {
        if (mdhdAtom == null) mdhdAtom = (MdhdAtom) findMetadataAtom("mdhd", getMoovAtom());
        return (int) (mdhdAtom.getDuration() / mdhdAtom.getTimescale());
    }

    @Override
    public int getBitrate() {
        if (stsdAtom == null) stsdAtom = (StsdAtom) findMetadataAtom("stsd", getMoovAtom());
        return stsdAtom.getAvgBitrate() / 1000;
    }

    @Override
    public int getSampleRate() {
        if (stsdAtom == null) stsdAtom = (StsdAtom) findMetadataAtom("stsd", getMoovAtom());
        return stsdAtom.getSampleRate();
    }

    @Override
    public void setDuration(int duration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setBitrate(int bitrate) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
