package com.rrtry.ogg;

import com.rrtry.utils.IntegerUtils;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class VorbisCommentsParser {

    public VorbisComments parse(byte[] data) {

        VorbisComments comments = new VorbisComments();

        int vendorStringLength = IntegerUtils.toUInt32LE(Arrays.copyOfRange(data, 0, 4));
        int commentsLength     = IntegerUtils.toUInt32LE(Arrays.copyOfRange(data, 4 + vendorStringLength, 4 + vendorStringLength + 4));
        String vendorString    = new String(Arrays.copyOfRange(data, 4, 4 + vendorStringLength));

        int n = 0;
        int offset = 4 + vendorStringLength + 4;

        comments.setVendorString(vendorString);
        while (n < commentsLength) {

            byte[] commentSizeBytes = Arrays.copyOfRange(data, offset, offset + 4); offset += 4;
            int commentSize = IntegerUtils.toUInt32LE(commentSizeBytes);

            String comment = new String(Arrays.copyOfRange(data, offset, offset + commentSize), StandardCharsets.UTF_8);
            String[] split = comment.split("=");

            String field = split[0];
            String value = split[1];

            comments.setComment(field, value);
            n++; offset += commentSize;
        }
        comments.assemble();
        return comments;
    }
}
