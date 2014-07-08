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

import me.ryandowling.noplaysolong.exceptions.UnknownPlayerException;

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

    @Override
    public void onDisable() {
        this.savePlayTime(); // Save the playtime to file on plugin disable
    }

    @Override
    public void onEnable() {
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(blockListener, this);

        // Register our commands
        getCommand("playtime").setExecutor(new PlayTimeCommand(this));

        PluginDescriptionFile pdfFile = this.getDescription();
        getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");

        // Load the playtime from file
        this.loadPlayTime();
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
            this.timePlayed.put(player, timePlayed);
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