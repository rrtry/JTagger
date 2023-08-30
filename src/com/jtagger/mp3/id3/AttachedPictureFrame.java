package com.jtagger.mp3.id3;

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

import com.jtagger.AbstractTag;
import com.jtagger.AttachedPicture;
import java.io.*;
import java.net.URL;
import java.util.Objects;

public class AttachedPictureFrame extends AbstractFrame<AttachedPicture> {

    public static final int DESCRIPTION_MAX_LENGTH = 64;
    public static final int MIME_TYPE_MAX_LENGTH   = 10;

    private AttachedPicture picture = new AttachedPicture();
    private byte encoding;

    @Override
    public String getKey() {
        byte type = getPictureType();
        return type != 0x01 && type != 0x02 ? String.format("%s:%s", getIdentifier(), getDescription()) : AbstractFrame.PICTURE;
    }

    @Override
    public final byte[] assemble(byte version) {

        byte[] pictureBytes = getPictureData();
        byte[] mimeType     = TextEncoding.getStringBytes(getMimeType(), TextEncoding.ENCODING_LATIN_1);
        byte[] description  = TextEncoding.getStringBytes(getDescription(), encoding);
        int size = 1 + mimeType.length + 1 + description.length + pictureBytes.length;

        byte[] frame = new byte[size];

        final int encodingOffset    = 0;
        final int mimeTypeOffset    = 1;
        final int pictureTypeOffset = mimeTypeOffset + mimeType.length;
        final int descriptionOffset = pictureTypeOffset + 1;
        final int pictureDataOffset = descriptionOffset + description.length;

        frame[encodingOffset]    = encoding;
        frame[pictureTypeOffset] = getPictureType();

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

    public byte[] getPictureData() {
        return picture.getPictureData();
    }

    public byte getPictureType() {
        return (byte) picture.getPictureType();
    }

    public byte getEncoding() {
        return encoding;
    }

    public String getDescription() {
        return picture.getDescription();
    }

    public String getMimeType() {
        return picture.getMimeType();
    }

    public boolean isPictureURL() {
        return picture.isPictureURL();
    }

    public void setMimeType(String mimeType) {
        picture.setMimeType(mimeType);
    }

    public void setEncoding(byte encoding) {
        if (!TextEncoding.isValidEncodingByte(encoding)) {
            throw new IllegalArgumentException("Invalid encoding");
        }
        this.encoding = encoding;
    }

    public void setDescription(String description) {
        picture.setDescription(description);
    }

    public void setPictureType(byte pictureType) {
        picture.setPictureType(pictureType);
    }

    public void setPictureURL(URL url) {
        picture.isPictureURL();
    }

    public void setPictureData(byte[] pictureData) {
        picture.setPictureData(pictureData);
    }

    public void setPictureData(File file) {
        picture.setPictureData(file);
    }

    public void setPictureData(URL url) {
        picture.setPictureData(url);
    }

    public static Builder newBuilder() { return new AttachedPictureFrame().new Builder(); }
    public static Builder newBuilder(AttachedPictureFrame frame) { return frame.new Builder(); }

    public static AttachedPictureFrame createInstance(String description, String mimeType, byte pictureType, byte[] buffer, byte version) {
        return AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(PICTURE, version))
                .setEncoding(TextEncoding.getEncodingForVersion(version))
                .setDescription(description)
                .setMimeType(mimeType)
                .setPictureType(pictureType)
                .setPictureData(buffer)
                .build(version);
    }

    @Override
    public AttachedPicture getFrameData() {
        return picture;
    }

    @Override
    public void setFrameData(AttachedPicture picture) {
        this.picture = picture;
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

        public Builder setPictureData(URL url) {
            AttachedPictureFrame.this.setPictureData(url);
            return this;
        }

        public Builder setPictureData(File file) {
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
