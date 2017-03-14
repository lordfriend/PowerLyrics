package io.nya.powerlyrics.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

import io.nya.powerlyrics.model.Track;
import io.nya.powerlyrics.persist.DBHelper;
import io.nya.powerlyrics.persist.LastPlayed;
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

    public void saveLyricAndTrack(Track track, String lyric) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_ID, track.id);
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID, track.realId);
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE, track.title);
        values.put(TrackLyric.Entry.COLUMN_NAME_LYRIC, lyric);
        values.put(TrackLyric.Entry.COLUMN_NAME_ALBUM, track.album);
        values.put(TrackLyric.Entry.COLUMN_NAME_ARTIST, track.artist);
        values.put(TrackLyric.Entry.COLUMN_NAME_DURATION, track.dur);

        db.insert(TrackLyric.Entry.TABLE_NAME, null, values);
    }

    public Object[] getLastPlayed() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor lastPlayedCursor = db.query(LastPlayed.Entry.TABLE_NAME,
                new String[]{LastPlayed.Entry.COLUMN_NAME_LAST_PLAYED_ID},
                LastPlayed.Entry._ID + " = ?",
                new String[]{String.valueOf(LastPlayed.DEFAULT_ID)}, null, null, null);
        if (lastPlayedCursor.getCount() == 0) {
            lastPlayedCursor.close();
            return null;
        }
        lastPlayedCursor.moveToFirst();
        int trackId = lastPlayedCursor.getInt(lastPlayedCursor.getColumnIndex(LastPlayed.Entry.COLUMN_NAME_LAST_PLAYED_ID));

        String [] projection = {
                TrackLyric.Entry.COLUMN_NAME_TRACK_ID,
                TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID,
                TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE,
                TrackLyric.Entry.COLUMN_NAME_LYRIC,
                TrackLyric.Entry.COLUMN_NAME_ALBUM,
                TrackLyric.Entry.COLUMN_NAME_ARTIST,
                TrackLyric.Entry.COLUMN_NAME_DURATION
        };
        String selection = TrackLyric.Entry.COLUMN_NAME_TRACK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(trackId)};
        Cursor trackCursor = db.query(TrackLyric.Entry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (trackCursor.getCount() == 0) {
            trackCursor.close();
            return null;
        }
        trackCursor.moveToFirst();
        Track track = new Track();
        track.id = trackCursor.getInt(trackCursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_ID));
        track.realId = trackCursor.getInt(trackCursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID));
        track.title = trackCursor.getString(trackCursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE));
        track.album = trackCursor.getString(trackCursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_ALBUM));
        track.artist = trackCursor.getString(trackCursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_ARTIST));
        track.dur = trackCursor.getInt(trackCursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_DURATION));
        String lyric = trackCursor.getString(trackCursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LYRIC));
        trackCursor.close();
        return new Object[]{track, lyric};
    }

    public void saveLastPlayed(long trackId) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LastPlayed.Entry.COLUMN_NAME_LAST_PLAYED_ID, trackId);
        String selection = LastPlayed.Entry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(LastPlayed.DEFAULT_ID)};
        db.update(LastPlayed.Entry.TABLE_NAME, values, selection, selectionArgs);
    }

    public void cleanUp() {
        this.mDBHelper.close();;
    }
}
