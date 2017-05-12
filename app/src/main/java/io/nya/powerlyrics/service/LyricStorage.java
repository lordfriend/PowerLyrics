package io.nya.powerlyrics.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxmpz.poweramp.player.PowerampAPI;

import io.nya.powerlyrics.model.Track;
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

    public Track getTrackById(long trackRealId) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String [] projection = {
                TrackLyric.Entry.COLUMN_NAME_TRACK_ID,
                TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID,
                TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE,
                TrackLyric.Entry.COLUMN_NAME_LYRIC,
                TrackLyric.Entry.COLUMN_NAME_TLYRIC,
                TrackLyric.Entry.COLUMN_NAME_LYRIC_STATUS,
                TrackLyric.Entry.COLUMN_NAME_ALBUM,
                TrackLyric.Entry.COLUMN_NAME_ARTIST,
                TrackLyric.Entry.COLUMN_NAME_DURATION,
                TrackLyric.Entry.COLUMN_NAME_LAST_PLAYED_TIME,
                TrackLyric.Entry.COLUMN_NAME_POSITION
        };
        String selection = TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(trackRealId)};
        Cursor cursor = db.query(TrackLyric.Entry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            cursor.moveToNext();
            Track track = new Track();
            track.id = cursor.getInt(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_ID));
            track.realId = cursor.getInt(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID));
            track.title = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE));
            track.album = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_ALBUM));
            track.artist = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_ARTIST));
            track.dur = cursor.getLong(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_DURATION));
            track.lyric = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LYRIC));
            track.tlyric = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TLYRIC));
            track.lyric_status = cursor.getInt(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LYRIC_STATUS));
            track.last_played_time = cursor.getLong(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LAST_PLAYED_TIME));
            track.pos = cursor.getLong(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_POSITION));
            cursor.close();
            return track;
        }
    }

    public void saveTrack(Track track) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_ID, track.id);
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID, track.realId);
        values.put(TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE, track.title);
        values.put(TrackLyric.Entry.COLUMN_NAME_LYRIC, track.lyric);
        values.put(TrackLyric.Entry.COLUMN_NAME_TLYRIC, track.tlyric);
        values.put(TrackLyric.Entry.COLUMN_NAME_LYRIC_STATUS, track.lyric_status);
        values.put(TrackLyric.Entry.COLUMN_NAME_ALBUM, track.album);
        values.put(TrackLyric.Entry.COLUMN_NAME_ARTIST, track.artist);
        values.put(TrackLyric.Entry.COLUMN_NAME_DURATION, track.dur);
        values.put(TrackLyric.Entry.COLUMN_NAME_POSITION,  track.pos);
        values.put(TrackLyric.Entry.COLUMN_NAME_LAST_PLAYED_TIME, System.currentTimeMillis());

        long resultId = db.insertWithOnConflict(TrackLyric.Entry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (resultId == -1) {
            db.update(TrackLyric.Entry.TABLE_NAME, values, TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID + " = ?", new String[]{String.valueOf(track.realId)});
        }
    }

    public Track getLastPlayed() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String [] projection = {
                TrackLyric.Entry.COLUMN_NAME_TRACK_ID,
                TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID,
                TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE,
                TrackLyric.Entry.COLUMN_NAME_LYRIC,
                TrackLyric.Entry.COLUMN_NAME_TLYRIC,
                TrackLyric.Entry.COLUMN_NAME_LYRIC_STATUS,
                TrackLyric.Entry.COLUMN_NAME_ALBUM,
                TrackLyric.Entry.COLUMN_NAME_ARTIST,
                TrackLyric.Entry.COLUMN_NAME_DURATION,
                TrackLyric.Entry.COLUMN_NAME_LAST_PLAYED_TIME,
                TrackLyric.Entry.COLUMN_NAME_POSITION
        };

        String orderBy = TrackLyric.Entry.COLUMN_NAME_LAST_PLAYED_TIME + " DESC";

        Cursor cursor = db.query(TrackLyric.Entry.TABLE_NAME, projection, null, null, null, null, orderBy, String.valueOf(1));
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        Track track = new Track();
        track.id = cursor.getInt(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_ID));
        track.realId = cursor.getInt(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_REAL_ID));
        track.title = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TRACK_TITLE));
        track.album = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_ALBUM));
        track.artist = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_ARTIST));
        track.dur = cursor.getLong(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_DURATION));
        track.lyric = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LYRIC));
        track.tlyric = cursor.getString(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_TLYRIC));
        track.lyric_status = cursor.getInt(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LYRIC_STATUS));
        track.last_played_time = cursor.getLong(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_LAST_PLAYED_TIME));
        track.pos = cursor.getLong(cursor.getColumnIndex(TrackLyric.Entry.COLUMN_NAME_POSITION));
        cursor.close();
        return track;
    }

    public void cleanUp() {
        this.mDBHelper.close();;
    }
}
