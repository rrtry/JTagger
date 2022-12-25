package com.rrtry.flac;

import com.rrtry.AttachedPicture;

import java.util.Arrays;
import static com.rrtry.utils.IntegerUtils.toUInt32BE;

public class PictureBlockParser implements BlockBodyParser<PictureBlock> {

    @Override
    public PictureBlock parse(byte[] block) {

        PictureBlock pictureBlock = new PictureBlock();
        AttachedPicture picture   = new AttachedPicture();

        int index          = 0;
        int pictureType    = toUInt32BE(Arrays.copyOfRange(block, index, index + 4)); index += 4;
        int mimeTypeLength = toUInt32BE(Arrays.copyOfRange(block, index, index + 4)); index += 4;

        String mimeType = new String(Arrays.copyOfRange(block, index, index + mimeTypeLength));
        index += mimeTypeLength;

        int descriptionLength = toUInt32BE(Arrays.copyOfRange(block, index, index + 4));
        index += 4;

        String description = new String(Arrays.copyOfRange(block, index, index + descriptionLength));
        index += descriptionLength;

        int width      = toUInt32BE(Arrays.copyOfRange(block, index, index + 4)); index += 4;
        int height     = toUInt32BE(Arrays.copyOfRange(block, index, index + 4)); index += 4;
        int colorDepth = toUInt32BE(Arrays.copyOfRange(block, index, index + 4)); index += 8;

        int pictureLength = toUInt32BE(Arrays.copyOfRange(block, index, index + 4)); index += 4;
        byte[] pictureData = Arrays.copyOfRange(block, index, index + pictureLength);

        picture.setPictureType(pictureType);
        picture.setMimeType(mimeType);
        picture.setDescription(description);
        picture.setPictureWidth(width);
        picture.setPictureHeight(height);
        picture.setPictureColorDepth(colorDepth);
        picture.setPictureData(pictureData);

        pictureBlock.setPicture(picture);
        pictureBlock.assemble();
        return pictureBlock;
    }
}
