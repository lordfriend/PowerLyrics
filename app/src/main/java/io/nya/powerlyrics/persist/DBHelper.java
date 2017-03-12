package io.nya.powerlyrics.persist;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nene on 3/12/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TrackLyric.Entry.TABLE_NAME + " (" +
            TrackLyric.Entry._ID + " INTEGER PRIMARY KEY," +
            TrackLyric.Entry.COLUMN_NAME_TRACK_ID + " INT," +
            TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE + " TEXT, " +
            TrackLyric.Entry.COLUMN_NAME_LYRIC + " TEXT)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TrackLyric.Entry.TABLE_NAME;

    public static final String DB_FILE_NAME = "track_lyric";

    public static final int DB_VERSION = 1;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public static DBHelper getInstance(Context context) {
        return new DBHelper(context, DB_FILE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: this upgrade policy need to complete
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
