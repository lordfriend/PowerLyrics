package io.nya.powerlyrics.lyric;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse Lyric Text String to Lyric Object
 */

public class LyricParser {

    private static final String LOG_TAG = LyricParser.class.getName();

    private static final String ID_TAG_PATTERN = "\\[([a-z]+):(.+)\\]";
    private static final String LYRIC_PATTERN = "(\\[\\d{2}:[0-5][0-9](?:.\\d{2,3})?\\])(.*)";
    private static final String TIMESTAMP_PATTERN = "\\[(\\d{2}):([0-5][0-9]).(\\d{2,3})\\]";
    private static final String SIMPLE_TIMESTAMP_PATTERN = "\\[(\\d{2}):([0-5][0-9])\\]";

    private static boolean isIdTag(String line) {
        return line.matches(ID_TAG_PATTERN);
    }

    private static boolean isLyric(String line) {
        return line.matches(LYRIC_PATTERN);
    }

    private static long parseTimestamp(String timestampStr) {
        if (timestampStr.matches(TIMESTAMP_PATTERN)) {
            Pattern timestampPattern = Pattern.compile(TIMESTAMP_PATTERN);
            Matcher timestampMatcher = timestampPattern.matcher(timestampStr);
            timestampMatcher.find();
            long timestampValue = (Long.parseLong(timestampMatcher.group(1)) * 60L +  Long.parseLong(timestampMatcher.group(2))) * 1000L;
            if(timestampMatcher.group(3).length() == 3) {
                timestampValue += Long.parseLong(timestampMatcher.group(3));
            } else {
                timestampValue += Long.parseLong(timestampMatcher.group(3)) * 10L;
            }
            return timestampValue;
        } else if (timestampStr.matches(SIMPLE_TIMESTAMP_PATTERN)) {
            Pattern simpleTimestampPattern = Pattern.compile(SIMPLE_TIMESTAMP_PATTERN);
            Matcher timestampMatcher = simpleTimestampPattern.matcher(timestampStr);
            timestampMatcher.find();
            return (Long.parseLong(timestampMatcher.group(1)) * 60L +  Long.parseLong(timestampMatcher.group(2))) * 1000L;
        } else {
            return -1;
        }
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
            entry.timestamp = parseTimestamp(matcher.group(1));
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
        lyric.calculateEntryDuration();
        return lyric;
    }

    public static Lyric parse(String content, String tContent) throws IOException {
        Lyric lyric = new Lyric();
        Lyric rawLyric = parse(content);
        Lyric tLyric = parse(tContent);
        HashMap<Long, LyricEntry> entryHashMap = new HashMap<>();
        for (LyricEntry rawEntry: rawLyric) {
            entryHashMap.put(rawEntry.timestamp, rawEntry);
        }
        for (LyricEntry tEntry: tLyric) {
            if (entryHashMap.containsKey(tEntry.timestamp)) {
                entryHashMap.get(tEntry.timestamp).tLyric = tEntry.lyric;
            } else {
                tEntry.tLyric = tEntry.lyric;
                tEntry.lyric = null;
                entryHashMap.put(tEntry.timestamp, tEntry);
            }
        }
        for (LyricEntry entry: entryHashMap.values()) {
            lyric.add(entry);
        }
        lyric.sort();
        lyric.calculateEntryDuration();

        for (Map.Entry<String, String> tag: rawLyric.getIdTags().entrySet()) {
            lyric.addTag(tag.getKey(), tag.getValue());
        }
        return lyric;
    }

}
