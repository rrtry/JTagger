package com.jtagger.mp3.id3;

public class UnsynchronisedLyricsFrame extends CommentFrame {

    @Override
    public String toString() {
        return String.format(
                "ID: USLT, LANG: %s, DESC: %s, COMMENT: %s, ENCODING: %d",
                language, description, text, encoding
        );
    }

    public static Builder newBuilder() {
        return new UnsynchronisedLyricsFrame().new Builder();
    }

    public static Builder newBuilder(UnsynchronisedLyricsFrame frame) {
        return frame.new Builder();
    }

    public static UnsynchronisedLyricsFrame createInstance(String lyrics, String language, byte version) {
        return (UnsynchronisedLyricsFrame) UnsynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(U_LYRICS, version))
                .setLanguage(language)
                .setDescription("")
                .setText(lyrics)
                .build(version);
    }
}
