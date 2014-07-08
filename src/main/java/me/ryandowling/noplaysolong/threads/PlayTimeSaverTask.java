/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong.threads;

import java.util.TimerTask;

import me.ryandowling.noplaysolong.NoPlaySoLong;

public class PlayTimeSaverTask extends TimerTask {
    private final NoPlaySoLong plugin;

    public PlayTimeSaverTask(NoPlaySoLong instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        this.plugin.savePlayTime(); // Save playtime every 10 minutes
    }
}
