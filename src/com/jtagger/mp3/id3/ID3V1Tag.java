package com.jtagger.mp3.id3;

import com.jtagger.AbstractTag;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.mp3.id3.TextEncoding.isNumeric;
import static java.lang.Byte.toUnsignedInt;

public class ID3V1Tag extends ID3Tag {

    public static final String ID     = "TAG";
    private static final int TAG_SIZE = 128;

    public static final byte ID_OFFSET      = 0;
    public static final byte TITLE_OFFSET   = 3;
    public static final byte ARTIST_OFFSET  = 33;
    public static final byte ALBUM_OFFSET   = 63;
    public static final byte YEAR_OFFSET    = 93;
    public static final byte COMMENT_OFFSET = 97;
    public static final byte TRACK_NUMBER   = 126;
    public static final byte GENRE_OFFSET   = 127;

    public static final byte ID3V1   = 0;
    public static final byte ID3V1_1 = 1;

    public static final int BLUES                   = 0;
    public static final int CLASSIC_ROCK            = 1;
    public static final int COUNTRY                 = 2;
    public static final int DANCE                   = 3;
    public static final int DISCO                   = 4;
    public static final int FUNK                    = 5;
    public static final int GRUNGE                  = 6;
    public static final int HIP_HOP                 = 7;
    public static final int JAZZ                    = 8;
    public static final int METAL                   = 9;
    public static final int NEW_AGE                 = 10;
    public static final int OLDIES                  = 11;
    public static final int OTHER                   = 12;
    public static final int POP                     = 13;
    public static final int R_AND_B                 = 14;
    public static final int RAP                     = 15;
    public static final int REGGAE                  = 16;
    public static final int ROCK                    = 17;
    public static final int TECHNO                  = 18;
    public static final int INDUSTRIAL              = 19;
    public static final int ALTERNATIVE             = 20;
    public static final int SKA                     = 21;
    public static final int DEATH_METAL             = 22;
    public static final int PRANKS                  = 23;
    public static final int SOUNDTRACK              = 24;
    public static final int EURO_TECHNO             = 25;
    public static final int AMBIENT                 = 26;
    public static final int TRIP_HOP                = 27;
    public static final int VOCAL                   = 28;
    public static final int JAZZ_FUNK               = 29;
    public static final int FUSION                  = 30;
    public static final int TRANCE                  = 31;
    public static final int CLASSICAL               = 32;
    public static final int INSTRUMENTAL            = 33;
    public static final int ACID                    = 34;
    public static final int HOUSE                   = 35;
    public static final int GAME                    = 36;
    public static final int SOUND_CLIP              = 37;
    public static final int GOSPEL                  = 38;
    public static final int NOISE                   = 39;
    public static final int ALTERNATIVE_ROCK        = 40;
    public static final int BASS                    = 41;
    public static final int SOUL                    = 42;
    public static final int PUNK                    = 43;
    public static final int SPACE                   = 44;
    public static final int MEDITATIVE              = 45;
    public static final int INSTRUMENTAL_POP        = 46;
    public static final int INSTRUMENTAL_ROCK       = 47;
    public static final int ETHNIC                  = 48;
    public static final int GOTHIC                  = 49;
    public static final int DARKWAVE                = 50;
    public static final int TECHNO_INDUSTRIAL       = 51;
    public static final int ELECTRONIC              = 52;
    public static final int POP_FOLK                = 53;
    public static final int EURODANCE               = 54;
    public static final int DREAM                   = 55;
    public static final int SOUTHERN_ROCK           = 56;
    public static final int COMEDY                  = 57;
    public static final int CULT                    = 58;
    public static final int GANGSTA_RAP             = 59;
    public static final int TOP_40                  = 60;
    public static final int CHRISTIAN_RAP           = 61;
    public static final int POP_FUNK                = 62;
    public static final int JUNGLE                  = 63;
    public static final int NATIVE_AMERICAN         = 64;
    public static final int CABARET                 = 65;
    public static final int NEW_WAVE                = 66;
    public static final int PSYCHEDELIC             = 67;
    public static final int RAVE                    = 68;
    public static final int SHOWTUNES               = 69;
    public static final int TRAILER                 = 70;
    public static final int LO_FI                   = 71;
    public static final int TRIBAL                  = 72;
    public static final int ACID_PUNK               = 73;
    public static final int ACID_JAZZ               = 74;
    public static final int POLKA                   = 75;
    public static final int RETRO                   = 76;
    public static final int MUSICAL                 = 77;
    public static final int ROCK_AND_ROLL           = 78;
    public static final int HARD_ROCK               = 79;
    public static final int FOLK                    = 80;
    public static final int FOLK_ROCK               = 81;
    public static final int NATIONAL_FOLK           = 82;
    public static final int SWING                   = 83;
    public static final int FAST_FUSION             = 84;
    public static final int BEBOB                   = 85;
    public static final int LATIN                   = 86;
    public static final int REVIVAL                 = 87;
    public static final int CELTIC                  = 88;
    public static final int BLUEGRASS               = 89;
    public static final int AVANTGARDE              = 90;
    public static final int GOTHIC_ROCK             = 91;
    public static final int PROGRESSIVE_ROCK        = 92;
    public static final int PSYCHEDELIC_ROCK        = 93;
    public static final int SYMPHONIC_ROCK          = 94;
    public static final int SLOW_ROCK               = 95;
    public static final int BIG_BAND                = 96;
    public static final int CHORUS                  = 97;
    public static final int EASY_LISTENING          = 98;
    public static final int ACOUSTIC                = 99;
    public static final int HUMOUR                  = 100;
    public static final int SPEECH                  = 101;
    public static final int CHANSON                 = 102;
    public static final int OPERA                   = 103;
    public static final int CHAMBER_MUSIC           = 104;
    public static final int SONATA                  = 105;
    public static final int SYMPHONY                = 106;
    public static final int BOOTY_BASS              = 107;
    public static final int PRIMUS                  = 108;
    public static final int PORN_GROOVE             = 109;
    public static final int SATIRE                  = 110;
    public static final int SLOW_JAM                = 111;
    public static final int CLUB                    = 112;
    public static final int TANGO                   = 113;
    public static final int SAMBA                   = 114;
    public static final int FOLKLORE                = 115;
    public static final int BALLAD                  = 116;
    public static final int POWER_BALLAD            = 117;
    public static final int RHYTHMIC_SOUL           = 118;
    public static final int FREESTYLE               = 119;
    public static final int DUET                    = 120;
    public static final int PUNK_ROCK               = 121;
    public static final int DRUM_SOLO               = 122;
    public static final int A_CAPPELLA              = 123;
    public static final int EURO_HOUSE              = 124;
    public static final int DANCE_HALL              = 125;
    public static final int GOA                     = 126;
    public static final int DRUM_BASS               = 127;
    public static final int CLUB_HOUSE              = 128;
    public static final int HARDCORE                = 129;
    public static final int TERROR                  = 130;
    public static final int INDIE                   = 131;
    public static final int BRITPOP                 = 132;
    public static final int NEGERPUNK               = 133;
    public static final int POLSK_PUNK              = 134;
    public static final int BEAT                    = 135;
    public static final int CHRISTIAN_GANGSTA_RAP   = 136;
    public static final int HEAVY_METAL             = 137;
    public static final int BLACK_METAL             = 138;
    public static final int CROSSOVER               = 139;
    public static final int CONTEMPORARY_CHRISTIAN  = 140;
    public static final int CHRISTIAN_ROCK          = 141;
    public static final int MERENGUE                = 142;
    public static final int SALSA                   = 143;
    public static final int THRASH_METAL            = 144;
    public static final int ANIME                   = 145;
    public static final int JPOP                    = 146;
    public static final int SYNTHPOP                = 147;
    public static final int ABSTRACT                = 148;
    public static final int ART_ROCK                = 149;
    public static final int BAROQUE                 = 150;
    public static final int BHANGRA                 = 151;
    public static final int BIG_BEAT                = 152;
    public static final int BREAKBEAT               = 153;
    public static final int CHILLOUT                = 154;
    public static final int DOWNTEMPO               = 155;
    public static final int DUB                     = 156;
    public static final int EBM                     = 157;
    public static final int ECLECTIC                = 158;
    public static final int ELECTRO                 = 159;
    public static final int ELECTROCLASH            = 160;
    public static final int EMO                     = 161;
    public static final int EXPERIMENTAL            = 162;
    public static final int GARAGE                  = 163;
    public static final int GLOBAL                  = 164;
    public static final int IDM                     = 165;
    public static final int ILLBIENT                = 166;
    public static final int INDUSTRO_GOTH           = 167;
    public static final int JAM_BAND                = 168;
    public static final int KRAUTROCK               = 169;
    public static final int LEFTFIELD               = 170;
    public static final int LOUNGE                  = 171;
    public static final int MATH_ROCK               = 172;
    public static final int NEW_ROMANTIC            = 173;
    public static final int NU_BREAKZ               = 174;
    public static final int POST_PUNK               = 175;
    public static final int POST_ROCK               = 176;
    public static final int PSYTRANCE               = 177;
    public static final int SHOEGAZE                = 178;
    public static final int SPACE_ROCK              = 179;
    public static final int TROP_ROCK               = 180;
    public static final int WORLD_MUSIC             = 181;
    public static final int NEOCLASSICAL            = 182;
    public static final int AUDIOBOOK               = 183;
    public static final int AUDIO_THEATRE           = 184;
    public static final int NEUE_DEUTSCHE_WELLE     = 185;
    public static final int PODCAST                 = 186;
    public static final int INDIE_ROCK              = 187;
    public static final int G_FUNK                  = 188;
    public static final int DUBSTEP                 = 189;
    public static final int GARAGE_ROCK             = 190;
    public static final int PSYBIENT                = 191;
    public static final int UNKNOWN                 = 192;

    public static final String[] GENRES = new String[] {
            "Blues",
            "Classic Rock",
            "Country",
            "Dance",
            "Disco",
            "Funk",
            "Grunge",
            "Hip-Hop",
            "Jazz",
            "Metal",
            "New Age",
            "Oldies",
            "Other",
            "Pop",
            "R&B",
            "Rap",
            "Reggae",
            "Rock",
            "Techno",
            "Industrial",
            "Alternative",
            "Ska",
            "Death Metal",
            "Pranks",
            "Soundtrack",
            "Euro-Techno",
            "Ambient",
            "Trip-Hop",
            "Vocal",
            "Jazz+Funk",
            "Fusion",
            "Trance",
            "Classical",
            "Instrumental",
            "Acid",
            "House",
            "Game",
            "Sound Clip",
            "Gospel",
            "Noise",
            "Alternative Rock",
            "Bass",
            "Soul",
            "Punk",
            "Space",
            "Meditative",
            "Instrumental Pop",
            "Instrumental Rock",
            "Ethnic",
            "Gothic",
            "Darkwave",
            "Techno-Industrial",
            "Electronic",
            "Pop-Folk",
            "Eurodance",
            "Dream",
            "Southern Rock",
            "Comedy",
            "Cult",
            "Gangsta Rap",
            "Top 40",
            "Christian Rap",
            "Pop/Funk",
            "Jungle",
            "Native American",
            "Cabaret",
            "New Wave",
            "Psychedelic",
            "Rave",
            "Showtunes",
            "Trailer",
            "Lo-Fi",
            "Tribal",
            "Acid Punk",
            "Acid Jazz",
            "Polka",
            "Retro",
            "Musical",
            "Rock & Roll",
            "Hard Rock",
            "Folk",
            "Folk-Rock",
            "National Folk",
            "Swing",
            "Fast Fusion",
            "Bebob",
            "Latin",
            "Revival",
            "Celtic",
            "Bluegrass",
            "Avantgarde",
            "Gothic Rock",
            "Progressive Rock",
            "Psychedelic Rock",
            "Symphonic Rock",
            "Slow Rock",
            "Big Band",
            "Chorus",
            "Easy Listening",
            "Acoustic",
            "Humour",
            "Speech",
            "Chanson",
            "Opera",
            "Chamber Music",
            "Sonata",
            "Symphony",
            "Booty Bass",
            "Primus",
            "Porn Groove",
            "Satire",
            "Slow Jam",
            "Club",
            "Tango",
            "Samba",
            "Folklore",
            "Ballad",
            "Power Ballad",
            "Rhythmic Soul",
            "Freestyle",
            "Duet",
            "Punk Rock",
            "Drum Solo",
            "A Cappella",
            "Euro-House",
            "Dance Hall",
            "Goa",
            "Drum & Bass",
            "Club-House",
            "Hardcore",
            "Terror",
            "Indie",
            "BritPop",
            "Negerpunk",
            "Polsk Punk",
            "Beat",
            "Christian Gangsta Rap",
            "Heavy Metal",
            "Black Metal",
            "Crossover",
            "Contemporary Christian",
            "Christian Rock",
            "Merengue",
            "Salsa",
            "Thrash Metal",
            "Anime",
            "JPop",
            "Synthpop",
            "Abstract",
            "Art Rock",
            "Baroque",
            "Bhangra",
            "Big Beat",
            "Breakbeat",
            "Chillout",
            "Downtempo",
            "Dub",
            "EBM",
            "Eclectic",
            "Electro",
            "Electroclash",
            "Emo",
            "Experimental",
            "Garage",
            "Global",
            "IDM",
            "Illbient",
            "Industro-Goth",
            "Jam Band",
            "Krautrock",
            "Leftfield",
            "Lounge",
            "Math Rock",
            "New Romantic",
            "Nu-Breakz",
            "Post-Punk",
            "Post-Rock",
            "Psytrance",
            "Shoegaze",
            "Space Rock",
            "Trop Rock",
            "World Music",
            "Neoclassical",
            "Audiobook",
            "Audio Theatre",
            "Neue Deutsche Welle",
            "Podcast",
            "Indie Rock",
            "G-Funk",
            "Dubstep",
            "Garage Rock",
            "Psybient",
            "Unknown",
    };

    private byte[] tagBytes;
    private String title   = "";
    private String artist  = "";
    private String album   = "";
    private String year    = "";
    private String comment = "";

    private byte genre       = (byte) UNKNOWN;
    private byte trackNumber = 0;
    private byte version     = ID3V1_1;

    @Override
    protected <T> void setFieldValue(String fieldId, T value) {
        String fieldValue = (String) value;
        switch (fieldId) {
            case AbstractTag.TITLE:
                setTitle(fieldValue);
                break;
            case AbstractTag.ARTIST:
                setArtist(fieldValue);
                break;
            case AbstractTag.ALBUM:
                setAlbum(fieldValue);
                break;
            case AbstractTag.YEAR:
                setYear(fieldValue);
                break;
            case AbstractTag.COMMENT:
                setComment(fieldValue);
                break;
            case AbstractTag.GENRE:
                setGenre(fieldValue);
                break;
            case AbstractTag.TRACK_NUMBER:
                setAlbumTrack(Integer.parseInt(fieldValue));
                break;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getFieldValue(String fieldId) {
        switch (fieldId) {
            case AbstractTag.TITLE:
                return (T) title;
            case AbstractTag.ARTIST:
                return (T) artist;
            case AbstractTag.ALBUM:
                return (T) album;
            case AbstractTag.YEAR:
                return (T) year;
            case AbstractTag.COMMENT:
                return (T) comment;
            case AbstractTag.GENRE:
                return (T) String.valueOf(genre);
            case AbstractTag.TRACK_NUMBER:
                return (T) String.valueOf(trackNumber);
            default:
                return null;
        }
    }

    @Override
    public void removeField(String fieldId) {
        switch (fieldId) {
            case AbstractTag.TITLE:
                title = "";
                break;
            case AbstractTag.ARTIST:
                artist = "";
                break;
            case AbstractTag.ALBUM:
                album = "";
                break;
            case AbstractTag.YEAR:
                year = "";
                break;
            case AbstractTag.COMMENT:
                comment = "";
                break;
            case AbstractTag.GENRE:
                genre = (byte) UNKNOWN;
                break;
            case AbstractTag.TRACK_NUMBER:
                trackNumber = 0;
                break;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "title: %s\nartist: %s\nalbum: %s\nyear: %s\ncomment: %s\ntrack:%d\ngenre:%d\nversion: %d",
                getTitle(), getArtist(), getAlbum(), getYear(), getComment(), getTrackNumber(), getGenre(), getVersion()
        );
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getYear() {
        return year;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public void setVersion(byte version) {
        if (version != ID3V1 && version != ID3V1_1) {
            throw new IllegalArgumentException("Invalid version number");
        }
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public int getGenre() {
        return toUnsignedInt(genre);
    }

    public int getTrackNumber() {
        return toUnsignedInt(trackNumber);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setGenre(String genre) {
        if (genre.isBlank()) return;
        if (isNumeric(genre)) {
            int id3Genre = Integer.parseInt(genre);
            setGenre(id3Genre < 0 || id3Genre > 255 ? UNKNOWN : id3Genre);
            return;
        }
        int genreIndex = Arrays.asList(GENRES).indexOf(genre);
        if (genreIndex != -1) {
            setGenre(genreIndex);
        }
    }

    public void setGenre(int genre) {
        if (genre >= 0 && genre < 256) {
            this.genre = (byte) Math.min(genre, UNKNOWN);
            return;
        }
        throw new IllegalArgumentException("Invalid genre: " + genre);
    }

    public void setAlbumTrack(int trackNumber) {
        if (trackNumber > 0 && trackNumber < 256) {
            this.trackNumber = (byte) trackNumber;
            return;
        }
        throw new IllegalArgumentException("Invalid track number: " + trackNumber);
    }

    private static byte[] padBuffer(byte[] buffer, int size) {
        return buffer.length == size ? buffer : Arrays.copyOf(buffer, size);
    }

    @Override
    public byte[] assemble(byte version) {

        if (version != ID3V1 && version != ID3V1_1) {
            throw new IllegalArgumentException("Invalid version number: " + version);
        }

        this.version = version;
        byte[] tag   = new byte[TAG_SIZE];

        byte[] tagIdBytes   = ID.getBytes(StandardCharsets.ISO_8859_1);
        byte[] titleBytes   = padBuffer(title.getBytes(StandardCharsets.ISO_8859_1),  30);
        byte[] artistBytes  = padBuffer(artist.getBytes(StandardCharsets.ISO_8859_1), 30);
        byte[] albumBytes   = padBuffer(album.getBytes(StandardCharsets.ISO_8859_1),  30);
        byte[] yearBytes    = padBuffer(year.getBytes(StandardCharsets.ISO_8859_1),    4);
        byte[] commentBytes = padBuffer(
                comment.getBytes(StandardCharsets.ISO_8859_1), version == ID3V1_1 ? 28 : 30
        );

        System.arraycopy(tagIdBytes,   0, tag, ID_OFFSET,      tagIdBytes.length);
        System.arraycopy(titleBytes,   0, tag, TITLE_OFFSET,   titleBytes.length);
        System.arraycopy(artistBytes,  0, tag, ARTIST_OFFSET,  artistBytes.length);
        System.arraycopy(albumBytes,   0, tag, ALBUM_OFFSET,   albumBytes.length);
        System.arraycopy(yearBytes,    0, tag, YEAR_OFFSET,    yearBytes.length);
        System.arraycopy(commentBytes, 0, tag, COMMENT_OFFSET, commentBytes.length);
        System.out.println(COMMENT_OFFSET + commentBytes.length);

        if (version == ID3V1_1) {
            tag[TRACK_NUMBER] = trackNumber;
        }

        tag[GENRE_OFFSET] = genre;
        this.tagBytes = tag;
        return tag;
    }

    @Override
    public byte[] getBytes() {
        return tagBytes;
    }

    public static Builder newBuilder() {
        return new ID3V1Tag().new Builder();
    }

    public static Builder newBuilder(ID3V1Tag tag) {
        return tag.new Builder();
    }

    public class Builder {

        public Builder setVersion(byte version) {
            ID3V1Tag.this.setVersion(version);
            return this;
        }

        public Builder setTitle(String title) {
            ID3V1Tag.this.setTitle(title);
            return this;
        }

        public Builder setAlbumTrack(int trackNumber) {
            ID3V1Tag.this.setAlbumTrack(trackNumber);
            return this;
        }

        public Builder setArtist(String artist) {
            ID3V1Tag.this.setArtist(artist);
            return this;
        }

        public Builder setAlbum(String album) {
            ID3V1Tag.this.setAlbum(album);
            return this;
        }

        public Builder setGenre(int genre) {
            ID3V1Tag.this.setGenre(genre);
            return this;
        }

        public Builder setYear(String year) {
            ID3V1Tag.this.setYear(year);
            return this;
        }

        public Builder setComment(String comment) {
            ID3V1Tag.this.setComment(comment);
            return this;
        }

        public ID3V1Tag buildExisting(byte[] tagBytes) {
            ID3V1Tag.this.tagBytes = tagBytes;
            return ID3V1Tag.this;
        }

        public ID3V1Tag build(byte version) {
            ID3V1Tag.this.assemble(version);
            return ID3V1Tag.this;
        }
    }
}
