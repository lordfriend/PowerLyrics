package io.nya.powerlyrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.maxmpz.poweramp.player.PowerampAPI;

import io.nya.powerlyrics.service.PlayService;

public class onStatusChange extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PlayService.class);
        serviceIntent.setAction(PlayService.ACTION_STATUS_CHANGED);
        serviceIntent.putExtra(PowerampAPI.STATUS, intent.getIntExtra(PowerampAPI.STATUS, PowerampAPI.Status.TRACK_PLAYING));
        context.startService(serviceIntent);
    }
}
