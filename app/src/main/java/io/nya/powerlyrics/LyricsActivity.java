package io.nya.powerlyrics;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxmpz.poweramp.player.PowerampAPI;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricParser;
import io.nya.powerlyrics.model.PlayStatus;
import io.nya.powerlyrics.model.Track;
import io.nya.powerlyrics.service.PlayService;
import io.nya.powerlyrics.view.LyricView;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;


public class LyricsActivity extends Activity {

    private final static String TAG = LyricsActivity.class.getName();

    /**
     * the interval to update play position
     */
    private static final long DEFAULT_INTERVAL = 400;
    /**
     * whenever a play status changes to pause or stop. invalid current position
     */
    private static final long INVALID_POSITION = -1;

    private LyricApplication mApp;
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private Track mCurrentTrack;
    private long mCurrentTrackPos;
    private long mLastSyncTimestamp;
    private PlayStatus mPlayStatus;

    TextView mStateIndicator;
    ViewGroup mMainContainer;
    LyricView mLyricView;
    TextView mTrackTitleView;

    private void updateTrackPosition(int position) {
        mCurrentTrackPos = position * 1000;
        Log.d(TAG, "mTrackPosSyncReceiver sync=" + mCurrentTrackPos);
        mLastSyncTimestamp = System.currentTimeMillis();
    }

    private BroadcastReceiver mTrackPosSyncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra(PowerampAPI.Track.POSITION, 0);
            updateTrackPosition(pos);
        }

    };

    private void startTrackPosition() {
        // immediately sync the position of track.
        startService(PowerampAPI.newAPIIntent().putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.POS_SYNC));

        mDisposable.add(Observable.interval(DEFAULT_INTERVAL, TimeUnit.MILLISECONDS)
                .mergeWith(mApp.mStatusSubject.map(new Function<PlayStatus, Long>() {
                    @Override
                    public Long apply(@NonNull PlayStatus status) throws Exception {
                        mPlayStatus = status;
                        return 0L;
                    }
                }))
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(@NonNull Long aLong) throws Exception {
                        return mPlayStatus != null && mPlayStatus.status == PowerampAPI.Status.TRACK_PLAYING && !mPlayStatus.isPaused;
                    }
                })
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        return System.currentTimeMillis() - mLastSyncTimestamp + mCurrentTrackPos;
                    }
                })
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long pos) {
                        mLyricView.updateCurrentTime(pos);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "track error: " + e.toString());
                        mCurrentTrackPos = INVALID_POSITION;
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "Track complete");
                        mCurrentTrackPos = INVALID_POSITION;
                    }
                }));
    }

    private void setLyric(String lyricStr) {
        try {
            Lyric lyric = LyricParser.parse(lyricStr);
            mLyricView.setLyric(lyric);
            mLyricView.setDuration(mCurrentTrack.dur);
        } catch (IOException e) {
            mMainContainer.setVisibility(View.INVISIBLE);
            mStateIndicator.setVisibility(View.VISIBLE);
            mStateIndicator.setText(getResources().getString(R.string.lrc_parse_error));
            e.printStackTrace();
        }
    }

    private void trackPlayerStatus() {
        registerReceiver(mTrackPosSyncReceiver, new IntentFilter(PowerampAPI.ACTION_TRACK_POS_SYNC));
//        registerReceiver(mStatsChangeReceiver, new IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED));
        startTrackPosition();
    }

    private void stopTrackPlayerStatus() {
        unregisterReceiver(mTrackPosSyncReceiver);
//        unregisterReceiver(mStatsChangeReceiver);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_MAIN.equals(action)) {
            Intent serviceIntent = new Intent(PlayService.ACTION_FROM_LAUNCHER);
            serviceIntent.setClass(this, PlayService.class);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        mApp = (LyricApplication) getApplication();

        mStateIndicator = (TextView) findViewById(R.id.state_indicator);
        mMainContainer = (ViewGroup) findViewById(R.id.main_content_container);
        mLyricView = (LyricView) findViewById(R.id.lyric_view);
        mTrackTitleView = (TextView) findViewById(R.id.track_title);
        handleIntent(getIntent());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        mDisposable.add(mApp.mCurrentTrackSubject.subscribeWith(new DisposableObserver<Track>() {
            @Override
            public void onNext(Track track) {
                mCurrentTrack = track;
                setTitle(track.title);
                mTrackTitleView.setText(track.title);
                switch (track.lyric_status) {
                    case Track.LyricStatus.SEARCHING:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.searching));
                        break;
                    case Track.LyricStatus.FOUND:
                        mMainContainer.setVisibility(View.VISIBLE);
                        mStateIndicator.setVisibility(View.INVISIBLE);
                        setLyric(track.lyric);
                        break;
                    case Track.LyricStatus.NOT_FOUND:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.lyric_not_found));
                        break;
                    case Track.LyricStatus.ERROR:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.search_error));
                        break;
                    default:
                        // no default
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "current track subject complete");
            }
        }));

        trackPlayerStatus();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDisposable.dispose();
        stopTrackPlayerStatus();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mApp = null;
        super.onDestroy();
    }

}
