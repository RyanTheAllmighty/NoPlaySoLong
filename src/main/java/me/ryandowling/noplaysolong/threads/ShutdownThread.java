/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong.threads;

import me.ryandowling.noplaysolong.NoPlaySoLong;

public class ShutdownThread extends Thread {
    private final NoPlaySoLong plugin;

    public ShutdownThread(NoPlaySoLong plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.savePlayTime(); // Save playtime when server is shut down
    }
}
