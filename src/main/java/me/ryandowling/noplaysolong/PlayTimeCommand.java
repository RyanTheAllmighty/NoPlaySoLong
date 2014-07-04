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

        if (args.length == 0) {
            this.printUsage(player);
            return false;
        }

        if (args[0].equals("add") && args.length == 3) {
            if (!player.hasPermission("noplaysolong.playtime.add")) {
                player.sendMessage(ChatColor.RED
                        + "You don't have permission to add to a players playtime!!");
                return false;
            } else {
                try {
                    plugin.addPlayTime(args[1], Integer.parseInt(args[2]));
                    player.sendMessage(ChatColor.GREEN + "Added " + Integer.parseInt(args[2])
                            + " seconds of playtime to " + args[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Invalid number of seconds given!");
                    return false;
                }
                return true;
            }
        } else if (args[0].equals("check")) {
            if (args.length == 1) {
                if (!player.hasPermission("noplaysolong.playtime.check.self")) {
                    player.sendMessage(ChatColor.RED
                            + "You don't have permission to check your playtime!");
                    return false;
                } else {
                    player.sendMessage(ChatColor.GREEN + "You have played for "
                            + plugin.getPlayerPlayTime(player.getName()) + " seconds!");
                    return true;
                }
            } else {
                if (!player.hasPermission("noplaysolong.playtime.check.others")) {
                    player.sendMessage(ChatColor.RED
                            + "You don't have permission to check other players playtime!");
                    return false;
                } else {
                    player.sendMessage(ChatColor.GREEN + args[0] + " has played for "
                            + plugin.getPlayerPlayTime(args[0]) + " seconds!");
                    return true;
                }
            }
        }

        this.printUsage(player);

        return false;
    }

    public void printUsage(Player player) {
        String[] usage = {
                ChatColor.YELLOW + "/playtime usage:",
                ChatColor.AQUA + "/playtime add [user] [time]" + ChatColor.RESET
                        + " - Add time in seconds to the user's playtime.",
                ChatColor.AQUA + "/playtime check [user]" + ChatColor.RESET
                        + " - Check the time played for a given user, or if blank, for yourself." };
        player.sendMessage(usage);
    }
}