package io.nya.powerlyrics.persist;

import android.provider.BaseColumns;

/**
 * Storage for track lyric relation
 */

public final class TrackLyric {
    private TrackLyric() {}

    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "track_lyric";
        public static final String COLUMN_NAME_TRACK_ID = "track_id";
        public static final String COLUMN_NAME_TRACK_REAL_ID = "track_real_id";
        public static final String COLUMN_NAME_TRACK_TITLE = "track_title";
        public static final String COLUMN_NAME_LYRIC = "lyric";
        public static final String COLUMN_NAME_ALBUM = "album";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_DURATION = "duration";
    }
}
