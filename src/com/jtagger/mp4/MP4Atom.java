package com.jtagger.mp4;

import com.jtagger.Component;
import com.jtagger.utils.IntegerUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MP4Atom implements Component {

    private final String type;
    protected byte[] data;

    private ArrayList<MP4Atom> childAtoms = new ArrayList<>();
    private boolean isTopLevelAtom = false;

    public MP4Atom(String type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public MP4Atom(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    @Override
    public byte[] assemble(byte version) {

        ArrayList<Byte> byteList = new ArrayList<>();
        boolean isMeta = type.equals("meta");

        if (hasChildAtoms()) {

            for (MP4Atom atom : childAtoms) {
                byte[] childAtom = atom.assemble();
                byteList.addAll(
                        IntStream.range(0, childAtom.length).mapToObj(i -> childAtom[i])
                                .collect(Collectors.toList())
                );
            }

            byte[] atom = new byte[byteList.size() + 8 + (isMeta ? 4 : 0)];
            System.arraycopy(IntegerUtils.fromUInt32BE(atom.length), 0, atom, 0, 4);
            System.arraycopy(type.getBytes(ISO_8859_1), 0, atom, 4, 4);

            if (isMeta) {
                for (int i = 8; i < 12; i++) {
                    atom[i] = 0x0;
                }
            }

            for (int i = (isMeta ? 12 : 8); i < atom.length; i++) {
                atom[i] = byteList.get(i - (isMeta ? 12 : 8));
            }

            this.data = atom;
            return atom;
        }
        return data;
    }

    @Override
    public byte[] getBytes() {
        return getData();
    }

    public String getType() {
        return type;
    }

    public boolean isTopLevelAtom() {
        return isTopLevelAtom;
    }

    public boolean hasChildAtoms() {
        return childAtoms.size() > 0;
    }

    public <T extends MP4Atom> ArrayList<T> getChildAtoms() {
        return (ArrayList<T>) childAtoms;
    }

    public <T extends MP4Atom> void setChildAtoms(ArrayList<T> childAtoms) {
        if (childAtoms.isEmpty()) {
            removeAllChildAtoms(); return;
        }
        this.childAtoms = (ArrayList<MP4Atom>) childAtoms;
    }

    public void appendChildAtom(MP4Atom atom) {
        if (!childAtoms.contains(atom)) childAtoms.add(atom);
    }

    public void setTopLevelAtom(boolean isTopLevelAtom) {
        this.isTopLevelAtom = isTopLevelAtom;
    }

    public void removeAllChildAtoms() {

        this.childAtoms = new ArrayList<>();
        data = new byte[8];

        System.arraycopy(IntegerUtils.fromUInt32BE(data.length), 0, data, 0, 4);
        System.arraycopy(type.getBytes(ISO_8859_1), 0, data, 4, 4);
    }

    public byte[] getData() {
        return data;
    }
}
