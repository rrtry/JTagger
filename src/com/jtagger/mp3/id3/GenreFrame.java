package com.jtagger.mp3.id3;

import java.util.ArrayList;

import static com.jtagger.mp3.id3.ID3V1Tag.UNKNOWN;
import static com.jtagger.mp3.id3.TextEncoding.isNumeric;

public class GenreFrame extends TextFrame {

    public static final String REMIX = "RX";
    public static final String COVER = "CR";

    private ArrayList<String> types = new ArrayList<>();

    @Override
    public byte[] assemble(byte version) {
        StringBuilder sb = new StringBuilder();
        for (String type : types) {
            if (isNumeric(type)) {
                int genre = Integer.parseInt(type);
                if (genre >= 0 && genre < UNKNOWN) {
                    sb.append(String.format("(%d)", genre));
                }
            }
            else if (type.equals(REMIX) || type.equals(COVER)) {
                sb.append(String.format("(%s)", type));
            }
            else {
                sb.append(type);
            }
        }
        setText(sb.toString());
        return super.assemble(version);
    }

    @Override
    public void parseFrameData(byte[] buffer, FrameHeader header) {
        super.parseFrameData(buffer, header);
        types = parseTCON(super.getText());
    }

    @Override
    public String getFrameData() {
        return parseGenre(getText());
    }

    public void addGenre(int genre) {
        if (genre >= 0 && genre < UNKNOWN) {
            types.add(0, String.valueOf(genre));
            return;
        }
        throw new IllegalArgumentException("Invalid genre: " + genre);
    }

    public void addGenre(String genre) {
        types.add(genre);
    }

    public void addRemix() {
        types.add(0, REMIX);
    }

    public void addCover() {
        types.add(0, COVER);
    }

    public void removeAll() {
        types.clear();
    }

    public static String parseGenre(String tcon) {

        StringBuilder sb = new StringBuilder();
        for (String type : parseTCON(tcon)) {

            String separator = sb.length() != 0 ? " " : "";
            if (type.equals(REMIX)) {
                sb.append(separator).append("Remix");
            } else if (type.equals(COVER)) {
                sb.append(separator).append("Cover");
            } else {
                try {
                    int genreIndex = Integer.parseInt(type);
                    if (genreIndex >= 0 && genreIndex < UNKNOWN) {
                        sb.append(separator).append(ID3V1Tag.GENRES[genreIndex]);
                    }
                } catch (NumberFormatException e) {
                    sb.append(separator).append(type);
                }
            }
        }
        return sb.toString();
    }

    public static ArrayList<String> parseTCON(String tcon) {

        ArrayList<String> types = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean ref = false;
        for (int i = 0; i < tcon.length(); i++) {

            char ch = tcon.charAt(i);
            if (i == tcon.length() - 1 && !ref) {
                sb.append(ch);
                if (sb.length() != 0) types.add(sb.toString());
                break;
            }
            if (ch == ')' && ref) {
                String refString = sb.toString();
                if (refString.equals(REMIX) ||
                    refString.equals(COVER) ||
                    isNumeric(refString))
                {
                    types.add(sb.toString());
                }
                sb  = new StringBuilder();
                ref = false;
                continue;
            }
            if (ch == '(' && !ref) {

                char next = tcon.charAt(i + 1);
                ref = next != '(';
                i += next == '(' ? 1 : 0;

                if (next == '(') {
                    sb.append('(');
                }
                if (ref && sb.length() != 0) {
                    types.add(sb.toString());
                    sb = new StringBuilder();
                }
            } else {
                sb.append(ch);
            }
        }
        return types;
    }

    public static Builder newBuilder() {
        return new GenreFrame().new Builder();
    }

    public class Builder extends TextFrame.Builder<Builder, GenreFrame> {

        @Override
        public Builder setHeader(FrameHeader header) {
            if (!header.getIdentifier().equals(AbstractFrame.GENRE)) {
                throw new IllegalArgumentException("Type should be 'TCON'");
            }
            GenreFrame.this.header = header;
            return this;
        }

        public Builder addGenre(int genre) {
            GenreFrame.this.addGenre(genre);
            return this;
        }

        public Builder addGenre(String genre) {
            GenreFrame.this.addGenre(genre);
            return this;
        }

        public Builder addRemix() {
            GenreFrame.this.addRemix();
            return this;
        }

        public Builder addCover() {
            GenreFrame.this.addCover();
            return this;
        }

        public Builder removeAll() {
            GenreFrame.this.removeAll();
            return this;
        }
    }
}
