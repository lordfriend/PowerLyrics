package io.nya.powerlyrics.lyric;

/**
 * A class represent a single Lyric entry
 */

public class LyricEntry implements Comparable {
    public Long timestamp;
    public String lyric;

    public LyricEntry() {
    }
    public LyricEntry(Long timestamp, String lyric) {
        this.timestamp = timestamp;
        this.lyric = lyric;
    }

    @Override
    public int compareTo(Object another) {
        return (int) (timestamp - ((LyricEntry) another).timestamp);
    }
}
