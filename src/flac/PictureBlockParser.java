package flac;

import java.util.Arrays;
import static utils.IntegerUtils.toUInt32BE;

public class PictureBlockParser implements BlockBodyParser<PictureBlock> {

    @Override
    public PictureBlock parse(byte[] block) {

        PictureBlock pictureBlock = new PictureBlock();

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

        pictureBlock.setPictureType(pictureType);
        pictureBlock.setMimeType(mimeType);
        pictureBlock.setDescription(description);
        pictureBlock.setPictureWidth(width);
        pictureBlock.setPictureHeight(height);
        pictureBlock.setPictureColorDepth(colorDepth);
        pictureBlock.setPictureData(pictureData);

        pictureBlock.assemble();
        return pictureBlock;
    }
}
