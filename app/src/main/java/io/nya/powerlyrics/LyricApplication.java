package io.nya.powerlyrics;

import android.app.Application;

import com.facebook.stetho.Stetho;

import io.nya.powerlyrics.model.PlayStatus;
import io.nya.powerlyrics.model.Track;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Application instance to provide a global state container
 */

public class LyricApplication extends Application {
    public BehaviorSubject<Track> mCurrentTrackSubject;
    public BehaviorSubject<String> mCurrentLyricSubject;
    public BehaviorSubject<Integer> mSearchStateSubject;
    public BehaviorSubject<PlayStatus> mStatusSubject;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentTrackSubject = BehaviorSubject.create();
        mCurrentLyricSubject = BehaviorSubject.create();
        mSearchStateSubject = BehaviorSubject.create();
        mStatusSubject = BehaviorSubject.create();
        Stetho.initializeWithDefaults(this);
    }
}
