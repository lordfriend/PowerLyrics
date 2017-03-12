package io.nya.powerlyrics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.maxmpz.poweramp.player.RemoteTrackTime;

import io.nya.powerlyrics.service.PlayService;


public class LyricsActivity extends Activity implements RemoteTrackTime.TrackTimeListener {

    private final static String LOG_TAG = LyricsActivity.class.getName();

    RemoteTrackTime mRemoteTrackTime;

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (PlayService.ACTION_LYRIC_FOUND.equals(action)) {
            // TODO: we need do something open from notification
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        handleIntent(getIntent());

        mRemoteTrackTime = new RemoteTrackTime(this);
        PlayService service = PlayService.getInstance();
        if (service != null) {
            Log.d(LOG_TAG, "activity: " + service.mCurrentTrack.title);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
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
