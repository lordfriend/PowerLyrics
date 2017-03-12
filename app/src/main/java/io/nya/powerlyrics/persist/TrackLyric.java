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
        public static final String COLUMN_NAME_TRACK_TITLE = "track_title";
        public static final String COLUMN_NAME_LYRIC = "lyric";
    }
}
