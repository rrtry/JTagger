package com.rrtry.flac;

import com.rrtry.utils.IntegerUtils;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class VorbisCommentBlockParser implements BlockBodyParser<VorbisCommentBlock> {

    @Override
    public VorbisCommentBlock parse(byte[] block) {

        VorbisCommentBlock vorbisComment = new VorbisCommentBlock();

        int vendorStringLength = IntegerUtils.toUInt32LE(Arrays.copyOfRange(block, 0, 4));
        int commentsLength     = IntegerUtils.toUInt32LE(Arrays.copyOfRange(block, 4 + vendorStringLength, 4 + vendorStringLength + 4));
        String vendorString = new String(Arrays.copyOfRange(block, 4, 4 + vendorStringLength));

        int n = 0;
        int offset = 4 + vendorStringLength + 4;

        vorbisComment.setVendorString(vendorString);
        while (n < commentsLength) {

            byte[] commentSizeBytes = Arrays.copyOfRange(block, offset, offset + 4); offset += 4;
            int commentSize = IntegerUtils.toUInt32LE(commentSizeBytes);

            String comment = new String(Arrays.copyOfRange(block, offset, offset + commentSize), StandardCharsets.UTF_8);
            String[] split = comment.split("=");

            String field = split[0];
            String value = split[1];

            vorbisComment.setComment(field, value);
            n++; offset += commentSize;
        }
        vorbisComment.assemble();
        return vorbisComment;
    }
}
