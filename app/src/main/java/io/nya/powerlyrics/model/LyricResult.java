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
}
