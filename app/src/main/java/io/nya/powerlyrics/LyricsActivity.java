package io.nya.powerlyrics;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxmpz.poweramp.player.PowerampAPI;

import java.io.IOException;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricParser;
import io.nya.powerlyrics.model.PlayStatus;
import io.nya.powerlyrics.model.Track;
import io.nya.powerlyrics.service.PlayService;
import io.nya.powerlyrics.view.LyricView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;


public class LyricsActivity extends Activity {

    private final static String TAG = LyricsActivity.class.getName();

    /**
     * the interval to update play position
     */
    private static final long DEFAULT_INTERVAL = 400;

    private LyricApplication mApp;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    private Track mCurrentTrack;
    private long mCurrentTrackPos;
    private long mLastSyncTimestamp;

    TextView mStateIndicator;
    ViewGroup mMainContainer;
    LyricView mLyricView;
    TextView mTrackTitleView;

    private Handler mHandler = new Handler();

    private Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            long pos = System.currentTimeMillis() - mLastSyncTimestamp + mCurrentTrackPos;
            mLyricView.updateCurrentTime(pos);
            mHandler.postDelayed(this, DEFAULT_INTERVAL);
        }
    };

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
        if (mCurrentTrack == null || mCurrentTrack.lyric_status != Track.LyricStatus.FOUND) {
            return;
        }
        // immediately sync the position of track.
        startService(PowerampAPI.newAPIIntent().putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.POS_SYNC));
        mHandler.postDelayed(mTickRunnable, 0);
    }

    private void stopTrackPosition() {
        mHandler.removeCallbacks(mTickRunnable);
    }

    private void setLyric(Track track) {
        try {
            Lyric lyric;
            if (track.tlyric == null) {
                lyric = LyricParser.parse(track.lyric);
            } else {
                lyric = LyricParser.parse(track.lyric, track.tlyric);
            }

            mLyricView.setLyric(lyric);
            mLyricView.setDuration(mCurrentTrack.dur);
        } catch (IOException e) {
            mMainContainer.setVisibility(View.INVISIBLE);
            mStateIndicator.setVisibility(View.VISIBLE);
            mStateIndicator.setText(getResources().getString(R.string.lrc_parse_error));
            e.printStackTrace();
        }
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
        Log.w(TAG, "onCreate app id: " + (Math.random() * 1000));
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
        Log.w(TAG, "onResume");
        registerReceiver(mTrackPosSyncReceiver, new IntentFilter(PowerampAPI.ACTION_TRACK_POS_SYNC));
        mDisposable.add(mApp.mCurrentTrackSubject.subscribeWith(new DisposableObserver<Track>() {
            @Override
            public void onNext(Track track) {
                mCurrentTrack = track;
                Log.d(TAG, "track is " + track.toString());
                setTitle(track.title);
                mTrackTitleView.setText(track.title);

                // set current position
                mCurrentTrackPos = track.pos;
                mLastSyncTimestamp = System.currentTimeMillis();

                switch (track.lyric_status) {
                    case Track.LyricStatus.SEARCHING:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.searching));
                        stopTrackPosition();
                        break;
                    case Track.LyricStatus.FOUND:
                        mMainContainer.setVisibility(View.VISIBLE);
                        mStateIndicator.setVisibility(View.INVISIBLE);
                        setLyric(track);
                        startTrackPosition();
                        break;
                    case Track.LyricStatus.NOT_FOUND:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.lyric_not_found));
                        stopTrackPosition();
                        break;
                    case Track.LyricStatus.ERROR:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.search_error));
                        stopTrackPosition();
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
        mDisposable.add(mApp.mStatusSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<PlayStatus>() {
                    @Override
                    public void onNext(PlayStatus playStatus) {
                        if (playStatus.status == PowerampAPI.Status.TRACK_PLAYING && !playStatus.isPaused) {
                            startTrackPosition();
                        } else {
                            stopTrackPosition();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "track error: " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "Track complete");
                    }
                }));

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.w(TAG, "on pause");
        mDisposable.dispose();
        stopTrackPosition();
        unregisterReceiver(mTrackPosSyncReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy");
        mApp = null;
        super.onDestroy();
    }

}
