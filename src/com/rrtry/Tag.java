package com.rrtry;

import com.rrtry.flac.PictureBlock;
import com.rrtry.flac.PictureBlockParser;

import java.util.Base64;
import java.util.LinkedHashMap;

public class Tag extends AbstractTag {

    private final LinkedHashMap<String, String> fields = new LinkedHashMap<>();

    @Override
    protected <T> void setFieldValue(String fieldId, T value) {
        if (AbstractTag.PICTURE.equals(fieldId)) {

            PictureBlock pictureBlock = new PictureBlock();
            pictureBlock.setPicture((AttachedPicture) value);

            fields.put(
                    AbstractTag.PICTURE,
                    Base64.getEncoder().encodeToString(pictureBlock.assemble())
            );
            return;
        }
        fields.put(fieldId, (String) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getFieldValue(String fieldId) {
        if (Tag.PICTURE.equals(fieldId)) {

            String value = fields.getOrDefault(AbstractTag.PICTURE, "");
            if (value.isEmpty()) return null;

            PictureBlockParser parser = new PictureBlockParser();
            PictureBlock pictureBlock = parser.parse(Base64.getDecoder().decode(value));

            return (T) pictureBlock.getPicture();
        }
        return (T) fields.get(fieldId);
    }

    @Override
    public void removeField(String fieldId) {
        fields.remove(fieldId);
    }

    @Override
    public byte[] assemble(byte version) {
        return new byte[0];
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
