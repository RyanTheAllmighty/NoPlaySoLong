/**
 * Copyright 2014 by RyanTheAllmighty and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import me.ryandowling.noplaysolong.exceptions.UnknownPlayerException;
import me.ryandowling.noplaysolong.threads.PlayTimeCheckerTask;
import me.ryandowling.noplaysolong.threads.PlayTimeSaverTask;
import me.ryandowling.noplaysolong.threads.ShutdownThread;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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

    private boolean shutdownHookAdded = false;
    private Timer savePlayTimeTimer = null;
    private Timer checkPlayTimeTimer = null;

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

        if (!getConfig().isSet("timeStarted")) {
            getConfig().set("timeStarted", (System.currentTimeMillis() / 1000));
            saveConfig();
        }

        if (!getConfig().isSet("initialTime")) {
            getConfig().set("initialTime", 28800);
            saveConfig();
        }

        if (!getConfig().isSet("timePerDay")) {
            getConfig().set("timePerDay", 3600);
            saveConfig();
        }

        getLogger().info(
                String.format("Server started at %s which was %s seconds ago!",
                        getConfig().get("timeStarted"),
                        ((System.currentTimeMillis() / 1000) - getConfig().getInt("timeStarted"))));

        PluginDescriptionFile pdfFile = this.getDescription();
        getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");

        // Load the playtime from file
        this.loadPlayTime();

        if (savePlayTimeTimer == null) {
            this.savePlayTimeTimer = new Timer();
            this.savePlayTimeTimer.scheduleAtFixedRate(new PlayTimeSaverTask(this), 30 * 1000,
                    10 * 60 * 1000);
        }

        if (checkPlayTimeTimer == null) {
            this.checkPlayTimeTimer = new Timer();
            this.checkPlayTimeTimer.scheduleAtFixedRate(new PlayTimeCheckerTask(this), 30 * 1000,
                    1 * 60 * 1000);
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
            this.timePlayed.put(player, this.timePlayed.get(player) + seconds);
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

    @SuppressWarnings("unchecked")
    public void loadPlayTime() {
        File file = new File(getDataFolder(), "playtime.dat");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            getLogger().warning("playtime.dat file missing! Not loading in values");
            return;
        }
        getLogger().info("Loading data from playtime.dat");
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            this.timePlayed = (Map<String, Integer>) ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (fis != null) {
                fis.close();
            }
            if (ois != null) {
                ois.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayTime() {
        this.savePlayTime(false);
    }

    public void savePlayTime(boolean force) {
        if (force) {
            for (String key : this.timeLoggedIn.keySet()) {
                this.setPlayerLoggedOut(key);
            }
        }
        File file = new File(getDataFolder(), "playtime.dat");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        getLogger().info("Saving data to playtime.dat");
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(this.timePlayed);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (fos != null) {
                fos.close();
            }
            if (oos != null) {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}