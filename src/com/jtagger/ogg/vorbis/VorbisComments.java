package com.jtagger.ogg.vorbis;

import com.jtagger.AttachedPicture;
import com.jtagger.AbstractTag;
import com.jtagger.flac.PictureBlock;
import com.jtagger.flac.PictureBlockParser;
import com.jtagger.utils.IntegerUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class VorbisComments extends AbstractTag {

    public static final String VENDOR_STRING = "reference libFLAC 1.3.1 20141125";
    public static final String PICTURE       = "METADATA_BLOCK_PICTURE";

    private String vendorString = VENDOR_STRING;
    private LinkedHashMap<String, String> commentsMap = new LinkedHashMap<>();

    private byte[] bytes;
    private boolean framingBit = false;

    public VorbisComments() {
        /* empty constructor */
    }

    public VorbisComments(boolean framingBit) {
        this.framingBit = framingBit;
    }

    public void setFramingBit(boolean framingBit) {
        this.framingBit = framingBit;
    }

    private int getSize() {
        int size = framingBit ? 1 : 0;
        for (Map.Entry<String, String> entry : commentsMap.entrySet()) {
            size += entry.getKey().getBytes(StandardCharsets.US_ASCII).length +
                    entry.getValue().getBytes(StandardCharsets.UTF_8).length + 1 + 4;
        }
        return size;
    }

    public String getVendorString() {
        return vendorString;
    }

    public String getComment(String field) {
        return commentsMap.getOrDefault(field.toUpperCase(), "");
    }

    public void setVendorString(String vendorString) {
        this.vendorString = vendorString;
    }

    public void setCommentsMap(LinkedHashMap<String, String> commentsMap) {
        this.commentsMap = commentsMap;
    }

    public void setComment(String field, String value) {
        commentsMap.put(field.toUpperCase(), value);
    }

    public void removeComment(String field) {
        commentsMap.remove(field.toUpperCase());
    }

    @Override
    public byte[] assemble(byte version) {

        byte[] vendorStringLength = IntegerUtils.fromUInt32LE(VENDOR_STRING.length());
        byte[] vendorStringBytes  = VENDOR_STRING.getBytes(StandardCharsets.UTF_8);
        byte[] numOfComments     = IntegerUtils.fromUInt32LE(commentsMap.keySet().size());

        int index = 4 + vendorStringBytes.length + 4;

        final int size = getSize() + index;
        final int vendorStringOffset  = 4;
        final int numOfCommentsOffset = vendorStringOffset + vendorStringBytes.length;

        byte[] comments = new byte[size];

        System.arraycopy(vendorStringLength, 0, comments, 0, vendorStringLength.length);
        System.arraycopy(vendorStringBytes, 0, comments, vendorStringOffset, vendorStringBytes.length);
        System.arraycopy(numOfComments, 0, comments, numOfCommentsOffset, numOfComments.length);

        for (Map.Entry<String, String> entry : commentsMap.entrySet()) {

            String field = entry.getKey();
            String value = entry.getValue();

            byte[] fieldBytes = field.getBytes(StandardCharsets.US_ASCII);
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
            byte separator    = 0x3d;

            final int commentSize = fieldBytes.length + valueBytes.length + 1;

            byte[] commentSizeBytes = IntegerUtils.fromUInt32LE(commentSize);
            byte[] comment          = new byte[commentSize + 4];

            final int fieldOffset     = 4;
            final int separatorOffset = fieldOffset + fieldBytes.length;
            final int valueOffset     = separatorOffset + 1;

            System.arraycopy(commentSizeBytes, 0, comment, 0, commentSizeBytes.length);
            System.arraycopy(fieldBytes, 0, comment, fieldOffset, fieldBytes.length);
            System.arraycopy(valueBytes, 0, comment, valueOffset, valueBytes.length);
            comment[separatorOffset] = separator;

            System.arraycopy(comment, 0, comments, index, comment.length);
            index += comment.length;
        }

        if (framingBit) {
            comments[comments.length - 1] = 0x1;
        }

        this.bytes = comments;
        return comments;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    protected <T> void setFieldValue(String fieldId, T value) {
        if (fieldId.equals(AbstractTag.PICTURE)) {

            PictureBlock pictureBlock = new PictureBlock();
            pictureBlock.setPicture((AttachedPicture) value);

            setComment(
                    VorbisComments.PICTURE,
                    Base64.getEncoder().encodeToString(pictureBlock.assemble())
            );
            return;
        }
        setComment(fieldId, (String) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getFieldValue(String fieldId) {
        if (fieldId.equals(AbstractTag.PICTURE)) {

            String value = getComment(VorbisComments.PICTURE);
            if (value.isEmpty()) return null;

            PictureBlockParser parser = new PictureBlockParser();
            PictureBlock pictureBlock = parser.parse(Base64.getDecoder().decode(value));

            return (T) pictureBlock.getPicture();
        }
        return (T) getComment(fieldId);
    }

    @Override
    public void removeField(String fieldId) {
        if (AbstractTag.PICTURE.equals(fieldId)) {
            fieldId = PICTURE;
        }
        removeComment(fieldId);
    }
}
