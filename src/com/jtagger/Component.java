package com.jtagger;

public interface Component {

    default byte[] assemble() { return assemble((byte) 0x00); }
    byte[] assemble(byte version);
    byte[] getBytes();

}
