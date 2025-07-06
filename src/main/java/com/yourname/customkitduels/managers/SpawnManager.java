package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SpawnManager {
    
    private final CustomKitDuels plugin;
    private final File spawnFile;
    private Location spawnLocation;
    
    public SpawnManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        loadSpawn();
    }
    
    private void loadSpawn() {
        if (!spawnFile.exists()) {
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(spawnFile);
        if (config.contains("spawn")) {
            spawnLocation = (Location) config.get("spawn");
        }
    }
    
    public void setSpawn(Location location) {
        this.spawnLocation = location;
        saveSpawn();
    }
    
    public Location getSpawn() {
        return spawnLocation;
    }
    
    public boolean hasSpawn() {
        return spawnLocation != null;
    }
    
    private void saveSpawn() {
        FileConfiguration config = new YamlConfiguration();
        if (spawnLocation != null) {
            config.set("spawn", spawnLocation);
        }
        
        try {
            config.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save spawn location: " + e.getMessage());
        }
    }
}