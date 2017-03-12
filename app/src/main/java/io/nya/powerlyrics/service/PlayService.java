package io.nya.powerlyrics.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

import java.io.IOException;
import java.util.concurrent.Callable;

import io.nya.powerlyrics.LyricsActivity;
import io.nya.powerlyrics.R;
import io.nya.powerlyrics.model.LyricResult;
import io.nya.powerlyrics.model.SearchResult;
import io.nya.powerlyrics.model.Track;
import io.nya.powerlyrics.persist.DBHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PlayService extends Service {

    private static final String LOG_TAG = PlayService.class.getName();

    public static final String ACTION_TRACK_CHANGED = "io.nya.powerlyrics.TRACK_CHANGED";
    public static final String ACTION_STATUS_CHANGED = "io.nya.powerlyrics.STATUS_CHANGED";

    public static final String ACTION_LYRIC_FOUND = "io.nya.powerlyrics.LYRIC_FOUND";
    public static final String ACTION_LYRIC_NOT_FOUND = "io.nya.powerlyrics.LYRIC_NOT_FOUND";

    /**
     * current playing track
     */
    public Track mCurrentTrack;

    public int mPlayStatus;

    public String mCurrentLyric = null;

    private CompositeDisposable mDisposable = new CompositeDisposable();
    private NeteaseCloud mLyricSource;
    private LyricStorage mLyricStorage;

    private int mNotificationId = 0xff;

    private static PlayService instance = null;

    public static PlayService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLyricSource = new NeteaseCloud();
        mLyricStorage = new LyricStorage(DBHelper.getInstance(getApplicationContext()));
        instance = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_TRACK_CHANGED:
                    Bundle track = intent.getBundleExtra(PowerampAPI.TRACK);
//                    Long ts = intent.getLongExtra("ts", System.currentTimeMillis());
                    if (track != null) {
                        long realId = track.getLong(PowerampAPI.Track.REAL_ID);
                        if (mCurrentTrack == null) {
                            mCurrentTrack = new Track();
                        }
                        if (realId != mCurrentTrack.realId) {
                            mCurrentTrack.id = track.getLong(PowerampAPI.Track.ID);
                            mCurrentTrack.realId = track.getLong(PowerampAPI.Track.REAL_ID);
                            mCurrentTrack.dur = track.getInt(PowerampAPI.Track.DURATION) * 1000;
                            mCurrentTrack.title = track.getString(PowerampAPI.Track.TITLE);
                            mCurrentTrack.album = track.getString(PowerampAPI.Track.ALBUM);
                            mCurrentTrack.artist = track.getString(PowerampAPI.Track.ARTIST);
                            searchTrackLyric();
                        }
                    }
                    break;
                case ACTION_STATUS_CHANGED:
                    mPlayStatus = intent.getIntExtra(PowerampAPI.STATUS, PowerampAPI.Status.TRACK_PLAYING);
                    Log.d(LOG_TAG, "Status changed: " + mPlayStatus);
                    if (mPlayStatus == PowerampAPI.Status.TRACK_PLAYING) {
                        createNotification();
                    } else if (mPlayStatus == PowerampAPI.Status.PLAYING_ENDED) {
                        removeNotification();
                    }
                    break;
                default:
                    // Do nothing
            }
        }
        return START_STICKY;
    }

    /**
     * Create a notification intent and add a new notification or update an exist notification
     */
    private void createNotification() {
        if (mCurrentTrack == null) {
            return;
        }
        boolean lyricFound = mCurrentLyric != null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mCurrentTrack.title)
                .setContentText(lyricFound ? getResources().getText(R.string.lyric_found): getResources().getText(R.string.lyric_not_found));

        Intent resultIntent = new Intent(this, LyricsActivity.class);
        if (lyricFound) {
            resultIntent.setAction(ACTION_LYRIC_FOUND);
        } else {
            resultIntent.setAction(ACTION_LYRIC_NOT_FOUND);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LyricsActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, builder.build());
    }

    /**
     * remove the notification
     */
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mNotificationId);
    }

    private int matchSong(SearchResult.Song[] songs) throws IOException {
        int sourceSongId = -1;

        for (SearchResult.Song song: songs) {
            if (song.album != null && song.album.name != null && song.album.name.equals(mCurrentTrack.album)) {
                sourceSongId = song.id;
                break;
            }
            if (song.artists != null) {
                for (SearchResult.Artist artist: song.artists) {
                    if (artist.name != null && artist.name.equals(mCurrentTrack.artist)) {
                        sourceSongId = song.id;
                        break;
                    }
                }
            }
        }
        return sourceSongId;
    }

    private String searchLyricFromSource() throws IOException {
        int offset = 0;
        int limit = NeteaseCloud.DEFAULT_LIMIT;
        int sourceSongId = -1;
        int total = Integer.MAX_VALUE;
        while(sourceSongId == -1 && (offset + 1) * limit < total) {
            SearchResult result = mLyricSource.searchMusic(mCurrentTrack.title, offset, limit);
            sourceSongId = matchSong(result.songs);
            total = result.songCount;
            offset++;
        }

        if (sourceSongId != -1) {
            LyricResult lyricResult = mLyricSource.getLyric(sourceSongId);
            if (lyricResult != null && lyricResult.lrc != null) {
                return lyricResult.lrc.lyric;
            }
        }
        return null;
    }

    private void searchTrackLyric() {
        Log.d(LOG_TAG, "current track: " + mCurrentTrack.title);
        mDisposable.add(Observable
                .fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        // query the saved lyric from storage.
                        String lyric = mLyricStorage.getLyricByTrackId(mCurrentTrack.realId);
                        if (lyric == null) {
                            // no lyric found. query from lyric source
                            lyric = searchLyricFromSource();
                            // save this lyric
                            mLyricStorage.saveLyricByTrackId(mCurrentTrack.realId, lyric, mCurrentTrack.title);
                        }
                        return lyric;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.d(LOG_TAG, "lyric is: " + s);
                        mCurrentLyric = s;
                        if (mPlayStatus == PowerampAPI.Status.TRACK_PLAYING) {
                            createNotification();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(LOG_TAG, "complete");
                    }
                }));
    }

    @Override
    public void onDestroy() {
        mDisposable.dispose();
        instance = null;
        super.onDestroy();
    }
}
