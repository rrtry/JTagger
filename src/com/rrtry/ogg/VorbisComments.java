package com.rrtry.ogg;

import com.rrtry.Component;
import com.rrtry.utils.IntegerUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class VorbisComments implements Component {

    public static final String VENDOR_STRING = "reference libFLAC 1.3.1 20141125";

    private String vendorString = VENDOR_STRING;
    private LinkedHashMap<String, String> commentsMap = new LinkedHashMap<>();

    private byte[] bytes;

    private int getSize() {
        int size = 0;
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

        byte[] block = new byte[size];

        System.arraycopy(vendorStringLength, 0, block, 0, vendorStringLength.length);
        System.arraycopy(vendorStringBytes, 0, block, vendorStringOffset, vendorStringBytes.length);
        System.arraycopy(numOfComments, 0, block, numOfCommentsOffset, numOfComments.length);

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

            System.arraycopy(comment, 0, block, index, comment.length);
            index += comment.length;
        }

        this.bytes = block;
        return block;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
