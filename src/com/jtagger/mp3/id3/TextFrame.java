package com.jtagger.mp3.id3;

public class TextFrame extends AbstractFrame<String> {

    private byte encoding;
    private String text;

    @Override
    public String toString() {
        return String.format("ID: %s, TEXT: %s, ENCODING: %d, SIZE: %d",
                getIdentifier(), getText(), getEncoding(), getHeader().getFrameSize()
        );
    }

    @Override
    public byte[] assemble(byte version) {

        byte[] textBuffer = TextEncoding.getStringBytes(text, encoding);
        byte[] frame = new byte[textBuffer.length + 1];
        frame[0] = encoding;

        System.arraycopy(textBuffer, 0, frame, 1, textBuffer.length);
        this.frameBytes = frame;

        header = FrameHeader.newBuilder(header)
                .setFrameSize(getBytes().length)
                .build(version);

        return frame;
    }

    @Override
    public String getFrameData() {
        return text;
    }

    @Override
    public void setFrameData(String text) {
        setText(text);
    }

    public String getText() {
        return text;
    }

    public byte getEncoding() {
        return encoding;
    }

    public void setEncoding(byte encoding) {
        if (!TextEncoding.isValidEncodingByte(encoding)) {
            throw new IllegalArgumentException("Invalid encoding");
        }
        this.encoding = encoding;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static Builder newBuilder() { return new TextFrame().new Builder(); }
    public static Builder newBuilder(TextFrame frame) { return frame.new Builder(); }

    public static TextFrame createInstance(String id, String text, byte encoding, byte version) {
        return TextFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(id, version))
                .setEncoding(encoding)
                .setText(text)
                .build(version);
    }

    public class Builder {

        public Builder setHeader(FrameHeader frameHeader) {
            if (frameHeader.getIdentifier().charAt(0) != 'T') {
                throw new IllegalArgumentException("Invalid frame identifier: " + frameHeader.getIdentifier());
            }
            header = frameHeader;
            return this;
        }

        public Builder setEncoding(byte encoding) {
            TextFrame.this.setEncoding(encoding);
            return this;
        }

        public Builder setText(String text) {
            TextFrame.this.setText(text);
            return this;
        }

        public TextFrame build(byte[] frameData) {
            TextFrame.this.frameBytes = frameData;
            return TextFrame.this;
        }

        public TextFrame build(byte version) {
            assemble(version);
            return TextFrame.this;
        }
    }
}
