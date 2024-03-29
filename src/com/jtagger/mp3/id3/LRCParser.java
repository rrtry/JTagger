package com.jtagger.mp3.id3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LRCParser {

    public static TreeMap<Integer, String> parseSynchronisedLyrics(File fileObj) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(fileObj));
        TreeMap<Integer, String> syncedLyrics = new TreeMap<>();
        String line;

        while ((line = reader.readLine()) != null) {

            if (line.isEmpty()) {
                continue;
            }

            Pattern pattern = Pattern.compile("(\\[\\d\\d\\:\\d\\d\\.\\d+?(?=\\])\\]+)|(\\[\\d\\d\\:\\d+?(?=\\])\\]+)");
            Matcher matcher = pattern.matcher(line);

            ArrayList<String> matches = new ArrayList<>();
            int endIndex = 0;

            while (matcher.find()) {
                matches.add(matcher.group());
                endIndex = Math.max(endIndex, matcher.end());
            }

            String lyrics = line.substring(endIndex);
            for (String match : matches) {

                String timeString = match.replace("[", "")
                        .replace("]", "");

                int minutes = 0;
                int seconds = 0;
                int millis  = 0;

                boolean hasMillis = timeString.contains(".");
                String regex      = hasMillis ? "[\\.:]" : ":";
                String[] parts    = timeString.split(regex);

                minutes = Integer.parseInt(parts[0]);
                seconds = Integer.parseInt(parts[1]);

                if (hasMillis) millis = Integer.parseInt(parts[2]);
                int timestamp = minutes * 60 * 1000 + seconds * 1000 + millis;
                syncedLyrics.put(timestamp, lyrics);
            }
        }

        reader.close();
        return syncedLyrics;
    }
}
