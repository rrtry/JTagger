package flac;

import utils.IntegerUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class VorbisCommentBlock extends AbstractMetadataBlock {

    public static final String VENDOR_STRING = "reference libFLAC 1.3.1 20141125";

    public static final String TITLE        = "TITLE";
    public static final String VERSION      = "VERSION";
    public static final String ALBUM        = "ALBUM";
    public static final String TRACK_NUMBER = "TRACKNUMBER";
    public static final String ARTIST       = "ARTIST";
    public static final String PERFORMER    = "PERFORMER";
    public static final String COPYRIGHT    = "COPYRIGHT";
    public static final String LICENCE      = "LICENSE";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String DESCRIPTION  = "DESCRIPTION";
    public static final String GENRE        = "GENRE";
    public static final String DATE         = "DATE";
    public static final String LOCATION     = "LOCATION";
    public static final String CONTACT      = "CONTACT";
    public static final String ISRC         = "ISRC";

    private String vendorString = VENDOR_STRING;
    private HashMap<String, String> commentsMap = new HashMap<>();

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
        return commentsMap.getOrDefault(field, "");
    }

    public void setVendorString(String vendorString) {
        this.vendorString = vendorString;
    }

    public void setComment(String field, String value) {
        commentsMap.put(field, value);
    }

    public void removeComment(String field) {
        commentsMap.remove(field);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "METADATA BLOCK VORBIS_COMMENT, length:" + blockBody.length + ", comments: " + commentsMap.keySet().size() + '\n'
        );
        for (Map.Entry<String, String> entry : commentsMap.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        return sb.toString();
    }

    @Override
    public int getBlockType() {
        return BLOCK_TYPE_VORBIS_COMMENT;
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

        this.blockBody = block;
        return block;
    }
}
