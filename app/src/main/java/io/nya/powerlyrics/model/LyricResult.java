package io.nya.powerlyrics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Lyric Result from API
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LyricResult {
//    public boolean sgc;
//    public boolean sfy;
//    public boolean qfy;
//    public static class TransUser {
//        public int id;
//        public int status;
//        public int demand;
//        public int userid;
//        public int nickname;
//        public long uptime;
//    }
//    public static class LyricUser {
//
//    }
    public static class Lrc {
        public int version;
        public String lyric;
    }
    public Lrc lrc;
//    Lrc klyric;
    public Lrc tlyric;
    public int code;

    @Override
    public String toString() {
        return "lrc: "  + (lrc == null ? "null": lrc.lyric.substring(0, 5)) + ", tlyric: {" + tlyric + (tlyric == null ? "null": "{lrc: " + tlyric.lyric.substring(0, 5) + "}");
    }
}
