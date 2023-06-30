package com.jtagger.mp3.id3;

import com.jtagger.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public abstract class AbstractFrame<T> implements Component {

    public static final String EQUALIZATION          = "EQUA";
    public static final String INVOLVED_PEOPLE       = "IPLS";
    public static final String RELATIVE_VOL_ADJUST   = "RVAD";
    public static final String DATE                  = "TDAT";
    public static final String TIME                  = "TIME";
    public static final String ORIGINAL_RELEASE_YEAR = "TORY";
    public static final String RECORDING_DATES       = "TRDA";
    public static final String SIZE                  = "TSIZ";
    public static final String YEAR                  = "TYER";
    public static final String U_LYRICS              = "USLT";
    public static final String S_LYRICS              = "SYLT";

    public static final String ALBUM                 = "TALB";
    public static final String COMMENT               = "COMM";
    public static final String PICTURE               = "APIC";
    public static final String COMPOSER              = "TCOM";
    public static final String GENRE                 = "TCON";
    public static final String RECORDING_TIME        = "TDRC";
    public static final String TITLE                 = "TIT2";
    public static final String ARTIST                = "TPE1";
    public static final String TRACK_NUMBER          = "TRCK";
    public static final String BPM                   = "TBPM";
    public static final String COPYRIGHT             = "TCOP";
    public static final String ENCODING_TIME         = "TDEN";
    public static final String PLAYLIST_DELAY        = "TDLY";
    public static final String ORIGINAL_RELEASE_TIME = "TDOR";
    public static final String RELEASE_TIME          = "TDRL";
    public static final String TAGGING_TIME          = "TDTG";
    public static final String ENCODED_BY            = "TENC";
    public static final String TEXT_WRITER           = "TEXT";
    public static final String FILE_TYPE             = "TFLT";
    public static final String PEOPLE                = "TIPL";
    public static final String GENRE_DESCRIPTION     = "TIT1";
    public static final String SUBTITLE              = "TIT3";
    public static final String INITIAL_KEY           = "TKEY";
    public static final String LANGUAGES             = "TLAN";
    public static final String LENGTH                = "TLEN";
    public static final String CREDITS               = "TMCL";
    public static final String MEDIA_TYPE            = "TMED";
    public static final String MOOD                  = "TMOO";
    public static final String ORIGINAL_ALBUM_TITLE  = "TOAL";
    public static final String ORIGINAL_FILE_NAME    = "TOFN";
    public static final String ORIGINAL_TEXT_WRITER  = "TOLY";
    public static final String ORIGINAL_ARTIST       = "TOPE";
    public static final String FILE_OWNER            = "TOWN";
    public static final String BAND                  = "TPE2";
    public static final String CONDUCTOR             = "TPE3";
    public static final String REMIXED_BY            = "TPE4";
    public static final String PART_OF_SET           = "TPOS";
    public static final String PRODUCED_NOTICE       = "TPRO";
    public static final String PUBLISHER             = "TPUB";
    public static final String RADIO_STATION_NAME    = "TRSN";
    public static final String RADIO_STATION_OWNER   = "TRSO";
    public static final String ALBUM_SORT_ORDER      = "TSOA";
    public static final String PERFORMER_SORT_ORDER  = "TSOP";
    public static final String TITLE_SORT_ORDER      = "TSOT";
    public static final String RECORDING_CODE        = "TSRC";
    public static final String SOFTWARE_CONFIG       = "TSSE";
    public static final String SET_SUBTITLE          = "TSST";
    public static final String CUSTOM                = "TXXX";

    public static final String[] V2_3_FRAMES = new String[] {
            ALBUM, COMMENT, PICTURE, COMPOSER,
            GENRE, TITLE, ARTIST, TRACK_NUMBER,
            BPM, COPYRIGHT, PLAYLIST_DELAY, ENCODED_BY,
            TEXT_WRITER, FILE_TYPE, GENRE_DESCRIPTION, SUBTITLE,
            INITIAL_KEY, LANGUAGES, LENGTH, MEDIA_TYPE,
            ORIGINAL_ALBUM_TITLE, ORIGINAL_FILE_NAME, ORIGINAL_TEXT_WRITER, ORIGINAL_ARTIST,
            FILE_OWNER, BAND, CONDUCTOR, REMIXED_BY, PART_OF_SET,
            PUBLISHER, RADIO_STATION_NAME, RADIO_STATION_OWNER,RECORDING_CODE,
            SOFTWARE_CONFIG, YEAR, ORIGINAL_RELEASE_YEAR, CUSTOM, RECORDING_DATES,
            SIZE, DATE, TIME, RELATIVE_VOL_ADJUST, INVOLVED_PEOPLE, EQUALIZATION,
            U_LYRICS, S_LYRICS
    };

    public static final String[] V2_4_FRAMES = new String[] {
            ALBUM, COMMENT, PICTURE, COMPOSER,
            GENRE, RECORDING_TIME, TITLE, ARTIST, TRACK_NUMBER,
            BPM, COPYRIGHT, ENCODING_TIME, PLAYLIST_DELAY,
            ORIGINAL_RELEASE_TIME, RELEASE_TIME, TAGGING_TIME, ENCODED_BY,
            TEXT_WRITER, FILE_TYPE, PEOPLE, GENRE_DESCRIPTION, SUBTITLE,
            INITIAL_KEY, LANGUAGES, LENGTH, CREDITS, MEDIA_TYPE, MOOD, ORIGINAL_ALBUM_TITLE,
            ORIGINAL_FILE_NAME, ORIGINAL_TEXT_WRITER, ORIGINAL_ARTIST, FILE_OWNER, BAND,
            CONDUCTOR, REMIXED_BY, PART_OF_SET, PRODUCED_NOTICE,
            PUBLISHER, RADIO_STATION_NAME, RADIO_STATION_OWNER, ALBUM_SORT_ORDER,
            PERFORMER_SORT_ORDER, TITLE_SORT_ORDER, RECORDING_CODE,
            SOFTWARE_CONFIG, SET_SUBTITLE, CUSTOM, U_LYRICS, S_LYRICS
    };

    protected FrameHeader header;
    protected byte[] frameBytes;

    abstract T getFrameData();
    abstract void setFrameData(T data);

    @Override
    public byte[] getBytes() {

        byte[] frameBody = Arrays.copyOf(frameBytes, frameBytes.length);
        byte[] fieldBytes = header.buildFlagFields(frameBody.length);

        if (header.isFrameCompressed()) {
            frameBody = compressFrame(frameBody);
        }
        if (header.isFrameUnsynch()) {
            frameBody = UnsynchronisationUtils.toUnsynch(frameBody);
        }

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

    public static byte[] compressFrame(byte[] frame) {
        try {

            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            DeflaterOutputStream compressedOut = new DeflaterOutputStream(byteArrayOut);

            compressedOut.write(frame);
            compressedOut.flush();
            compressedOut.close();

            return byteArrayOut.toByteArray();
        } catch (IOException e) {
            return frame;
        }
    }

    public static byte[] decompressFrame(byte[] compressedFrame) {
        try {

            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            InflaterOutputStream decompressedOut = new InflaterOutputStream(byteArrayOut);

            decompressedOut.write(compressedFrame);
            decompressedOut.flush();
            decompressedOut.close();

            return byteArrayOut.toByteArray();
        } catch (IOException e) {
            return compressedFrame;
        }
    }
}
