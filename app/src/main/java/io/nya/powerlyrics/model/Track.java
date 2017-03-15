package io.nya.powerlyrics.model;

/**
 * Created by nene on 3/12/17.
 */

public class Track {
    public long id;
    public long realId;
    public String title;
    public String album;
    public String artist;
    public long dur;
    public long pos;
    public long last_played_time;
    public String lyric;
    public int lyric_status = LyricStatus.SEARCHING;

    public static final class LyricStatus {
        public static final int SEARCHING = 1;
        public static final int FOUND = 2;
        public static final int NOT_FOUND = 3;
        public static final int ERROR = 4;
    }

    @Override
    public Track clone() throws CloneNotSupportedException {
        Track newObj = new Track();
        newObj.id = id;
        newObj.realId = realId;
        newObj.title = title;
        newObj.album = album;
        newObj.artist = artist;
        newObj.dur = dur;
        newObj.pos = pos;
        newObj.last_played_time = last_played_time;
        newObj.lyric = lyric;
        newObj.lyric_status = lyric_status;
        return newObj;
    }

    @Override
    public String toString() {
        return "#" + id + "(" + realId + ") \"" + title + "\" duration=" + dur + " album=\"" + album + "\" artist=\"" + artist + "\"";
    }
}
