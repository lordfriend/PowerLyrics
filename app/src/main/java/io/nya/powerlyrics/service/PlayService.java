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
import java.io.InterruptedIOException;
import java.util.concurrent.Callable;

import io.nya.powerlyrics.LyricApplication;
import io.nya.powerlyrics.LyricsActivity;
import io.nya.powerlyrics.R;
import io.nya.powerlyrics.model.Constants;
import io.nya.powerlyrics.model.LyricNotFoundException;
import io.nya.powerlyrics.model.LyricResult;
import io.nya.powerlyrics.model.PlayStatus;
import io.nya.powerlyrics.model.SearchResult;
import io.nya.powerlyrics.model.Track;
import io.nya.powerlyrics.persist.DBHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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

    public PlayStatus mPlayStatus;

    public String mCurrentLyric = null;

    private CompositeDisposable mDisposable = new CompositeDisposable();
    private Disposable mSearchLyricDisposable = null;
    private NeteaseCloud mLyricSource;
    private LyricStorage mLyricStorage;
    private LyricApplication mApp;
    private int mNotificationId = 0xff;

    @Override
    public void onCreate() {
        super.onCreate();
        mLyricSource = new NeteaseCloud();
        mLyricStorage = new LyricStorage(DBHelper.getInstance(getApplicationContext()));
        mApp = (LyricApplication) getApplication();
        Object[] result = mLyricStorage.getLastPlayed();
        if (result != null) {
            mCurrentTrack = (Track) result[0];
            mCurrentLyric = result[1] == null ? null : (String) result[1];
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle trackBundle;
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_TRACK_CHANGED:
                    trackBundle = intent.getBundleExtra(PowerampAPI.TRACK);
//                    Long ts = intent.getLongExtra("ts", System.currentTimeMillis());
                    if (trackBundle != null) {
                        long realId = trackBundle.getLong(PowerampAPI.Track.REAL_ID);
                        if (mCurrentTrack == null) {
                            mCurrentTrack = new Track();
                        }
                        if (realId != mCurrentTrack.realId) {
                            mCurrentTrack = createTrack(trackBundle);
                            mApp.mCurrentTrackSubject.onNext(mCurrentTrack);
                            searchTrackLyric();
                        }
                    }
                    break;
                case ACTION_STATUS_CHANGED:
                    mPlayStatus = new PlayStatus();
                    mPlayStatus.status = intent.getIntExtra(PowerampAPI.STATUS, PowerampAPI.Status.TRACK_PLAYING);
                    if (mPlayStatus.status == PowerampAPI.Status.TRACK_PLAYING) {
                        mPlayStatus.isPaused = intent.getBooleanExtra(PowerampAPI.PAUSED, false);
                        trackBundle = intent.getBundleExtra(PowerampAPI.TRACK);
                        Log.d(LOG_TAG, "trackBundle is null: " + (trackBundle == null));
                        if (trackBundle != null) {
                            Track track = createTrack(trackBundle);
                            if (track.id != mCurrentTrack.id && track.realId != mCurrentTrack.realId) {
                                mCurrentTrack = track;
                                mApp.mCurrentTrackSubject.onNext(mCurrentTrack);
                            }
                        }
                        createNotification();
                    } else if (mPlayStatus.status == PowerampAPI.Status.PLAYING_ENDED) {
                        removeNotification();
                    }
                    mApp.mStatusSubject.onNext(mPlayStatus);
                    break;
                default:
                    // Do nothing
            }
        }
        return START_STICKY;
    }

    private Track createTrack(Bundle trackBundle) {
        Track track = new Track();
        track.id = trackBundle.getLong(PowerampAPI.Track.ID);
        track.realId = trackBundle.getLong(PowerampAPI.Track.REAL_ID);
        track.dur = trackBundle.getInt(PowerampAPI.Track.DURATION) * 1000;
        track.title = trackBundle.getString(PowerampAPI.Track.TITLE);
        track.album = trackBundle.getString(PowerampAPI.Track.ALBUM);
        track.artist = trackBundle.getString(PowerampAPI.Track.ARTIST);
        return track;
    }

    /**
     * Create a notification intent and add a new notification or update an exist notification
     */
    private void createNotification() {
//        Log.d(LOG_TAG, "create notification, mCurrentTrack is null: " + (mCurrentTrack == null));
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
        mCurrentLyric = null;
        mApp.mSearchStateSubject.onNext(Constants.SearchState.STATE_SEARCHING);
        // remove the former task
        if (mSearchLyricDisposable != null) {
            mDisposable.remove(mSearchLyricDisposable);
        }
        mSearchLyricDisposable = Observable
                .fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            // query the saved lyric from storage.
                            String lyric = mLyricStorage.getLyricByTrackId(mCurrentTrack.realId);
                            if (lyric == null) {
                                // no lyric found. query from lyric source
                                lyric = searchLyricFromSource();
                                // save this lyric
                                mLyricStorage.saveLyricAndTrack(mCurrentTrack, lyric);
                            }
                            mLyricStorage.saveLastPlayed(mCurrentTrack.id);
                            if (lyric == null) {
                                throw new LyricNotFoundException();
                            }
                            return lyric;
                        } catch (InterruptedIOException e) {
                            Log.d(LOG_TAG, "cancelled");
                            return "";
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
//                        Log.d(LOG_TAG, "lyric is: " + s);
                        mCurrentLyric = s;
                        mApp.mCurrentLyricSubject.onNext(s);
                        mApp.mSearchStateSubject.onNext(Constants.SearchState.STATE_COMPLETE);
                        if (mPlayStatus.status == PowerampAPI.Status.TRACK_PLAYING) {
                            createNotification();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.toString());
                        mCurrentLyric = null;
                        if (e instanceof LyricNotFoundException) {
                            mApp.mCurrentLyricSubject.onNext("");
                            mApp.mSearchStateSubject.onNext(Constants.SearchState.STATE_NOT_FOUND);
                            if (mPlayStatus.status == PowerampAPI.Status.TRACK_PLAYING) {
                                createNotification();
                            }
                        } else {
                            mApp.mSearchStateSubject.onNext(Constants.SearchState.STATE_ERROR);
                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.e(LOG_TAG, "complete");
                    }
                });
        mDisposable.add(mSearchLyricDisposable);
    }

    @Override
    public void onDestroy() {
        mLyricStorage.cleanUp();
        mDisposable.dispose();
        mApp = null;
        super.onDestroy();
    }
}
