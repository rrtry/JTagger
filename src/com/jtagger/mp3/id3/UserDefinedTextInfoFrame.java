package com.jtagger.mp3.id3;

import java.nio.charset.Charset;
import java.util.Arrays;

import static com.jtagger.mp3.id3.TextEncoding.getStringLength;

public class UserDefinedTextInfoFrame extends TextFrame {

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Builder newBuilder() {
        return new UserDefinedTextInfoFrame().new Builder();
    }

    @Override
    public String getKey() {
        return String.format("%s:%s", getIdentifier(), getDescription());
    }

    @Override
    public String toString() {
        return String.format("ID: %s, DESCRIPTION: %s, TEXT: %s, ENCODING: %d, SIZE: %d",
                getIdentifier(), getDescription(), getText(), getEncoding(), getHeader().getFrameSize()
        );
    }

    @Override
    public byte[] assemble(byte version) {

        int position = 0;
        byte[] textBuffer = TextEncoding.getStringBytes(getText(), getEncoding());
        byte[] descBuffer = TextEncoding.getStringBytes(description, getEncoding());
        byte[] frame      = new byte[1 + textBuffer.length + descBuffer.length];

        frame[position++] = getEncoding();
        System.arraycopy(descBuffer, 0, frame, position, descBuffer.length); position += descBuffer.length;
        System.arraycopy(textBuffer, 0, frame, position, textBuffer.length);

        this.frameBytes = frame;
        header = FrameHeader.newBuilder(header)
                .setFrameSize(getBytes().length)
                .build(version);

        return frame;
    }

    @Override
    public void parseFrameData(byte[] buffer, FrameHeader header) {

        int position = 0;
        int strLength;

        byte encoding;
        String description;
        String text;

        encoding    = buffer[position++];
        strLength   = getStringLength(buffer, position, encoding);
        description = TextEncoding.getString(buffer, position, strLength, encoding);
        position += strLength;

        text = TextEncoding.getString(
                buffer, position, buffer.length - position, encoding
        );

        this.header = header;
        setEncoding(encoding);
        setDescription(description);
        setText(text);
    }

    public class Builder extends TextFrame.Builder<Builder, UserDefinedTextInfoFrame> {

        @Override
        public Builder setHeader(FrameHeader frameHeader) {
            if (!frameHeader.getIdentifier().equals(AbstractFrame.CUSTOM)) {
                throw new IllegalArgumentException("Frame should have id 'TXXX'");
            }
            header = frameHeader;
            return this;
        }

        public Builder setDescription(String description) {
            UserDefinedTextInfoFrame.this.setDescription(description);
            return this;
        }
    }
}
