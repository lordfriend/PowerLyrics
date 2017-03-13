package io.nya.powerlyrics.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.nya.powerlyrics.persist.DBHelper;
import io.nya.powerlyrics.persist.TrackLyric;

/**
 * A helper to store and retrieve track lyric info from persistent layer
 */

public class LyricStorage {
    private DBHelper mDBHelper;
    public LyricStorage(DBHelper helper) {
        mDBHelper = helper;
    }

    public String getLyricByTrackId(long trackId) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] projection = {TrackLyric.Entry._ID, TrackLyric.Entry.COLUMN_NAME_LYRIC};
        String selection = TrackLyric.Entry.COLUMN_NAME_TRACK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(trackId)};
        Cursor cursor = db.query(TrackLyric.Entry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            cursor.moveToNext();
            String lyric = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LYRIC));
            cursor.close();
            return lyric;
        }
    }

    public void saveLyricByTrackId(long trackId, String lyric, String trackTitle, String album, String artist) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_ID, trackId);
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE, trackTitle);
        values.put(TrackLyric.Entry.COLUMN_NAME_LYRIC, lyric);
        values.put(TrackLyric.Entry.COLUMN_NAME_ALBUM, album);
        values.put(TrackLyric.Entry.COLUMN_NAME_ARTIST, artist);

        db.insert(TrackLyric.Entry.TABLE_NAME, null, values);
    }
}
