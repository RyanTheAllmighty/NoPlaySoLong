/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong.threads;

import java.util.TimerTask;

import me.ryandowling.noplaysolong.NoPlaySoLong;

import org.bukkit.entity.Player;

public class PlayTimeCheckerTask extends TimerTask {
    private final NoPlaySoLong plugin;

    public PlayTimeCheckerTask(NoPlaySoLong instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (this.plugin.getTimeAllowedInSeconds(player.getName()) <= 0) {
                player.kickPlayer("You have exceeded the time allowed to play! Come back in "
                        + this.plugin.secondsToDaysHoursSecondsString(this.plugin
                                .secondsUntilNextDay()) + "!");
            }
        }
    }
}
