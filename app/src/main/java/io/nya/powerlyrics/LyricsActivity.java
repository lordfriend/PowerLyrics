package io.nya.powerlyrics;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.maxmpz.poweramp.player.RemoteTrackTime;

import io.nya.powerlyrics.service.PlayService;


public class LyricsActivity extends Activity implements RemoteTrackTime.TrackTimeListener {

    private final static String LOG_TAG = LyricsActivity.class.getName();

    RemoteTrackTime mRemoteTrackTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lyrics);

        mRemoteTrackTime = new RemoteTrackTime(this);
        PlayService service = PlayService.getInstance();
        if (service != null) {
            Log.d(LOG_TAG, "activity: " + service.mCurrentTrack.title);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onTrackDurationChanged(int duration) {

    }

    @Override
    public void onTrackPositionChanged(int position) {

    }
}
