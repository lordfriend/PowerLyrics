package io.nya.powerlyrics.powerlyrics;

import java.util.TreeMap;

/**
 * Represent the lyric content
 */

public class Lyric {

    public TreeMap<String, String> mIdTags = new TreeMap<>();

    private TreeMap<Long, String> lyricTimeTree = new TreeMap<>();

    public void put(long timestamp, String lyricText) {
        this.lyricTimeTree.put(timestamp, lyricText);
    }

    public String get(long timestamp) {
        return lyricTimeTree.get(timestamp);
    }

    public TreeMap<Long, String> getLyricTimeTree() {
        return this.lyricTimeTree;
    }
}
