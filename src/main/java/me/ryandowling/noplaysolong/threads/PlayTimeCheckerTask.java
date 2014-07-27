/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong.threads;

import java.io.File;
import java.util.TimerTask;

import me.ryandowling.noplaysolong.NoPlaySoLong;
import me.ryandowling.noplaysolong.utils.FileUtils;
import me.ryandowling.noplaysolong.utils.Timestamper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayTimeCheckerTask extends TimerTask {
    private final NoPlaySoLong plugin;

    public PlayTimeCheckerTask(NoPlaySoLong instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            int timeLeft = this.plugin.getTimeAllowedInSeconds(player.getName());
            if (timeLeft <= 0) {
                FileUtils.appendStringToFile(
                        new File(this.plugin.getDataFolder(), "playtime.log"),
                        String.format("[%s] %s was kicked for exceeding play time",
                                Timestamper.now(), player.getName()));
                player.kickPlayer("You have exceeded the time allowed to play! Come back in "
                        + this.plugin.secondsToDaysHoursSecondsString(this.plugin
                                .secondsUntilNextDay()) + "!");
            } else if (timeLeft <= 10 && !this.plugin.hasPlayerSeenMessage(player.getName(), 10)) {
                player.sendMessage(ChatColor.RED
                        + "WARNING!"
                        + ChatColor.RESET
                        + " You have less than 10 seconds of playtime left! Stop what your doing and prepare to be disconnected!");
                this.plugin.sentPlayerWarningMessage(player.getName(), 10);
            } else if (timeLeft <= 60 && timeLeft > 10
                    && !this.plugin.hasPlayerSeenMessage(player.getName(), 60)) {
                player.sendMessage(ChatColor.RED
                        + "WARNING!"
                        + ChatColor.RESET
                        + " You have less than 60 seconds of playtime left! Stop what your doing and prepare to be disconnected!");
                this.plugin.sentPlayerWarningMessage(player.getName(), 60);
            } else if (timeLeft <= 300 && timeLeft > 60
                    && !this.plugin.hasPlayerSeenMessage(player.getName(), 300)) {
                player.sendMessage(ChatColor.RED
                        + "WARNING!"
                        + ChatColor.RESET
                        + " You have less than 5 minutes of playtime left! Stop what your doing and prepare to be disconnected!");
                this.plugin.sentPlayerWarningMessage(player.getName(), 300);
            }
        }
    }
}
