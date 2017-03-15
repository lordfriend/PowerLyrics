package io.nya.powerlyrics.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

import io.nya.powerlyrics.service.PlayService;

public class OnTrackChange extends BroadcastReceiver {

    private static final String TAG = OnTrackChange.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PlayService.class);
        serviceIntent.setAction(PlayService.ACTION_TRACK_CHANGED);
        serviceIntent.putExtra(PowerampAPI.TRACK, intent.getBundleExtra("track"));
        serviceIntent.putExtra(PowerampAPI.TIMESTAMP, intent.getLongExtra(PowerampAPI.TIMESTAMP, System.currentTimeMillis()));
        int pos = intent.getIntExtra(PowerampAPI.Track.POSITION, 0);
        Log.e(TAG, "track changed, track.title=" + intent.getBundleExtra("track").getString(PowerampAPI.Track.TITLE) + " track.pos=" + pos);
        context.startService(serviceIntent);
    }
}
