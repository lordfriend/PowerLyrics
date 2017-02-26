package io.nya.powerlyrics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Search Result from API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    public Song[] songs;
    public int songCount;

    public static class Song {
        public int id;
        public String name;
        public Artist[] artists;
        public Album album;
        public int duration;
//        public int copyrightId;
//        public int status;
        public String[] alias;
//        public int rtype;
//        public int ftype;
//        public int mvid;
//        public int fee;
//        public String rUrl;
    }

    public static class Album {
        public int id;
        public String name;
        public Artist artist;
        public long publishTime;
        public int size;
//        public int copyrightId;
//        public int status;
        public String[] alias;
//        public long picId;
    }

    public static class Artist {
        public int id;
        public String name;
        public String picUrl;
        public String[] alias;
        public int albumSize;
//        public long picId;
        public String img1v1Url;
//        public long img1v1;
//        public String trans;
    }
}
