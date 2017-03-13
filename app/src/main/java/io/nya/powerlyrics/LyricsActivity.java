package io.nya.powerlyrics;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxmpz.poweramp.player.RemoteTrackTime;

import java.io.IOException;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricParser;
import io.nya.powerlyrics.model.Constants;
import io.nya.powerlyrics.model.Track;
import io.nya.powerlyrics.view.LyricView;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;


public class LyricsActivity extends Activity implements RemoteTrackTime.TrackTimeListener {

    private final static String LOG_TAG = LyricsActivity.class.getName();

    RemoteTrackTime mRemoteTrackTime;

    private LyricApplication mApp;
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private Track mCurrentTrack;

    TextView mStateIndicator;
    ViewGroup mMainContainer;
    LyricView mLyricView;
    TextView mTrackTitleView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        mApp = (LyricApplication) getApplication();

        mStateIndicator = (TextView) findViewById(R.id.state_indicator);
        mMainContainer = (ViewGroup) findViewById(R.id.main_content_container);
        mLyricView = (LyricView) findViewById(R.id.lyric_view);
        mTrackTitleView = (TextView) findViewById(R.id.track_title);

        mRemoteTrackTime = new RemoteTrackTime(this);
        mRemoteTrackTime.setTrackTimeListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        mDisposable.add(mApp.mCurrentTrackSubject.subscribeWith(new DisposableObserver<Track>() {
            @Override
            public void onNext(Track track) {
                mCurrentTrack = track;
                setTitle(track.title);
                mTrackTitleView.setText(track.title);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, e.toString());
            }

            @Override
            public void onComplete() {
                Log.d(LOG_TAG, "current track subject complete");
            }
        }));
        mDisposable.add(mApp.mCurrentLyricSubject.subscribeWith(new DisposableObserver<String>() {
            @Override
            public void onNext(String lyric) {
                setLyric(lyric);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, e.toString());
            }

            @Override
            public void onComplete() {
                Log.d(LOG_TAG, "current lyric subject complete");
            }
        }));
        mDisposable.add(mApp.mSearchStateSubject.subscribeWith(new DisposableObserver<Integer>() {
            @Override
            public void onNext(Integer state) {
                switch(state) {
                    case Constants.SearchState.STATE_SEARCHING:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.searching));
                        break;
                    case Constants.SearchState.STATE_COMPLETE:
                        mMainContainer.setVisibility(View.VISIBLE);
                        mStateIndicator.setVisibility(View.INVISIBLE);
                        break;
                    case Constants.SearchState.STATE_NOT_FOUND:
                        mMainContainer.setVisibility(View.INVISIBLE);
                        mStateIndicator.setVisibility(View.VISIBLE);
                        mStateIndicator.setText(getResources().getString(R.string.lyric_not_found));
                        break;
                    case Constants.SearchState.STATE_ERROR:
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
                Log.e(LOG_TAG, e.toString());
            }

            @Override
            public void onComplete() {
                Log.d(LOG_TAG, "search state subject complete");
            }
        }));

        mRemoteTrackTime.registerAndLoadStatus();
        mRemoteTrackTime.startSongProgress();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDisposable.dispose();
        mRemoteTrackTime.unregister();
        mRemoteTrackTime.stopSongProgress();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mApp = null;
        mRemoteTrackTime.setTrackTimeListener(null);
        mRemoteTrackTime.unregister();

        mRemoteTrackTime = null;
        super.onDestroy();
    }

    @Override
    public void onTrackDurationChanged(int duration) {
        // do nothing
    }

    @Override
    public void onTrackPositionChanged(int position) {
        Log.d(LOG_TAG, "play position: " + (position * 1000));
        mLyricView.updateCurrentTime(position * 1000);
    }
}
