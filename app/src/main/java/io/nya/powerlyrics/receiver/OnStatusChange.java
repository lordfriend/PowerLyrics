package io.nya.powerlyrics.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

import io.nya.powerlyrics.service.PlayService;

public class OnStatusChange extends BroadcastReceiver {

    private static final String TAG = OnStatusChange.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PlayService.class);
        serviceIntent.setAction(PlayService.ACTION_STATUS_CHANGED);
        int status = intent.getIntExtra(PowerampAPI.STATUS, -1);
        serviceIntent.putExtra(PowerampAPI.STATUS, status);
        if (status == PowerampAPI.Status.TRACK_PLAYING) {
            serviceIntent.putExtra(PowerampAPI.PAUSED, intent.getBooleanExtra(PowerampAPI.PAUSED, false));
        }
        if (status == PowerampAPI.Status.TRACK_PLAYING || status == PowerampAPI.Status.TRACK_ENDED) {
            serviceIntent.putExtra(PowerampAPI.TRACK, intent.getBundleExtra(PowerampAPI.TRACK));
        }
        if (status == PowerampAPI.Status.TRACK_ENDED) {
            serviceIntent.putExtra(PowerampAPI.FAILED, intent.getBooleanExtra(PowerampAPI.FAILED, false));
        }
        Log.e(TAG, "status: " + status + ", paused: " + intent.getBooleanExtra(PowerampAPI.PAUSED, false) + ", track is null: " + intent.getBundleExtra(PowerampAPI.TRACK));
//        if (status != -1) {
//            context.startService(serviceIntent);
//        }
    }
}
