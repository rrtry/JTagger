package flac;

/*
METADATA_BLOCK_PICTURE
<32> 	The picture type according to the ID3v2 APIC frame:

    0 - Other
    1 - 32x32 pixels 'file icon' (PNG only)
    2 - Other file icon
    3 - Cover (front)
    4 - Cover (back)
    5 - Leaflet page
    6 - Media (e.g. label side of CD)
    7 - Lead artist/lead performer/soloist
    8 - Artist/performer
    9 - Conductor
    10 - Band/Orchestra
    11 - Composer
    12 - Lyricist/text writer
    13 - Recording Location
    14 - During recording
    15 - During performance
    16 - Movie/video screen capture
    17 - A bright coloured fish
    18 - Illustration
    19 - Band/artist logotype
    20 - Publisher/Studio logotype

    Others are reserved and should not be used. There may only be one each of picture type 1 and 2 in a file.
    <32> 	The length of the MIME type string in bytes.
    <n*8> 	The MIME type string, in printable ASCII characters 0x20-0x7e. The MIME type may also be --> to signify that the data part is a URL of the picture instead of the picture data itself.
    <32> 	The length of the description string in bytes.
    <n*8> 	The description of the picture, in UTF-8.
    <32> 	The width of the picture in pixels.
    <32> 	The height of the picture in pixels.
    <32> 	The color depth of the picture in bits-per-pixel.
    <32> 	For indexed-color pictures (e.g. GIF), the number of colors used, or 0 for non-indexed pictures.
    <32> 	The length of the picture data in bytes.
    <n*8> 	The binary picture data.
 */

import com.rrtry.id3.URLPicture;
import utils.ImageReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.io.File;
import java.net.URL;

import static utils.IntegerUtils.fromUInt32BE;

public class PictureBlock extends AbstractMetadataBlock {

    private int pictureType;

    private String mimeType;
    private String description;

    private int width;
    private int height;
    private int colorDepth;

    private byte[] pictureData;

    public int getPictureType() {
        return pictureType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getDescription() {
        return description;
    }

    public int getPictureWidth() {
        return width;
    }

    public int getPictureHeight() {
        return height;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public byte[] getPictureData() {
        return pictureData;
    }

    public void setPictureType(int type) {
        this.pictureType = type;
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
        setMimeType("-->");
        this.pictureData = url.toString().getBytes(StandardCharsets.US_ASCII);
    }

    public void setPictureFromFile(File file) {
        setMimeType(ImageReader.getMimeType(file));
        this.pictureData = ImageReader.readFromFile(file);
    }

    public void setPictureFromURL(URL url) {
        setMimeType(ImageReader.getMimeType(url));
        this.pictureData = ImageReader.readFromURL(url);
    }

    public boolean isPictureURL() {
        return mimeType.equals("-->");
    }

    public URLPicture getURLPicture() {
        try {
            if (!isPictureURL()) throw new IllegalStateException("Block does not contain url");
            return new URLPicture(pictureData);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public byte[] assemble(byte version) {

        byte[] mimeTypeBytes    = mimeType.getBytes(StandardCharsets.US_ASCII);
        byte[] descriptionBytes = description.getBytes(StandardCharsets.UTF_8);

        int size = 4 * 8 + pictureData.length + mimeTypeBytes.length + descriptionBytes.length;
        byte[] block = new byte[size];

        final int pictureTypeOffset       = 0;
        final int mimeTypeLengthOffset    = 4;
        final int mimeTypeOffset          = 8;
        final int descriptionLengthOffset = mimeTypeOffset + mimeTypeBytes.length;
        final int descriptionOffset       = descriptionLengthOffset + 4;
        final int widthOffset             = descriptionOffset + descriptionBytes.length;
        final int heightOffset            = widthOffset + 4;
        final int colorDepthOffset        = heightOffset + 4;
        final int pictureDataLengthOffset = colorDepthOffset + 8;
        final int pictureDataOffset       = pictureDataLengthOffset + 4;

        System.arraycopy(fromUInt32BE(pictureType), 0, block, pictureTypeOffset, 4);
        System.arraycopy(fromUInt32BE(mimeTypeBytes.length), 0, block, mimeTypeLengthOffset, 4);
        System.arraycopy(mimeTypeBytes, 0, block, mimeTypeOffset, mimeTypeBytes.length);
        System.arraycopy(fromUInt32BE(descriptionBytes.length), 0, block, descriptionLengthOffset, 4);
        System.arraycopy(descriptionBytes, 0, block, descriptionOffset, descriptionBytes.length);
        System.arraycopy(fromUInt32BE(width), 0, block, widthOffset, 4);
        System.arraycopy(fromUInt32BE(height), 0, block, heightOffset, 4);
        System.arraycopy(Arrays.copyOf(fromUInt32BE(colorDepth), 8), 0, block, colorDepthOffset, 8);
        System.arraycopy(fromUInt32BE(pictureData.length), 0, block, pictureDataLengthOffset, 4);
        System.arraycopy(pictureData, 0, block, pictureDataOffset, pictureData.length);

        this.blockBody = block;
        return block;
    }

    @Override
    public int getBlockType() {
        return AbstractMetadataBlock.BLOCK_TYPE_PICTURE;
    }
}
