package com.jtagger;

import com.jtagger.utils.ImageReader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.jtagger.mp3.id3.AttachedPictureFrame.DESCRIPTION_MAX_LENGTH;

public class AttachedPicture {

    public static final String MIME_TYPE_URL = "-->";

    public static final byte PICTURE_TYPE_OTHER                = 0x00;
    public static final byte PICTURE_TYPE_PNG_ICON             = 0x01;
    public static final byte PICTURE_TYPE_OTHER_ICON           = 0x02;
    public static final byte PICTURE_TYPE_FRONT_COVER          = 0x03;
    public static final byte PICTURE_TYPE_BACK_COVER           = 0x04;
    public static final byte PICTURE_TYPE_LEAFLET_PAGE         = 0x05;
    public static final byte PICTURE_TYPE_MEDIA                = 0x06;
    public static final byte PICTURE_TYPE_LEAD_ARTIST          = 0x07;
    public static final byte PICTURE_TYPE_ARTIST               = 0x08;
    public static final byte PICTURE_TYPE_CONDUCTOR            = 0x09;
    public static final byte PICTURE_TYPE_BAND                 = 0x0A;
    public static final byte PICTURE_TYPE_COMPOSER             = 0x0B;
    public static final byte PICTURE_TYPE_TEXT_WRITER          = 0x0C;
    public static final byte PICTURE_TYPE_RECORDING_LOCATION   = 0x0D;
    public static final byte PICTURE_TYPE_DURING_RECODING      = 0x0E;
    public static final byte PICTURE_TYPE_DURING_PERFORMANCE   = 0x0F;
    public static final byte PICTURE_TYPE_MOVIE_SCREEN_CAPTURE = 0x10;
    public static final byte PICTURE_TYPE_COLOURED_FISH        = 0x11;
    public static final byte PICTURE_TYPE_ILLUSTRATION         = 0x12;
    public static final byte PICTURE_TYPE_BAND_LOGO            = 0x13;
    public static final byte PICTURE_TYPE_STUDIO_LOGO          = 0x14;

    private static final HashMap<Integer, String> PICTURE_TYPE_NAME_MAP = new HashMap<>();

    static {
        PICTURE_TYPE_NAME_MAP.put(0x00, "Other");
        PICTURE_TYPE_NAME_MAP.put(0x01, "PNG Icon");
        PICTURE_TYPE_NAME_MAP.put(0x02, "Other Icon");
        PICTURE_TYPE_NAME_MAP.put(0x03, "Front cover");
        PICTURE_TYPE_NAME_MAP.put(0x04, "Back cover");
        PICTURE_TYPE_NAME_MAP.put(0x05, "Leaflet page");
        PICTURE_TYPE_NAME_MAP.put(0x06, "Media");
        PICTURE_TYPE_NAME_MAP.put(0x07, "Lead artist");
        PICTURE_TYPE_NAME_MAP.put(0x08, "Artist");
        PICTURE_TYPE_NAME_MAP.put(0x09, "Conductor");
        PICTURE_TYPE_NAME_MAP.put(0x0A, "Band");
        PICTURE_TYPE_NAME_MAP.put(0x0B, "Composer");
        PICTURE_TYPE_NAME_MAP.put(0x0C, "Text writer");
        PICTURE_TYPE_NAME_MAP.put(0x0D, "Recording location");
        PICTURE_TYPE_NAME_MAP.put(0x0E, "During recording");
        PICTURE_TYPE_NAME_MAP.put(0x0F, "During performance");
        PICTURE_TYPE_NAME_MAP.put(0x10, "Movie screen capture");
        PICTURE_TYPE_NAME_MAP.put(0x11, "Coloured fish");
        PICTURE_TYPE_NAME_MAP.put(0x12, "Illustration");
        PICTURE_TYPE_NAME_MAP.put(0x13, "Band logo");
        PICTURE_TYPE_NAME_MAP.put(0x14, "Studio logo");
    }

    private String mimeType    = "image/png";
    private String description = "Front cover";

    private int pictureType = PICTURE_TYPE_FRONT_COVER;
    private int width       = 0;
    private int height      = 0;
    private int colorDepth  = 24;

    private byte[] pictureData;

    public AttachedPicture() { /* empty constructor */ }

    public AttachedPicture(
            int pictureType,
            String mimeType,
            String description,
            int width,
            int height,
            int colorDepth,
            byte[] pictureData)
    {
        this.pictureType = pictureType;
        this.mimeType    = mimeType;
        this.description = description;
        this.width       = width;
        this.height      = height;
        this.colorDepth  = colorDepth;
        this.pictureData = pictureData;
    }

    public static String getDescriptionFromType(int type) {
        return PICTURE_TYPE_NAME_MAP.getOrDefault(type, "");
    }

    public int getPictureType() {
        return pictureType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getDescription() {
        return description;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public byte[] getPictureData() {
        return pictureData;
    }

    public byte[] getPictureDataFromURL() {
        if (!isPictureURL()) throw new IllegalArgumentException("Image is not an URL");
        try {
            URL url = new URL(new String(pictureData, StandardCharsets.ISO_8859_1));
            return ImageReader.readFromURL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void setPictureType(int type) {
        if (type < PICTURE_TYPE_OTHER || type > PICTURE_TYPE_STUDIO_LOGO) {
            throw new IllegalArgumentException("Invalid picture type: " + type);
        }
        this.pictureType = type;
    }

    public void setMimeType(String mimeType) {
        if (!ImageReader.isPictureMimeType(mimeType)) {
            throw new IllegalArgumentException("Invalid mime type: " + mimeType);
        }
        this.mimeType = mimeType;
    }

    public void setDescription(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Picture description should be <= 64 characters long");
        }
        this.description = description;
    }

    public void setPictureWidth(int width) {
        this.width = width;
    }

    public void setPictureHeight(int height) {
        this.height = height;
    }

    public void setPictureColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    public void setPictureData(byte[] pictureData) {
        this.pictureData = pictureData;
    }

    public void setPictureURL(URL url) {
        setMimeType(MIME_TYPE_URL);
        pictureData = url.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    public void setPictureData(File file) {
        if (file.isFile()) {
            setMimeType(ImageReader.getMimeType(file));
            setPictureData(ImageReader.readFromFile(file));
            return;
        }
        throw new IllegalArgumentException("Specify regular file");
    }

    public void setPictureData(URL url) {
        setMimeType(ImageReader.getMimeType(url));
        setPictureData(ImageReader.readFromURL(url));
    }

    public boolean isPictureURL() {
        return mimeType.equals(MIME_TYPE_URL);
    }
}
