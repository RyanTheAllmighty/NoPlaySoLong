/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong;

import java.util.ArrayList;
import java.util.List;

import me.ryandowling.noplaysolong.exceptions.UnknownPlayerException;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlayTimeCommand implements CommandExecutor {
    private final NoPlaySoLong plugin;

    public PlayTimeCommand(NoPlaySoLong plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isPlayer = true;
        Server server = plugin.getServer();
        ConsoleCommandSender console = server.getConsoleSender();

        if (!(sender instanceof Player)) {
            isPlayer = false;
        }

        if (args.length == 0) {
            this.printUsage(sender);
            return false;
        }

        if (args[0].equals("start") && args.length == 1) {
            if (!sender.hasPermission("noplaysolong.start")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to start the playtime counter!");
                return false;
            } else {
                if (plugin.start()) {
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Playtime already started!");
                    return false;
                }
            }
        } else if (args[0].equals("add") && args.length == 3) {
            if (!plugin.hasStarted()) {
                sender.sendMessage(ChatColor.RED + "Playtime hasn't started yet!");
                return false;
            }
            if (!sender.hasPermission("noplaysolong.playtime.add")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to add time to a players playtime!");
                return false;
            } else {
                try {
                    plugin.addPlayTime(args[1], Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.GREEN + "Added " + Integer.parseInt(args[2])
                            + " seconds of playtime to " + args[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Invalid number of seconds given!");
                    return false;
                }
                return true;
            }
        } else if (args[0].equals("remove") && args.length == 3) {
            if (!plugin.hasStarted()) {
                sender.sendMessage(ChatColor.RED + "Playtime hasn't started yet!");
                return false;
            }
            if (!sender.hasPermission("noplaysolong.playtime.remove")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to remove time from a players playtime!!");
                return false;
            } else {
                try {
                    plugin.removePlayTime(args[1], Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.GREEN + "Removed " + Integer.parseInt(args[2])
                            + " seconds of playtime from " + args[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Invalid number of seconds given!");
                    return false;
                } catch (UnknownPlayerException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }
                return true;
            }
        } else if (args[0].equals("check")) {
            if (!plugin.hasStarted()) {
                sender.sendMessage(ChatColor.RED + "Playtime hasn't started yet!");
                return false;
            }
            if (args.length == 1 && isPlayer) {
                if (!sender.hasPermission("noplaysolong.playtime.check.self")) {
                    sender.sendMessage(ChatColor.RED
                            + "You don't have permission to check your playtime!");
                    return false;
                } else {
                    sender.sendMessage(ChatColor.GREEN
                            + "You have played for "
                            + plugin.secondsToDaysHoursSecondsString(plugin
                                    .getPlayerPlayTime(sender.getName()))
                            + " and have "
                            + plugin.secondsToDaysHoursSecondsString(plugin
                                    .getTimeAllowedInSeconds(sender.getName())) + " remaining!");
                    return true;
                }
            } else if (args.length == 2) {
                if (!sender.hasPermission("noplaysolong.playtime.check.others")) {
                    sender.sendMessage(ChatColor.RED
                            + "You don't have permission to check other players playtime!");
                    return false;
                } else {
                    sender.sendMessage(ChatColor.GREEN
                            + args[1]
                            + " has played for "
                            + plugin.secondsToDaysHoursSecondsString(plugin
                                    .getPlayerPlayTime(args[1]))
                            + " and has "
                            + plugin.secondsToDaysHoursSecondsString(plugin
                                    .getTimeAllowedInSeconds(args[1])) + " remaining!");
                    return true;
                }
            }
        }

        this.printUsage(sender);

        return false;
    }

    public void printUsage(CommandSender sender) {
        List<String> usage = new ArrayList<String>();
        usage.add(ChatColor.YELLOW + "/playtime usage:");
        if (sender.hasPermission("noplaysolong.playtime.add")) {
            usage.add(ChatColor.AQUA + "/playtime add [user] [time]" + ChatColor.RESET
                    + " - Add time in seconds to the user's playtime.");
        }
        if (sender.hasPermission("noplaysolong.playtime.check.others")) {
            usage.add(ChatColor.AQUA
                    + "/playtime check [user]"
                    + ChatColor.RESET
                    + " - Check the time played and time left for a given user, or if blank, for yourself.");
        } else if (sender.hasPermission("noplaysolong.playtime.check.self")) {
            usage.add(ChatColor.AQUA + "/playtime check" + ChatColor.RESET
                    + " - Check the time played and time left for yourself.");
        }
        if (sender.hasPermission("noplaysolong.playtime.remove")) {
            usage.add(ChatColor.AQUA + "/playtime remove [user] [time]" + ChatColor.RESET
                    + " - Remove time in seconds from the user's playtime.");
        }
        sender.sendMessage(usage.toArray(new String[usage.size()]));
    }
}