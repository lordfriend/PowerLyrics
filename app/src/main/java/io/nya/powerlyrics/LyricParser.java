package io.nya.powerlyrics;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricEntry;

/**
 * Parse Lyric Text String to Lyric Object
 */

public class LyricParser {

    private static final String LOG_TAG = LyricParser.class.getName();

    private static final String ID_TAG_PATTERN = "\\[([a-z]+):(.+)\\]";
    private static final String LYRIC_PATTERN = "(\\[\\d{2}:[0-5][0-9].\\d{2}\\])(.*)";
    private static final String TIMESTAMP_PATTERN = "\\[(\\d{2}):([0-5][0-9]).(\\d{2})\\]";

    private static boolean isIdTag(String line) {
        return line.matches(ID_TAG_PATTERN);
    }

    private static boolean isLyric(String line) {
        return line.matches(LYRIC_PATTERN);
    }

    /**
     * This method is use to find the lyric entry using recursive method to support multiple tag on the same line
     * @param entryList
     * @param line
     * @return
     */
    private static ArrayList<LyricEntry> getLyricEntry(ArrayList<LyricEntry> entryList, String line) {
        Pattern lyricPattern = Pattern.compile(LYRIC_PATTERN);
        Matcher matcher = lyricPattern.matcher(line);
        if (matcher.find()) {
            LyricEntry entry = new LyricEntry();
            Pattern timestampPattern = Pattern.compile(TIMESTAMP_PATTERN);
            Matcher timestampMatcher = timestampPattern.matcher(matcher.group(1));
            timestampMatcher.find();
            entry.timestamp = (Long.parseLong(timestampMatcher.group(1)) * 60L +  Long.parseLong(timestampMatcher.group(2))) * 1000L + Long.parseLong(timestampMatcher.group(3)) * 10L;
            entryList.add(entry);
            return getLyricEntry(entryList, matcher.group(2));
        }

        for(LyricEntry entry: entryList) {
            entry.lyric = line;
        }
        return entryList;
    }

    public static Lyric parse(String content) throws IOException {
        Lyric lyric = new Lyric();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        Pattern tagPattern = Pattern.compile(ID_TAG_PATTERN);
//        Pattern lyricPattern = Pattern.compile(LYRIC_PATTERN);
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (isIdTag(line)) {
                Log.d(LOG_TAG, "line: " + line);
                Matcher matcher = tagPattern.matcher(line);
                matcher.find();
                lyric.addTag(matcher.group(1), matcher.group(2));
            } else if(isLyric(line)) {
                ArrayList<LyricEntry> entryList = getLyricEntry(new ArrayList<LyricEntry>(), line);
                for (LyricEntry entry: entryList) {
                    lyric.add(entry);
                }
            }
        }
        lyric.sort();
        return lyric;
    }
}
