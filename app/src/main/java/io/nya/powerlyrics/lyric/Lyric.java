package io.nya.powerlyrics.lyric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * A class represent for entire lyric file including the id tags
 */

public class Lyric {

    private HashMap<String, String> idTags = new HashMap<String, String>();
    private ArrayList<LyricEntry> lyricList = new ArrayList<LyricEntry>();

//    SortedMap

    public LyricEntry get(int position) {
        return lyricList.get(position);
    }

//    public LyricEntry getMostNear(Long timestamp) {
//        return null;
//    }

    public int size() {
        return lyricList.size();
    }

    public void addTag(String key, String value) {
        idTags.put(key, value);
    }

    public String getTag(String key) {
        return idTags.get(key);
    }

    public void add(LyricEntry entry) {
        lyricList.add(entry);
    }

    public void sort() {
        Collections.sort(lyricList);
    }

}
