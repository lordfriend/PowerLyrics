package io.nya.powerlyrics.model;

import com.maxmpz.poweramp.player.PowerampAPI;

/**
 * Composite object for play status and isPaused
 */

public class PlayStatus {
    public boolean isPaused;
    public int status;

    @Override
    public String toString() {
        return "status=" + status + " and " + (isPaused ? "paused" : "not paused");
    }
}
