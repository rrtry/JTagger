package com.jtagger.mp3;

public class MpegFrame {

    private final MpegFrameHeader header;
    private final byte[] frameBody;

    public MpegFrame(MpegFrameHeader header, byte[] frameBody) {
        this.header    = header;
        this.frameBody = frameBody;
    }

    public MpegFrameHeader getMpegHeader() {
        return header;
    }

    public byte[] getFrameBody() {
        return frameBody;
    }
}
