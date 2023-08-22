package com.jtagger.mp4;

import com.jtagger.AttachedPicture;
import com.jtagger.utils.IntegerUtils;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class PictureAtom extends ItunesAtom<AttachedPicture> {

    private AttachedPicture picture;
    private int atomType;

    PictureAtom(String type, byte[] data, int atomType) {
        super(type, data);
        this.atomType = atomType;
    }

    public PictureAtom(String type) {
        super(type);
        this.atomType = TYPE_JPEG;
    }

    @Override
    public byte[] assemble(byte version) {

        int index = 0;
        byte[] pictureBytes = picture.getPictureData();

        byte[] dataAtom   = new byte[8 + 8 + pictureBytes.length];
        byte[] itunesAtom = new byte[dataAtom.length + 8];

        System.arraycopy(IntegerUtils.fromUInt32BE(dataAtom.length), 0, dataAtom, 0, 4); index += 4;
        System.arraycopy("data".getBytes(ISO_8859_1), 0, dataAtom, index, 4); index += 4;

        System.arraycopy(IntegerUtils.fromUInt32BE(getAtomType()), 0, dataAtom, index, 4); index += 4;
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
    public int getAtomType() {
        return atomType;
    }

    @Override
    public void setAtomData(byte[] pictureData) {

        AttachedPicture picture = new AttachedPicture();
        picture.setPictureData(pictureData);

        if (ItunesAtom.TYPE_PNG  == atomType) picture.setMimeType("image/png");
        if (ItunesAtom.TYPE_BMP  == atomType) picture.setMimeType("image/bmp");
        if (ItunesAtom.TYPE_JPEG == atomType) picture.setMimeType("image/jpeg");
        this.picture = picture;
    }

    @Override
    public void setAtomData(AttachedPicture picture) {
        this.picture    = picture;
        String mimeType = picture.getMimeType();
        if (mimeType.equals("image/png"))  atomType = ItunesAtom.TYPE_PNG;
        if (mimeType.equals("image/bmp"))  atomType = ItunesAtom.TYPE_BMP;
        if (mimeType.equals("image/jpeg")) atomType = ItunesAtom.TYPE_JPEG;
    }
}
