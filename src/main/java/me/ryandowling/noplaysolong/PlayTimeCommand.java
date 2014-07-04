/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayTimeCommand implements CommandExecutor {
    private final NoPlaySoLong plugin;

    public PlayTimeCommand(NoPlaySoLong plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (args.length >= 1 && !player.hasPermission("noplaysolong.playtime.others")) {
            player.sendMessage(ChatColor.RED
                    + "You don't have permission to check other players playtime!");
            return false;
        } else if (!player.hasPermission("noplaysolong.playtime.self")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to check your playtime!");
            return false;
        }

        if (args.length >= 1) {
            player.sendMessage(args[0] + " has played for " + plugin.getPlayerPlayTime(args[0])
                    + " seconds!");
        } else {
            player.sendMessage("You have played for " + plugin.getPlayerPlayTime(player.getName())
                    + " seconds!");
        }
        return true;
    }
}