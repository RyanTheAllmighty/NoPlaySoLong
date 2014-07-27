/**
 * Copyright 2014 by RyanTheAllmighty and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import me.ryandowling.noplaysolong.exceptions.UnknownPlayerException;
import me.ryandowling.noplaysolong.threads.PlayTimeCheckerTask;
import me.ryandowling.noplaysolong.threads.PlayTimeSaverTask;
import me.ryandowling.noplaysolong.threads.ShutdownThread;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * No Play So Long plugin for Bukkit
 * 
 * @author RyanTheAllmighty
 */
public class NoPlaySoLong extends JavaPlugin {
    private final PlayerListener playerListener = new PlayerListener(this);
    private final BlockListener blockListener = new BlockListener();
    private Map<String, Integer> timePlayed = new HashMap<String, Integer>();
    private Map<String, Integer> timeLoggedIn = new HashMap<String, Integer>();
    private Map<String, Boolean> seenWarningMessages = new HashMap<String, Boolean>();

    private boolean shutdownHookAdded = false;
    private Timer savePlayTimeTimer = null;
    private Timer checkPlayTimeTimer = null;
    private boolean started = false;
    private final Gson GSON = new Gson();

    @Override
    public void onDisable() {
        this.savePlayTime(); // Save the playtime to file on plugin disable
    }

    @Override
    public void onEnable() {
        if (!this.shutdownHookAdded) {
            this.shutdownHookAdded = true;
            try {
                Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(blockListener, this);

        // Register our commands
        getCommand("playtime").setExecutor(new PlayTimeCommand(this));

        if (getConfig().isSet("timeStarted")) {
            this.started = true;
        }

        if (!getConfig().isSet("initialTime")) {
            getConfig().set("initialTime", 28800);
            saveConfig();
        }

        if (!getConfig().isSet("timePerDay")) {
            getConfig().set("timePerDay", 3600);
            saveConfig();
        }

        if (!getConfig().isSet("secondsBetweenPlayTimeChecks")) {
            getConfig().set("secondsBetweenPlayTimeChecks", 10);
            saveConfig();
        }

        if (!getConfig().isSet("secondsBetweenPlayTimeSaving")) {
            getConfig().set("secondsBetweenPlayTimeSaving", 600);
            saveConfig();
        }

        getLogger()
                .info(String.format("Server started at %s which was %s seconds ago!", getConfig()
                        .get("timeStarted"), this.secondsToDaysHoursSecondsString((int) ((System
                        .currentTimeMillis() / 1000) - getConfig().getInt("timeStarted")))));

        PluginDescriptionFile pdfFile = this.getDescription();
        getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");

        // Load the playtime from file
        this.loadPlayTime();

        if (savePlayTimeTimer == null) {
            this.savePlayTimeTimer = new Timer();
            this.savePlayTimeTimer.scheduleAtFixedRate(new PlayTimeSaverTask(this), 30000,
                    getConfig().getInt("secondsBetweenPlayTimeSaving") * 1000);
        }

        if (checkPlayTimeTimer == null) {
            this.checkPlayTimeTimer = new Timer();
            this.checkPlayTimeTimer.scheduleAtFixedRate(new PlayTimeCheckerTask(this), 30000,
                    getConfig().getInt("secondsBetweenPlayTimeChecks") * 1000);
        }
    }

    public int secondsUntilNextDay() {
        int timeStarted = getConfig().getInt("timeStarted");
        return (int) ((System.currentTimeMillis() / 1000) - timeStarted);
    }

    public String secondsToDaysHoursSecondsString(int secondsToConvert) {
        int hours = secondsToConvert / 3600;
        int minutes = (secondsToConvert % 3600) / 60;
        int seconds = secondsToConvert % 60;
        return String.format("%02d hours, %02d minutes & %02d seconds (%d)", hours, minutes,
                seconds, secondsToConvert);
    }

    public int getTimeAllowedInSeconds(String player) {
        int timeStarted = getConfig().getInt("timeStarted");
        int secondsSince = (int) ((System.currentTimeMillis() / 1000) - timeStarted);
        int secondsAllowed = 0;

        // Add the initial time we give the player at the beginning
        secondsAllowed += getConfig().getInt("initialTime");

        // Then for each day including the first day (24 hours realtime) add the set amount of
        // seconds to the time allowed
        while (secondsSince >= 0) {
            secondsAllowed += getConfig().getInt("timePerDay");
            secondsSince -= 86400;
        }

        // Remove the amount of time the player has played to get their time allowed
        secondsAllowed -= getPlayerPlayTime(player);

        return secondsAllowed;
    }

    public void addPlayTime(String player, int seconds) {
        if (this.timePlayed.containsKey(player)) {
            this.timePlayed.put(player, this.timePlayed.get(player) + seconds);
        } else {
            this.timePlayed.put(player, seconds);
        }
    }

    public void removePlayTime(String player, int seconds) throws UnknownPlayerException {
        if (this.timePlayed.containsKey(player)) {
            this.timePlayed.put(player, this.timePlayed.get(player) - seconds);
        } else {
            throw new UnknownPlayerException(player);
        }
    }

    public int getPlayerPlayTime(String player) {
        int timePlayed = 0;
        if (this.timePlayed.containsKey(player)) {
            timePlayed += this.timePlayed.get(player);
        }
        if (this.timeLoggedIn.containsKey(player)) {
            timePlayed += (int) ((System.currentTimeMillis() / 1000) - this.timeLoggedIn
                    .get(player));
        }
        return timePlayed;
    }

    public void setPlayerLoggedIn(String player) {
        if (!this.timePlayed.containsKey(player)) {
            this.timePlayed.put(player, 0);
            this.savePlayTime();
        }
        this.timeLoggedIn.put(player, (int) (System.currentTimeMillis() / 1000));
    }

    public void setPlayerLoggedOut(String player) {
        if (this.timeLoggedIn.containsKey(player)) {
            int timePlayed = (int) ((System.currentTimeMillis() / 1000) - this.timeLoggedIn
                    .get(player));
            if (this.timePlayed.containsKey(player)) {
                this.timePlayed.put(player, this.timePlayed.get(player) + timePlayed);
            } else {
                this.timePlayed.put(player, timePlayed);
            }
            this.timeLoggedIn.remove(player);
            getLogger().info(
                    "Player " + player + " played for a total of " + timePlayed + " seconds!");
            this.savePlayTime();
        }
    }

    public boolean hasPlayerSeenMessage(String player, int time) {
        if (this.seenWarningMessages.containsKey(player + ":" + time)) {
            return this.seenWarningMessages.get(player + ":" + time);
        } else {
            return false;
        }
    }

    public void sentPlayerWarningMessage(String player, int time) {
        this.seenWarningMessages.put(player + ":" + time, true);
    }

    public boolean start() {
        if (this.started) {
            return false;
        } else {
            this.started = true;
            String initial = (getConfig().getInt("initialTime") / 60 / 60) + "";
            String perday = (getConfig().getInt("timePerDay") / 60 / 60) + "";
            getServer().broadcastMessage(
                    ChatColor.GREEN + "Playtime has now started! You have " + initial
                            + " hour/s of playtime to start with and " + perday
                            + " hour/s of playtime added per day!");
            getConfig().set("timeStarted", (System.currentTimeMillis() / 1000));
            saveConfig();
            return true;
        }
    }

    public boolean hasStarted() {
        return this.started;
    }

    public void loadPlayTime() {
        if (!hasStarted()) {
            return;
        }
        File file = new File(getDataFolder(), "playtime.json");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            getLogger().warning("playtime.json file missing! Not loading in values");
            return;
        }
        getLogger().info("Loading data from playtime.json");
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            java.lang.reflect.Type type = new TypeToken<Map<String, Integer>>() {
            }.getType();
            this.timePlayed = GSON.fromJson(fileReader, type);
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayTime() {
        this.savePlayTime(false);
    }

    public void savePlayTime(boolean force) {
        if (!hasStarted()) {
            return;
        }

        if (force) {
            for (String key : this.timeLoggedIn.keySet()) {
                this.setPlayerLoggedOut(key);
            }
        }
        File file = new File(getDataFolder(), "playtime.json");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        getLogger().info("Saving data to playtime.json");
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(GSON.toJson(this.timePlayed));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (bw != null) {
                bw.close();
            }
            if (fw != null) {
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}