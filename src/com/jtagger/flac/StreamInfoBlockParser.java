package com.jtagger.flac;

import java.util.Arrays;

import static com.jtagger.utils.IntegerUtils.*;

public class StreamInfoBlockParser implements BlockBodyParser<StreamInfoBlock> {

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b: bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Override
    public StreamInfoBlock parse(byte[] block) {

        short minBlockSize = toUInt16BE(Arrays.copyOfRange(block, 0, 2));
        short maxBlockSize = toUInt16BE(Arrays.copyOfRange(block, 2, 4));

        int minFrameSize = toUInt24BE(Arrays.copyOfRange(block, 4, 7));
        int maxFrameSize = toUInt24BE(Arrays.copyOfRange(block, 7, 10));
        int bits         = toUInt24BE(Arrays.copyOfRange(block, 10, 13));
        int sampleRate   = bits >> 4;

        byte channels      = (byte) ((bits >> 1 & 0x7) + 1); bits = toUInt32BE(Arrays.copyOfRange(block,10, 14));
        byte bitsPerSample = (byte) (((bits >> 4) & 0x1f) + 1);
        int totalSamples   = toUInt32BE(Arrays.copyOfRange(block, 14, 18));

        byte[] signature = Arrays.copyOfRange(block, 18, 34);
        String md5 = toHexString(signature);

        return StreamInfoBlock.newBuilder()
                .setMinBlockSize(minBlockSize)
                .setMaxBlockSize(maxBlockSize)
                .setMinFrameSize(minFrameSize)
                .setMaxFrameSize(maxFrameSize)
                .setSampleRate(sampleRate)
                .setNumOfChannels(channels)
                .setBitsPerSample(bitsPerSample)
                .setTotalSamples(totalSamples)
                .setSignature(md5)
                .build(block);
    }
}
