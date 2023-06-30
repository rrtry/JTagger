package com.jtagger.mp4;

import com.jtagger.AttachedPicture;
import com.jtagger.utils.IntegerUtils;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class PictureAtom extends MP4Atom implements ItunesAtom<AttachedPicture> {

    private AttachedPicture picture;

    public PictureAtom(String type, byte[] data) {
        super(type, data);
    }

    public PictureAtom(String type) {
        super(type);
    }

    @Override
    public byte[] assemble(byte version) {

        int index = 0;
        byte[] pictureBytes = picture.getPictureData();

        byte[] dataAtom   = new byte[8 + 8 + pictureBytes.length];
        byte[] itunesAtom = new byte[dataAtom.length + 8];

        System.arraycopy(IntegerUtils.fromUInt32BE(dataAtom.length), 0, dataAtom, 0, 4); index += 4;
        System.arraycopy("data".getBytes(ISO_8859_1), 0, dataAtom, index, 4); index += 4;

        System.arraycopy(IntegerUtils.fromUInt32BE(TYPE_JPEG), 0, dataAtom, index, 4); index += 4;
        System.arraycopy(IntegerUtils.fromUInt32BE(0x000000), 0, dataAtom, index, 4); index += 4;
        System.arraycopy(pictureBytes, 0, dataAtom, index, pictureBytes.length);

        System.arraycopy(IntegerUtils.fromUInt32BE(itunesAtom.length), 0, itunesAtom, 0, 4);
        System.arraycopy(getType().getBytes(ISO_8859_1), 0, itunesAtom, 4, 4);
        System.arraycopy(dataAtom, 0, itunesAtom, 8, dataAtom.length);

        this.data = itunesAtom;
        return itunesAtom;
    }

    @Override
    public AttachedPicture getAtomData() {
        return picture;
    }

    @Override
    public void setAtomData(byte[] pictureData) {
        AttachedPicture picture = new AttachedPicture();
        picture.setPictureData(pictureData);
        this.picture = picture;
    }

    @Override
    public void setAtomData(AttachedPicture picture) {
        this.picture = picture;
    }
}
