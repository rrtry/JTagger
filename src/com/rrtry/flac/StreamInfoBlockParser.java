package com.rrtry.flac;

import java.util.Arrays;

import static com.rrtry.utils.IntegerUtils.*;

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
        int sampleRate   = toUInt24BE(Arrays.copyOfRange(block, 10, 13)) >> 4;

        byte channels      = (byte) (block[12] >> 5);
        byte bitsPerSample = (byte) (block[12] >> 2);
        int totalSamples   = toUInt32BE(Arrays.copyOfRange(block, 14, 18));

        byte[] signature = Arrays.copyOfRange(block, 18, 34);
        String md5 = toHexString(signature);

        return StreamInfoBlock.createBuilder()
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
