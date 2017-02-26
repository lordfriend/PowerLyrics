package io.nya.powerlyrics.lyric;

/**
 * A class represent a single Lyric entry
 */

public class LyricEntry {
    public Long timestamp;
    public String lyric;

    public LyricEntry() {
    }
    public LyricEntry(Long timestamp, String lyric) {
        this.timestamp = timestamp;
        this.lyric = lyric;
    }
}
