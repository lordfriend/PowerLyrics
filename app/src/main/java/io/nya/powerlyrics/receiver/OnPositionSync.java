package io.nya.powerlyrics.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

public class OnPositionSync extends BroadcastReceiver {
    private static final String TAG = OnPositionSync.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        int pos = intent.getIntExtra(PowerampAPI.Track.POSITION, 0);
        Log.e(TAG, "pos sync=" + pos);
    }
}
