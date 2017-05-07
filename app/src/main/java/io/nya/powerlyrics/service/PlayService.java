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
import io.nya.powerlyrics.LyricChooserActivity;
import io.nya.powerlyrics.LyricsActivity;
import io.nya.powerlyrics.R;
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
    public static final String ACTION_FROM_LAUNCHER = "io.nya.powerlyrics.FROM_LAUNCHER";

    public static final String ACTION_LYRIC_FOUND = "io.nya.powerlyrics.LYRIC_FOUND";
    public static final String ACTION_LYRIC_NOT_FOUND = "io.nya.powerlyrics.LYRIC_NOT_FOUND";

    /**
     * current playing track
     */
    public Track mCurrentTrack;

    public PlayStatus mPlayStatus;

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
        // retrieve the last played track in case play don't broadcast the ACTION_TRACK_CHANGED event.
        Track track = mLyricStorage.getLastPlayed();
        if (track != null) {
            mCurrentTrack = track;
        }
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
                    findAndUpdateTrack(intent);
                    createNotification();
                    break;
                case ACTION_STATUS_CHANGED:
                    mPlayStatus = new PlayStatus();
                    mPlayStatus.status = intent.getIntExtra(PowerampAPI.STATUS, PowerampAPI.Status.TRACK_PLAYING);
                    if (mPlayStatus.status == PowerampAPI.Status.TRACK_PLAYING) {
                        mPlayStatus.isPaused = intent.getBooleanExtra(PowerampAPI.PAUSED, false);
                        findAndUpdateTrack(intent);
                        createNotification();
                    } else if (mPlayStatus.status == PowerampAPI.Status.PLAYING_ENDED) {
                        removeNotification();
                    }
                    mApp.mStatusSubject.onNext(mPlayStatus);
                    break;
                case ACTION_FROM_LAUNCHER:
                    if (mCurrentTrack != null) {
                        mApp.mCurrentTrackSubject.onNext(mCurrentTrack);
                        if (mCurrentTrack.lyric_status == Track.LyricStatus.ERROR || mCurrentTrack.lyric_status == Track.LyricStatus.SEARCHING) {
                            searchTrackLyric(mCurrentTrack);
                        }
                    }
                    break;
                default:
                    // Do nothing
            }
        }
        return START_STICKY;
    }

    /**
     * try to find track from intent
     *
     * @param intent
     */
    private void findAndUpdateTrack(Intent intent) {
        Bundle trackBundle = intent.getBundleExtra(PowerampAPI.TRACK);
//                    Long ts = intent.getLongExtra("ts", System.currentTimeMillis());
        if (trackBundle != null) {
            Track track = createTrack(trackBundle);
            if ((mCurrentTrack == null) || (track.id != mCurrentTrack.id && track.realId != mCurrentTrack.realId)) {
                mCurrentTrack = track;
                mApp.mCurrentTrackSubject.onNext(track);
                searchTrackLyric(track);
            }
        }
    }

    private Track createTrack(Bundle trackBundle) {
        Track track = new Track();
        track.id = trackBundle.getLong(PowerampAPI.Track.ID);
        track.realId = trackBundle.getLong(PowerampAPI.Track.REAL_ID);
        track.dur = trackBundle.getInt(PowerampAPI.Track.DURATION) * 1000;
        Log.d(LOG_TAG, "duration = " + track.dur);
        track.title = trackBundle.getString(PowerampAPI.Track.TITLE);
        track.album = trackBundle.getString(PowerampAPI.Track.ALBUM);
        track.artist = trackBundle.getString(PowerampAPI.Track.ARTIST);
        track.pos = trackBundle.getLong(PowerampAPI.Track.POSITION);
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
        boolean lyricFound = mCurrentTrack.lyric_status == Track.LyricStatus.FOUND;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mCurrentTrack.title);

        switch (mCurrentTrack.lyric_status) {
            case Track.LyricStatus.FOUND:
                builder.setContentText(getResources().getText(R.string.lyric_found));
                break;
            case Track.LyricStatus.NOT_FOUND:
                builder.setContentText(getResources().getText(R.string.lyric_not_found));
                break;
            case Track.LyricStatus.ERROR:
                builder.setContentText(getResources().getText(R.string.search_error));
                break;
            case Track.LyricStatus.SEARCHING:
                builder.setContentText(getResources().getText(R.string.searching));
                break;
        }

        Intent resultIntent = new Intent();
        if (lyricFound) {
            resultIntent.setAction(ACTION_LYRIC_FOUND);
            resultIntent.setClass(this, LyricsActivity.class);
        } else {
            resultIntent.setAction(ACTION_LYRIC_NOT_FOUND);
            resultIntent.setClass(this, LyricChooserActivity.class);
            // put mCurrentTrack to bundle
//            resultIntent.putExtra(PowerampAPI.TRACK, mCurrentTrack);
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
        if (songs == null) {
            return sourceSongId;
        }

        for (SearchResult.Song song : songs) {
            if (song.name.equals(mCurrentTrack.title)) {
                if (song.album != null && song.album.name != null && song.album.name.equals(mCurrentTrack.album)) {
                    return song.id;
                }
                if (song.artists != null) {
                    for (SearchResult.Artist artist : song.artists) {
                        if (artist.name != null && artist.name.equals(mCurrentTrack.artist)) {
                            return song.id;
                        }
                    }
                }
                sourceSongId = song.id;
            }
        }
        return sourceSongId;
    }

    private LyricResult searchLyricFromSource() throws IOException {
        int offset = 0;
        int limit = NeteaseCloud.DEFAULT_LIMIT;
        int sourceSongId = -1;
        int total = Integer.MAX_VALUE;
        while (sourceSongId == -1 && (offset + 1) * limit < total) {
            SearchResult result = mLyricSource.searchMusic(mCurrentTrack.title, offset, limit);
            total = result.songCount;
            if (total == 0) {
                return null;
            }
            sourceSongId = matchSong(result.songs);
            offset++;
        }

        if (sourceSongId != -1) {
            return mLyricSource.getLyric(sourceSongId);
        }
        return null;
    }

    private void searchTrackLyric(final Track untouchedTrack) {
        // track is touched, skip it
        if (untouchedTrack.lyric_status == Track.LyricStatus.FOUND || untouchedTrack.lyric_status == Track.LyricStatus.NOT_FOUND) {
            return;
        }
        // remove the former task
        if (mSearchLyricDisposable != null) {
            mDisposable.remove(mSearchLyricDisposable);
        }
        mSearchLyricDisposable = Observable
                .fromCallable(new Callable<Track>() {
                    @Override
                    public Track call() throws Exception {
                        // query the saved lyric from storage.
                        Track track = mLyricStorage.getTrackById(untouchedTrack.realId);
                        Log.d(LOG_TAG, "track from db: " + track);
                        if (track == null) {
                            track = untouchedTrack.clone();
                            mLyricStorage.saveTrack(track);
                        }
                        if (track.lyric == null && track.tlyric == null) {
                            // no lyric found. query from lyric source
                            try {
                                LyricResult lyricResult = searchLyricFromSource();

                                if (lyricResult != null && lyricResult.lrc != null) {
                                    track.lyric = lyricResult.lrc.lyric;
                                }
                                if (lyricResult != null && lyricResult.tlyric != null) {
                                    track.tlyric = lyricResult.tlyric.lyric;
                                }
                                if (track.lyric == null && track.tlyric == null) {
                                    track.lyric_status = Track.LyricStatus.NOT_FOUND;
                                } else {
                                    track.lyric_status = Track.LyricStatus.FOUND;
                                }
                            } catch (InterruptedIOException e) {
                                Log.d(LOG_TAG, "cancelled");
                                track.lyric_status = Track.LyricStatus.SEARCHING;
                            } catch (IOException e) {
                                track.lyric_status = Track.LyricStatus.ERROR;
                            }
                            mLyricStorage.saveTrack(track);
                        }
                        return track;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Track>() {
                    @Override
                    public void onNext(Track track) {
                        if (mCurrentTrack.id == track.id) {
                            mCurrentTrack = track;
                            mApp.mCurrentTrackSubject.onNext(track);
                        }
                        if (mPlayStatus.status == PowerampAPI.Status.TRACK_PLAYING) {
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
