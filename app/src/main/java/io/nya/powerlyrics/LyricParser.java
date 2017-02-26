package io.nya.powerlyrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricEntry;

/**
 * Parse Lyric Text String to Lyric Object
 */

public class LyricParser {

    private static final String ID_TAG_PATTERN = "\\[([a-z]+)\\:(.+)\\]";
    private static final String LYRIC_PATTERN = "\\[(\\d{2}):([0-5][0-9]).(\\d{2})\\](.*)";

    private static boolean isIdTag(String line) {
        return line.matches(ID_TAG_PATTERN);
    }

    private static boolean isLyric(String line) {
        return line.matches(LYRIC_PATTERN);
    }

    public static Lyric parse(String content) throws IOException {
        Lyric lyric = new Lyric();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        Pattern tagPattern = Pattern.compile(ID_TAG_PATTERN);
        Pattern lyricPattern = Pattern.compile(LYRIC_PATTERN);
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (isIdTag(line)) {
                Matcher matcher = tagPattern.matcher(line);
                lyric.addTag(matcher.group(1), matcher.group(2));
            } else if(isLyric(line)) {
                Matcher matcher = lyricPattern.matcher(line);
                long timestamp = (Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2))) * 1000 + Integer.parseInt(matcher.group(3)) * 10;
                String lyricStr = matcher.group(4);
                LyricEntry entry = new LyricEntry(timestamp, lyricStr);
                lyric.put(timestamp, entry);
            }
        }
        return lyric;
    }
}
