package com.jtagger.mp3.id3;

import java.nio.charset.StandardCharsets;

/*
    <Header for 'Comment', ID: "COMM">
     Text encoding          $xx
     Language               $xx xx xx
     Short content descrip. <text string according to encoding> $00 (00)
     The actual text        <full text string according to encoding>
*/

public class CommentFrame extends AbstractFrame<String> {

    protected byte encoding;
    protected String language;
    protected String description;
    protected String text;

    @Override
    public String toString() {
        return String.format(
                "ID: COMM, LANG: %s, DESC: %s, COMMENT: %s, ENCODING: %d",
                language, description, text, encoding
        );
    }

    @Override
    public final byte[] assemble(byte version) {

        byte[] languageBuffer    = language.getBytes(StandardCharsets.ISO_8859_1);
        byte[] descriptionBuffer = TextEncoding.getStringBytes(description, encoding);
        byte[] commentBuffer     = TextEncoding.getStringBytes(text, encoding);

        int size = 1 + languageBuffer.length + descriptionBuffer.length + commentBuffer.length;

        final int encodingOffset = 0;
        final int languageOffset = 1;
        final int descriptionOffset = languageOffset + languageBuffer.length;
        final int commentOffset = descriptionOffset + descriptionBuffer.length;

        byte[] frame = new byte[size];
        frame[encodingOffset] = encoding;

        System.arraycopy(languageBuffer, 0, frame, languageOffset, languageBuffer.length);
        System.arraycopy(descriptionBuffer, 0, frame, descriptionOffset, descriptionBuffer.length);
        System.arraycopy(commentBuffer, 0, frame, commentOffset, commentBuffer.length);

        this.frameBytes = frame;

        header = FrameHeader.newBuilder(header)
                .setFrameSize(getBytes().length)
                .build(version);

        return frame;
    }

    public String getText() {
        return text;
    }

    public void setText(String comment) {
        this.text = comment;
    }

    public void setEncoding(byte encoding) {
        if (!TextEncoding.isValidEncodingByte(encoding)) {
            throw new IllegalArgumentException("Invalid encoding: " + encoding);
        }
        this.encoding = encoding;
    }

    public void setLanguage(String language) {
        if (language.length() != 3) throw new IllegalArgumentException("Invalid language code: " + language);
        this.language = language;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static boolean isCommentFrame(String id) {
        return id.equals(COMMENT);
    }

    public static Builder newBuilder() {
        return new CommentFrame().new Builder();
    }

    public static Builder newBuilder(CommentFrame frame) {
        return frame.new Builder();
    }

    public static CommentFrame createInstance(String comment, String language, byte version) {
        return CommentFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(COMMENT, version))
                .setLanguage(language)
                .setDescription("")
                .setText(comment)
                .build(version);
    }

    @Override
    public String getFrameData() {
        return text;
    }

    @Override
    public void setFrameData(String comment) {
        this.text = comment;
    }

    public class Builder {

        public Builder setHeader(FrameHeader header) {
            CommentFrame.this.header = header;
            return this;
        }

        public Builder setText(String comment) {
            CommentFrame.this.setText(comment);
            return this;
        }

        public Builder setDescription(String description) {
            CommentFrame.this.setDescription(description);
            return this;
        }

        public Builder setEncoding(byte encoding) {
            CommentFrame.this.setEncoding(encoding);
            return this;
        }

        public Builder setLanguage(String language) {
            CommentFrame.this.setLanguage(language);
            return this;
        }

        public CommentFrame build(byte[] frameData) {
            CommentFrame.this.frameBytes = frameData;
            return CommentFrame.this;
        }

        public CommentFrame build(byte version) {
            assemble(version);
            return CommentFrame.this;
        }
    }
}
