package com.jtagger;

import com.jtagger.utils.ImageReader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AttachedPicture {

    public static final String MIME_TYPE_URL = "-->";

    public static final byte PICTURE_TYPE_OTHER                = 0x00;
    public static final byte PICTURE_TYPE_PNG_ICON             = 0x01;
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

    private int pictureType = PICTURE_TYPE_OTHER;

    private String mimeType = "";
    private String description = "";

    private int width = 0;
    private int height = 0;
    private int colorDepth = 24;

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
        if (type >= PICTURE_TYPE_OTHER && type <= PICTURE_TYPE_STUDIO_LOGO) {
            this.pictureType = type;
            return;
        }
        throw new IllegalArgumentException("Invalid picture type: " + type);
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setDescription(String description) {
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
        setMimeType(ImageReader.getMimeType(file));
        setPictureData(ImageReader.readFromFile(file));
    }

    public void setPictureData(URL url) {
        setMimeType(ImageReader.getMimeType(url));
        setPictureData(ImageReader.readFromURL(url));
    }

    public boolean isPictureURL() {
        return mimeType.equals(MIME_TYPE_URL);
    }
}
