package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ArenaManager {
    
    private final CustomKitDuels plugin;
    private final Map<String, Arena> arenas;
    private final List<String> availableArenas;
    
    public ArenaManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.availableArenas = new ArrayList<>();
        
        loadArenas();
    }
    
    public void loadArenas() {
        arenas.clear();
        availableArenas.clear();
        
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection arenasSection = config.getConfigurationSection("arenas");
        
        if (arenasSection == null) return;
        
        for (String arenaName : arenasSection.getKeys(false)) {
            ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaName);
            if (arenaSection == null) continue;
            
            Arena arena = new Arena(arenaName);
            
            // Load positions
            if (arenaSection.contains("pos1")) {
                arena.setPos1((Location) arenaSection.get("pos1"));
            }
            if (arenaSection.contains("pos2")) {
                arena.setPos2((Location) arenaSection.get("pos2"));
            }
            
            // Load spawn points
            if (arenaSection.contains("spawn1")) {
                arena.setSpawn1((Location) arenaSection.get("spawn1"));
            }
            if (arenaSection.contains("spawn2")) {
                arena.setSpawn2((Location) arenaSection.get("spawn2"));
            }
            
            arenas.put(arenaName, arena);
            
            // Only add to available if fully configured
            if (arena.isComplete()) {
                availableArenas.add(arenaName);
            }
        }
    }
    
    public void createArena(String name) {
        Arena arena = new Arena(name);
        arenas.put(name, arena);
        saveArena(arena);
    }
    
    public void saveArena(Arena arena) {
        FileConfiguration config = plugin.getConfig();
        String path = "arenas." + arena.getName();
        
        if (arena.getPos1() != null) {
            config.set(path + ".pos1", arena.getPos1());
        }
        if (arena.getPos2() != null) {
            config.set(path + ".pos2", arena.getPos2());
        }
        if (arena.getSpawn1() != null) {
            config.set(path + ".spawn1", arena.getSpawn1());
        }
        if (arena.getSpawn2() != null) {
            config.set(path + ".spawn2", arena.getSpawn2());
        }
        
        plugin.saveConfig();
        
        // Update available arenas list
        if (arena.isComplete() && !availableArenas.contains(arena.getName())) {
            availableArenas.add(arena.getName());
        }
    }
    
    public Arena getArena(String name) {
        return arenas.get(name);
    }
    
    public Arena getRandomAvailableArena() {
        if (availableArenas.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        String arenaName = availableArenas.get(random.nextInt(availableArenas.size()));
        return arenas.get(arenaName);
    }
    
    public List<String> getAvailableArenas() {
        return new ArrayList<>(availableArenas);
    }
    
    public boolean hasArena(String name) {
        return arenas.containsKey(name);
    }
}