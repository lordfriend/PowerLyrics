package io.nya.powerlyrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.maxmpz.poweramp.player.PowerampAPI;

import io.nya.powerlyrics.service.PlayService;

public class OnTrackChange extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PlayService.class);
        serviceIntent.setAction(PlayService.ACTION_TRACK_CHANGED);
        serviceIntent.putExtra(PowerampAPI.TRACK, intent.getBundleExtra("track"));
        serviceIntent.putExtra(PowerampAPI.TIMESTAMP, intent.getLongExtra(PowerampAPI.TIMESTAMP, System.currentTimeMillis()));
        context.startService(serviceIntent);
    }
}
