package io.nya.powerlyrics.persist;

import android.provider.BaseColumns;

/**
 * Table definition for last_played table
 */

public class LastPlayed {
    private LastPlayed() {}

    public static final int DEFAULT_ID = 1;

    public static final class Entry implements BaseColumns {
        public static final String TABLE_NAME = "last_played";
        public static final String COLUMN_NAME_LAST_PLAYED_ID = "last_played_id";
    }
}
