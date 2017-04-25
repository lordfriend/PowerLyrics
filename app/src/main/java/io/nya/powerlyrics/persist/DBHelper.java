package io.nya.powerlyrics.persist;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nene on 3/12/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    /**
     * create track_lyric table
     */
    private static final String SQL_CREATE_TRACK_LYRIC = "CREATE TABLE IF NOT EXISTS " + TrackLyric.Entry.TABLE_NAME + " (" +
            TrackLyric.Entry._ID + " INTEGER PRIMARY KEY," +
            TrackLyric.Entry.COLUMN_NAME_TRACK_ID + " INT," +
            TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID + " INT, " +
            TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE + " TEXT, " +
            TrackLyric.Entry.COLUMN_NAME_LYRIC + " TEXT, " +
            TrackLyric.Entry.COLUMN_NAME_TLYRIC + " TEXT, " +
            TrackLyric.Entry.COLUMN_NAME_LYRIC_STATUS + " INT, " +
            TrackLyric.Entry.COLUMN_NAME_ALBUM + " TEXT, " +
            TrackLyric.Entry.COLUMN_NAME_ARTIST + " TEXT, " +
            TrackLyric.Entry.COLUMN_NAME_DURATION + " INT, " +
            TrackLyric.Entry.COLUMN_NAME_LAST_PLAYED_TIME + " INT, " +
            TrackLyric.Entry.COLUMN_NAME_POSITION + " INT)";

    /**
     * drop track_lyric table
     */
    private static final String SQL_DELETE_TRACK_LYRIC = "DROP TABLE IF EXISTS " + TrackLyric.Entry.TABLE_NAME;


    private static final String DB_FILE_NAME = "track_lyric";

    private static final int DB_VERSION = 3;

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public static DBHelper getInstance(Context context) {
        return new DBHelper(context, DB_FILE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRACK_LYRIC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: this upgrade policy need to complete
        db.execSQL(SQL_DELETE_TRACK_LYRIC);
        onCreate(db);
    }
}
