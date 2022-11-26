package com.rrtry;

/*
<Header for 'Attached picture', ID: "APIC">
     Text encoding      $xx
     MIME type          <text string> $00
     Picture type       $xx
     Description        <text string according to encoding> $00 (00)
     Picture data       <binary data>


   Picture type:  $00  Other
                  $01  32x32 pixels 'file icon' (PNG only)
                  $02  Other file icon
                  $03  Cover (front)
                  $04  Cover (back)
                  $05  Leaflet page
                  $06  Media (e.g. label side of CD)
                  $07  Lead artist/lead performer/soloist
                  $08  Artist/performer
                  $09  Conductor
                  $0A  Band/Orchestra
                  $0B  Composer
                  $0C  Lyricist/text writer
                  $0D  Recording Location
                  $0E  During recording
                  $0F  During performance
                  $10  Movie/video screen capture
                  $11  A bright coloured fish
                  $12  Illustration
                  $13  Band/artist logotype
                  $14  Publisher/Studio logotype
 */

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AttachedPictureFrame extends AbstractFrame {

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

    private String mimeType;
    private String description;

    private byte encoding;
    private byte pictureType;
    private byte[] pictureBytes;

    @Override
    public final byte[] assemble(byte version) {

        byte[] mimeType = TextEncoding.getStringBytes(getMimeType(), TextEncoding.ENCODING_LATIN_1);
        byte[] description = TextEncoding.getStringBytes(getDescription(), encoding);
        int size = 1 + mimeType.length + 1 + description.length + pictureBytes.length;

        byte[] frame = new byte[size];

        final int encodingOffset = 0;
        final int mimeTypeOffset = 1;
        final int pictureTypeOffset = mimeTypeOffset + mimeType.length;
        final int descriptionOffset = pictureTypeOffset + 1;
        final int pictureDataOffset = descriptionOffset + description.length;

        frame[encodingOffset] = encoding;
        frame[pictureTypeOffset] = pictureType;

        System.arraycopy(mimeType, 0, frame, mimeTypeOffset, mimeType.length);
        System.arraycopy(description, 0, frame, descriptionOffset, description.length);
        System.arraycopy(pictureBytes, 0, frame, pictureDataOffset, pictureBytes.length);

        this.frameBytes = frame;

        header = FrameHeader.newBuilder(header)
                .setFrameSize(getBytes().length)
                .build(version);

        return frame;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s, DESCRIPTION: %s, MIME_TYPE: %s, PICTURE_TYPE: %s, ENCODING: %d, SIZE: %s",
                getIdentifier(), getDescription(), getMimeType(), getPictureType(), getEncoding(), getHeader().getFrameSize()
        );
    }

    public byte[] getPictureBytes() {
        return pictureBytes;
    }

    public byte getPictureType() {
        return pictureType;
    }
    public byte getEncoding() {
        return encoding;
    }

    public String getDescription() {
        return description;
    }
    public String getMimeType() {
        return mimeType;
    }

    public boolean isPictureURL() {
        return mimeType.equals("-->");
    }

    public URLPicture getUrlPicture() throws MalformedURLException {
        if (!isPictureURL()) throw new IllegalStateException("Frame does not contain url");
        return new URLPicture(pictureBytes);
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setEncoding(byte encoding) {
        if (!TextEncoding.isValidEncodingByte(encoding)) {
            throw new IllegalArgumentException("Invalid encoding");
        }
        this.encoding = encoding;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPictureType(byte pictureType) {
        if (pictureType >= PICTURE_TYPE_OTHER && pictureType <= PICTURE_TYPE_STUDIO_LOGO) {
            this.pictureType = pictureType;
            return;
        }
        throw new IllegalArgumentException("Invalid picture type: " + pictureType);
    }

    public void setPictureURL(URL url) {
        if (!mimeType.equals("-->")) setMimeType("-->");
        pictureBytes = url.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    public void setPictureData(File file) throws IOException {

        String contentType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
        if (!isValidMimeType(contentType)) {
            throw new IllegalArgumentException("Invalid mime type: " + contentType);
        }

        setMimeType(contentType);

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (FileInputStream in = new FileInputStream(file)) {
            while (in.read(buffer, 0, buffer.length) != -1) {
                out.write(buffer, 0, buffer.length);
            }
        } finally {
            this.pictureBytes = out.toByteArray();
        }
    }

    public void setPictureData(byte[] pictureData) {
        this.pictureBytes = pictureData;
    }

    public void setPictureData(URL url) throws IOException {

        URLConnection urlConnection = url.openConnection();
        String contentType = urlConnection.getContentType();

        if (!isValidMimeType(contentType)) throw new IllegalArgumentException("Invalid mime type: " + contentType);

        mimeType = contentType;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (InputStream in = urlConnection.getInputStream()) {
            int data;
            while ((data = in.read()) != -1) {
                out.write(data);
            }
        } finally {
            this.pictureBytes = out.toByteArray();
        }
    }

    public static Builder newBuilder() { return new AttachedPictureFrame().new Builder(); }
    public static Builder newBuilder(AttachedPictureFrame frame) { return frame.new Builder(); }

    private static boolean isValidMimeType(String mimeType) {
        return mimeType.substring(0, mimeType.indexOf("/")).equals("image");
    }

    public static AttachedPictureFrame createInstance(String description, String mimeType, byte pictureType, byte[] buffer, byte version) {
        return AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(PICTURE, version))
                .setEncoding(TextEncoding.getAppropriateEncoding(version))
                .setDescription(description)
                .setMimeType(mimeType)
                .setPictureType(pictureType)
                .setPictureData(buffer)
                .build(version);
    }

    public class Builder {

        public Builder setHeader(FrameHeader frameHeader) {
            if (!frameHeader.getIdentifier().equals(PICTURE)) {
                throw new IllegalArgumentException("Invalid frame identifier");
            }
            AttachedPictureFrame.this.header = frameHeader;
            return this;
        }

        public Builder setMimeType(String mimeType) {
            AttachedPictureFrame.this.setMimeType(mimeType);
            return this;
        }

        public Builder setEncoding(byte encoding) {
            AttachedPictureFrame.this.setEncoding(encoding);
            return this;
        }

        public Builder setDescription(String description) {
            AttachedPictureFrame.this.setDescription(description);
            return this;
        }

        public Builder setPictureType(byte pictureType) {
            AttachedPictureFrame.this.setPictureType(pictureType);
            return this;
        }

        public Builder setPictureData(URL url) throws IOException {
            AttachedPictureFrame.this.setPictureData(url);
            return this;
        }

        public Builder setPictureData(File file) throws IOException {
            AttachedPictureFrame.this.setPictureData(file);
            return this;
        }

        public Builder setPictureData(byte[] pictureData) {
            AttachedPictureFrame.this.setPictureData(pictureData);
            return this;
        }

        public AttachedPictureFrame build(byte[] frameData) {
            AttachedPictureFrame.this.frameBytes = frameData;
            return AttachedPictureFrame.this;
        }

        public AttachedPictureFrame build(byte version) {
            assemble(version);
            return AttachedPictureFrame.this;
        }
    }
}
