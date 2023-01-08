package com.rrtry;

public abstract class Tag implements Component {

    public static final String TITLE             = "TITLE";
    public static final String ARTIST            = "ARTIST";
    public static final String ALBUM             = "ALBUM";
    public static final String COMMENT           = "COMMENT";
    public static final String YEAR              = "DATE";
    public static final String TRACK_NUMBER      = "TRACKNUMBER";
    public static final String GENRE             = "GENRE";
    public static final String ALBUM_ARTIST      = "ALBUMARTIST";
    public static final String ARRANGER          = "ARRANGER";
    public static final String AUTHOR            = "AUTHOR";
    public static final String BPM               = "BPM";
    public static final String CATALOG_NUMBER    = "CATALOGNUMBER";
    public static final String COMPILATION       = "COMPILATION";
    public static final String COMPOSER          = "COMPOSER";
    public static final String CONDUCTOR         = "CONDUCTOR";
    public static final String COPYRIGHT         = "COPYRIGHT";
    public static final String DESCRIPTION       = "DESCRIPTION";
    public static final String DISC_NUMBER       = "DISCNUMBER";
    public static final String ENCODED_BY        = "ENCODEDBY";
    public static final String ENCODER_SETTINGS  = "ENCODERSETTINGS";
    public static final String ENCODING_TIME     = "ENCODINGTIME";
    public static final String GROUPING          = "GROUPING";
    public static final String INITIAL_KEY       = "INITIALKEY";
    public static final String ISRC              = "ISRC";
    public static final String LANGUAGE          = "LANGUAGE";
    public static final String LYRICIST          = "LYRICIST";
    public static final String LYRICS            = "LYRICS";
    public static final String MEDIA             = "MEDIA";
    public static final String MOOD              = "MOOD";
    public static final String ORIGINAL_ALBUM    = "ORIGINALALBUM";
    public static final String ORIGINAL_ARTIST   = "ORIGINALARTIST";
    public static final String ORIGINAL_DATE     = "ORIGINALDATE";
    public static final String PERFORMER         = "PERFORMER";
    public static final String PICTURE           = "PICTURE";
    public static final String PUBLISHER         = "PUBLISHER";
    public static final String RATING            = "RATING";
    public static final String RELEASE_COUNTRY   = "RELEASECOUNTRY";
    public static final String RELEASE_DATE      = "RELEASEDATE";
    public static final String REMIXER           = "REMIXER";
    public static final String SORT_ALBUM        = "SORTALBUM";
    public static final String SORT_ALBUM_ARTIST = "SORTALBUMARTIST";
    public static final String SORT_ARTIST       = "SORTARTIST";
    public static final String SORT_COMPOSER     = "SORTCOMPOSER";
    public static final String SORT_NAME         = "SORTNAME";
    public static final String SUBTITLE          = "SUBTITLE";
    public static final String WEBSITE           = "WEBSITE";
    public static final String WORK              = "WORK";
    public static final String WWW_AUDIO_FILE    = "WWWAUDIOFILE";
    public static final String WWW_AUDIO_SOURCE  = "WWWAUDIOSOURCE";

    public void setStringField(String fieldId, String value) {
        if (fieldId.equals(PICTURE)) throw new IllegalArgumentException("Must be a string field");
        setFieldValue(fieldId, value.isEmpty() ? " " : value);
    }

    public String getStringField(String fieldId) {
        if (fieldId.equals(PICTURE)) throw new IllegalArgumentException("Must be a string field");
        return getFieldValue(fieldId);
    }

    public AttachedPicture getPictureField() {
        return getFieldValue(PICTURE);
    }

    public void setPictureField(AttachedPicture picture) {
        setFieldValue(PICTURE, picture);
    }

    abstract protected <T> void setFieldValue(String fieldId, T value);
    abstract protected <T> T getFieldValue(String fieldId);
}
