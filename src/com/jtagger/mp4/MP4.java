package com.jtagger.mp4;

import com.jtagger.AbstractTag;
import com.jtagger.AttachedPicture;
import com.jtagger.StreamInfo;
import com.jtagger.mp3.id3.GenreFrame;

import java.util.*;

import static com.jtagger.mp3.id3.ID3V1Tag.UNKNOWN;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MP4 extends AbstractTag implements StreamInfo {

    private final ArrayList<MP4Atom> atoms;
    private byte[] bytes;

    private StsdAtom stsdAtom;
    private MdhdAtom mdhdAtom;
    private MP4Atom ilstAtom;
    private MP4Atom moovAtom;

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

    public static final List<String> DUPLICATES     = List.of("mdat", "moof");
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

    public MP4(ArrayList<MP4Atom> atoms) {
        this.atoms = atoms;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MP4Atom atom : atoms) {
            sb.append(atom.toString()).append("\n");
            if (atom.hasChildren()) {
                iterateOverAtomTree(sb, atom, 1);
            }
        }
        return sb.toString();
    }

    private static void iterateOverAtomTree(StringBuilder sb, MP4Atom parent, int depth) {
        for (MP4Atom atom : parent.getChildren()) {
            sb.append("  ".repeat(depth)).append(" ")
                    .append(String.format("Atom %s @ %d of size: %d ends @ %d",
                            atom.getType(),
                            atom.getStart(),
                            atom.getSize(),
                            atom.getEnd()))
                    .append(" ↑ ")
                    .append(atom.getParent().getType())
                    .append("\n");
            if (atom.hasChildren()) {
                iterateOverAtomTree(sb, atom, depth + 1);
            }
        }
    }

    ArrayList<MP4Atom> getAtoms() {
        return atoms;
    }

    void findAtoms(MP4Atom parent, ArrayList<MP4Atom> atoms, String...atomPath) {
        for (MP4Atom atom : parent.getChildren()) {
            if (atom.getType().equals(atomPath[atoms.size()])) {

                boolean hasChildren = atom.hasChildren();
                boolean isLastAtom  = atoms.size() == atomPath.length - 1;
                atoms.add(atom);

                if (isLastAtom) {
                    return;
                }
                if (hasChildren) {
                    findAtoms(atom, atoms, atomPath);
                    break;
                }
            }
        }
    }

    MP4Atom findAtom(String type, MP4Atom currentAtom) {
        for (MP4Atom atom : currentAtom.getChildren()) {
            if (atom.getType().equals(type)) {
                return atom;
            }
            else if (atom.hasChildren()) {
                MP4Atom childAtom = findAtom(type, atom);
                if (childAtom == null) continue; else return childAtom;
            }
        }
        return null;
    }

    MP4Atom getMoovAtom() {

        if (moovAtom != null) {
            return moovAtom;
        }

        for (MP4Atom atom : atoms) {
            if (atom.getType().equals("moov")) {
                moovAtom = atom;
                return moovAtom;
            }
        }
        throw new IllegalStateException("MP4: moov atom is missing");
    }

    MP4Atom getIlstAtom() {

        if (ilstAtom != null) {
            return ilstAtom;
        }

        ilstAtom = findAtom("ilst", getMoovAtom());
        if (ilstAtom == null) {
            ilstAtom = new MP4Atom("ilst");
        }
        return ilstAtom;
    }

    public ArrayList<ItunesAtom> getMetadataAtoms() {
        return getIlstAtom().getChildren();
    }

    public void addMetadataAtom(ItunesAtom atom) {
        getIlstAtom().appendChild(atom);
    }

    public void setMetadataAtoms(ArrayList<ItunesAtom> metadataAtoms) {
        getIlstAtom().setChildren(metadataAtoms);
    }

    public void removeMetadataAtom(String type) {

        ArrayList<ItunesAtom> metadataAtoms = getMetadataAtoms();
        ItunesAtom atom = null;

        for (ItunesAtom childAtom : metadataAtoms) {
            if (childAtom.getType().equals(type)) {
                atom = childAtom;
                break;
            }
        }
        if (atom != null) {
            metadataAtoms.remove(atom);
        }
    }

    public void removeMetadataAtoms() {
        setMetadataAtoms(new ArrayList<>());
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public <T> void setFieldValue(String fieldId, T value) {

        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return;

        boolean addAtom       = true;
        ItunesAtom itunesAtom = null;

        ArrayList<ItunesAtom> itunesAtoms = getIlstAtom().getChildren();
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
                ilstAtom.appendChild(pictureAtom);
            }
            return;
        }

        assert value instanceof String;
        String string = (String) value;

        boolean isByteAtom    = BYTE_ATOMS.contains(atomType);
        boolean isIntAtom     = INT_ATOMS.contains(atomType);
        boolean isIntPairAtom = INT_PAIR_ATOMS.contains(atomType);

        try {
            if (isIntPairAtom) {

                String[] ints = string.split("/");
                int position = 0;
                int count    = 0;

                if (ints.length == 2) {
                    position = Integer.parseInt(ints[0]);
                    count    = Integer.parseInt(ints[1]);
                }
                else if (ints.length == 1) {
                    position = Integer.parseInt(ints[0]);
                }

                TrackNumberAtom trackNumberAtom = addAtom ? new TrackNumberAtom(atomType) : (TrackNumberAtom) itunesAtom;
                trackNumberAtom.setAtomData(new TrackNumber(position, count));
                itunesAtom = trackNumberAtom;
            }
            else if (isByteAtom || isIntAtom) {

                Number i = null;
                if (atomType.equals(ID3_GENRE)) {
                    ArrayList<String> types = GenreFrame.parseTCON(string);
                    for (String type : types) {
                        if (!type.equals(GenreFrame.COVER) && !type.equals(GenreFrame.REMIX)) {
                            short genre = Short.parseShort(type);
                            if (genre >= 0 && genre < UNKNOWN) {
                                i = genre;
                                break;
                            } else {
                                return;
                            }
                        }
                    }
                }
                NumberAtom numberAtom = addAtom ? new NumberAtom(atomType) : (NumberAtom) itunesAtom;
                numberAtom.setAtomData(i == null ? Short.parseShort(string) : i);
                numberAtom.setNumberLength(isByteAtom ? Byte.BYTES : Short.BYTES);
                itunesAtom = numberAtom;
            }
            else {
                TextAtom textAtom = addAtom ? new TextAtom(atomType) : (TextAtom) itunesAtom;
                textAtom.setAtomData(string);
                itunesAtom = textAtom;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            itunesAtom = null;
        }
        if (itunesAtom != null) {
            itunesAtom.assemble();
            if (addAtom) ilstAtom.appendChild(itunesAtom);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> T getFieldValue(String fieldId) {

        String atomType = FIELD_MAP.get(fieldId);
        if (atomType == null) return null;

        ArrayList<ItunesAtom> itunesAtoms = getIlstAtom().getChildren();
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

    @Override
    public byte[] assemble(byte version) {
        getIlstAtom();
        bytes = ilstAtom.assemble();
        return bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    public String getCodec() {
        if (stsdAtom == null) stsdAtom = (StsdAtom) findAtom("stsd", getMoovAtom());
        return stsdAtom == null ? "" : stsdAtom.getCodec();
    }

    @Override
    public byte getChannelCount() {
        if (stsdAtom == null) stsdAtom = (StsdAtom) findAtom("stsd", getMoovAtom());
        return stsdAtom == null ? 0 : (byte) stsdAtom.getChannels();
    }

    @Override
    public int getDuration() {
        if (mdhdAtom == null) mdhdAtom = (MdhdAtom) findAtom("mdhd", getMoovAtom());
        return mdhdAtom == null ? 0 : (int) (mdhdAtom.getDuration() / mdhdAtom.getTimescale());
    }

    @Override
    public int getBitrate() {
        if (stsdAtom == null) stsdAtom = (StsdAtom) findAtom("stsd", getMoovAtom());
        return stsdAtom == null ? 0 : stsdAtom.getAvgBitrate() / 1000;
    }

    @Override
    public int getSampleRate() {
        if (stsdAtom == null) stsdAtom = (StsdAtom) findAtom("stsd", getMoovAtom());
        return stsdAtom == null ? 0 : stsdAtom.getSampleRate();
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
