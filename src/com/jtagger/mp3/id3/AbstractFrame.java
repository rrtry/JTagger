package com.jtagger.mp3.id3;

import com.jtagger.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import static com.jtagger.mp3.id3.UnsynchronisationUtils.toUnsynch;

public abstract class AbstractFrame<T> implements Component {

    /* ID3v2.3 */
    public static final String EQUALIZATION          = "EQUA";
    public static final String INVOLVED_PEOPLE       = "IPLS";
    public static final String RVAD                  = "RVAD";
    public static final String DATE                  = "TDAT";
    public static final String TIME                  = "TIME";
    public static final String ORIGINAL_RELEASE_YEAR = "TORY";
    public static final String RECORDING_DATES       = "TRDA";
    public static final String SIZE                  = "TSIZ";
    public static final String YEAR                  = "TYER";

    /* ID3v2.4 */
    public static final String CHAPTER                = "CHAP"; // Chapter
    public static final String TABLE_OF_CONTENTS      = "CTOC"; // Table of contents
    public static final String AUDIO_ENCRYPTION       = "AENC"; // Audio encryption
    public static final String PICTURE                = "APIC"; // Attached picture
    public static final String SEEK_POINT_INDEX       = "ASPI"; // Audio seek point index
    public static final String COMMENT                = "COMM"; // Comments
    public static final String COMMERCIAL             = "COMR"; // Commercial frame
    public static final String ENCRYPTION_METHOD      = "ENCR"; // Encryption method registration
    public static final String EQUALISATION           = "EQU2"; // Equalisation (2)
    public static final String EVENT_TIMING_CODES     = "ETCO"; // Event timing codes
    public static final String GEOB                   = "GEOB"; // General encapsulated object
    public static final String GRID                   = "GRID"; // Group identification registration
    public static final String LINK                   = "LINK"; // Linked information
    public static final String MUSIC_CD_ID            = "MCDI"; // Music CD identifier
    public static final String MPEG_LOOKUP_TABLE      = "MLLT"; // MPEG location lookup table
    public static final String OWNERSHIP              = "OWNE"; // Ownership frame
    public static final String PRIVATE                = "PRIV"; // Private frame
    public static final String PLAY_COUNTER           = "PCNT"; // Play counter
    public static final String POPULARIMETER          = "POPM"; // Popularimeter
    public static final String POSITION_SYNC          = "POSS"; // Position synchronisation frame
    public static final String BUFFER_SIZE            = "RBUF"; // Recommended buffer size
    public static final String RVA2                   = "RVA2"; // Relative volume adjustment (2)
    public static final String REVERB                 = "RVRB"; // Reverb
    public static final String SEEK                   = "SEEK"; // Seek frame
    public static final String SIGNATURE              = "SIGN"; // Signature frame
    public static final String S_LYRICS               = "SYLT"; // Synchronised lyric/text
    public static final String SYNC_TEMPO_CODES       = "SYTC"; // Synchronised tempo codes
    public static final String ALBUM                  = "TALB"; // Album/Movie/Show title
    public static final String BPM                    = "TBPM"; // BPM (beats per minute)
    public static final String COMPOSER               = "TCOM"; // Composer
    public static final String GENRE                  = "TCON"; // Content type
    public static final String COPYRIGHT              = "TCOP"; // Copyright message
    public static final String ENCODING_TIME          = "TDEN"; // Encoding time
    public static final String PLAYLIST_DELAY         = "TDLY"; // Playlist delay
    public static final String ORIGINAL_RELEASE_TIME  = "TDOR"; // Original release time
    public static final String RECORDING_TIME         = "TDRC"; // Recording time
    public static final String RELEASE_TIME           = "TDRL"; // Release time
    public static final String TAGGING_TIME           = "TDTG"; // Tagging time
    public static final String ENCODED_BY             = "TENC"; // Encoded by
    public static final String TEXT_WRITER            = "TEXT"; // Lyricist/Text writer
    public static final String FILE_TYPE              = "TFLT"; // File type
    public static final String PEOPLE                 = "TIPL"; // Involved people list
    public static final String GENRE_DESCRIPTION      = "TIT1"; // Content group description
    public static final String TITLE                  = "TIT2"; // Title/songname/content description
    public static final String SUBTITLE               = "TIT3"; // Subtitle/Description refinement
    public static final String INITIAL_KEY            = "TKEY"; // Initial key
    public static final String LANGUAGE               = "TLAN"; // Language(s)
    public static final String LENGTH                 = "TLEN"; // Length
    public static final String CREDITS                = "TMCL"; // Musician credits list
    public static final String MEDIA_TYPE             = "TMED"; // Media type
    public static final String MOOD                   = "TMOO"; // Mood
    public static final String ORIGINAL_ALBUM_TITLE   = "TOAL"; // Original album/movie/show title
    public static final String ORIGINAL_FILE_NAME     = "TOFN"; // Original filename
    public static final String ORIGINAL_TEXT_WRITER   = "TOLY"; // Original lyricist(s)/text writer(s)
    public static final String ORIGINAL_ARTIST        = "TOPE"; // Original artist(s)/performer(s)
    public static final String FILE_OWNER             = "TOWN"; // File owner/licensee
    public static final String ARTIST                 = "TPE1"; // Lead performer(s)/Soloist(s)
    public static final String BAND                   = "TPE2"; // Band/orchestra/accompaniment
    public static final String CONDUCTOR              = "TPE3"; // Conductor/performer refinement
    public static final String REMIXED_BY             = "TPE4"; // Interpreted, remixed, or otherwise
    public static final String PART_OF_SET            = "TPOS"; // Part of a set
    public static final String PRODUCED_NOTICE        = "TPRO"; // Produced notice
    public static final String PUBLISHER              = "TPUB"; // Publisher
    public static final String TRACK_NUMBER           = "TRCK"; // Track number/Position in set
    public static final String RADIO_STATION_NAME     = "TRSN"; // Internet radio station name
    public static final String RADIO_STATION_OWNER    = "TRSO"; // Internet radio station owner
    public static final String ALBUM_SORT_ORDER       = "TSOA"; // Album sort order
    public static final String PERFORMER_SORT_ORDER   = "TSOP"; // Performer sort order
    public static final String TITLE_SORT_ORDER       = "TSOT"; // Title sort order
    public static final String RECORDING_CODE         = "TSRC"; // ISRC (international standard record
    public static final String SOFTWARE_CONFIG        = "TSSE"; // Software/Hardware and settings used
    public static final String SET_SUBTITLE           = "TSST"; // Set subtitle
    public static final String CUSTOM                 = "TXXX"; // User defined text information frame
    public static final String UNIQUE_FILE_ID         = "UFID"; // Unique file identifier
    public static final String TERMS_OF_USE           = "USER"; // Terms of use
    public static final String U_LYRICS               = "USLT"; // Unsynchronised lyric/text transcrip
    public static final String URL_COMMERCIAL_INFO    = "WCOM"; // Commercial information
    public static final String URL_COPYRIGHT          = "WCOP"; // Copyright/Legal information
    public static final String URL_AUDIO_PAGE         = "WOAF"; // Official audio file webpage
    public static final String URL_ARTIST_PAGE        = "WOAR"; // Official artist/performer webpage
    public static final String URL_AUDIO_SOURCE_PAGE  = "WOAS"; // Official audio source webpage
    public static final String URL_RADIO_STATION_PAGE = "WORS"; // Official Internet radio station hom
    public static final String URL_PAYMENT            = "WPAY"; // Payment
    public static final String URL_PUBLISHER          = "WPUB"; // Publishers official webpage
    public static final String URL_CUSTOM             = "WXXX"; // User defined URL link frame

    /* Itunes extensions */
    public static final String COMPILATION       = "TCMP";
    public static final String COMPOSER_SORT     = "TSOC";
    public static final String ALBUM_ARTIST_SORT = "TSO2";
    public static final String MOVEMENT          = "MVNM";

    public static final Set<String> V23_DEPRECATED_FRAMES = Set.of(
            "EQUA",
            "IPLS",
            "RVAD",
            "TDAT",
            "TIME",
            "TORY",
            "TRDA",
            "TSIZ",
            "TYER"
    );

    public static final Set<String> V24_NEW_FRAMES = Set.of(
            "ASPI",
            "EQU2",
            "RVA2",
            "SEEK",
            "SIGN",
            "TDEN",
            "TDOR",
            "TDRC",
            "TDRL",
            "TDTG",
            "TIPL",
            "TMCL",
            "TMOO",
            "TPRO",
            "TSOA",
            "TSOP",
            "TSOT",
            "TSST"
    );

    protected FrameHeader header;
    protected byte[] frameBytes;

    abstract T getFrameData();
    abstract void setFrameData(T data);
    abstract void parseFrameData(byte[] buffer, FrameHeader header);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractFrame<?> frame = (AbstractFrame<?>) obj;
        return getKey().equals(frame.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Override
    public byte[] getBytes() {

        byte[] frameBody  = frameBytes;
        byte[] fieldBytes = header.buildFlagFields(frameBody.length);

        if (header.isFrameCompressed()) frameBody = compressFrame(frameBody);
        if (header.isFrameUnsynch())    frameBody = toUnsynch(frameBody);

        byte[] frame = new byte[frameBody.length + fieldBytes.length];
        System.arraycopy(fieldBytes, 0, frame, 0, fieldBytes.length);
        System.arraycopy(frameBody, 0, frame, fieldBytes.length, frameBody.length);

        return frame;
    }

    public String getIdentifier() {
        return header.getIdentifier();
    }

    public FrameHeader getHeader() {
        return header;
    }

    public String getKey() {
        return getIdentifier();
    }

    public static byte[] compressFrame(byte[] frame) {
        try {

            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            DeflaterOutputStream compressedOut = new DeflaterOutputStream(byteArrayOut);

            compressedOut.write(frame);
            compressedOut.flush();
            compressedOut.close();

            return byteArrayOut.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decompressFrame(byte[] compressedFrame) {
        try {

            ByteArrayOutputStream byteArrayOut   = new ByteArrayOutputStream();
            InflaterOutputStream decompressedOut = new InflaterOutputStream(byteArrayOut);

            decompressedOut.write(compressedFrame);
            decompressedOut.flush();
            decompressedOut.close();

            return byteArrayOut.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
