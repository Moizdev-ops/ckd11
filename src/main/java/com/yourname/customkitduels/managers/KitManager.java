package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Kit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {
    
    private final CustomKitDuels plugin;
    private final File kitsFolder;
    private final Map<UUID, List<Kit>> playerKits;
    
    public KitManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.kitsFolder = new File(plugin.getDataFolder(), "kits");
        this.playerKits = new HashMap<>();
        
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }
        
        loadAllKits();
    }
    
    private void loadAllKits() {
        File[] playerFiles = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (playerFiles == null) return;
        
        for (File file : playerFiles) {
            try {
                UUID playerId = UUID.fromString(file.getName().replace(".yml", ""));
                loadPlayerKits(playerId);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid kit file: " + file.getName());
            }
        }
    }
    
    private void loadPlayerKits(UUID playerId) {
        File file = new File(kitsFolder, playerId.toString() + ".yml");
        if (!file.exists()) return;
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<Kit> kits = new ArrayList<>();
        
        for (String kitName : config.getKeys(false)) {
            try {
                String displayName = config.getString(kitName + ".displayName", kitName);
                ItemStack[] contents = (ItemStack[]) config.get(kitName + ".contents");
                ItemStack[] armor = (ItemStack[]) config.get(kitName + ".armor");
                
                if (contents != null && armor != null) {
                    Kit kit = new Kit(kitName, displayName, contents, armor);
                    kits.add(kit);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit " + kitName + " for player " + playerId);
            }
        }
        
        playerKits.put(playerId, kits);
    }
    
    public void savePlayerKits(UUID playerId) {
        File file = new File(kitsFolder, playerId.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        List<Kit> kits = playerKits.get(playerId);
        if (kits == null || kits.isEmpty()) {
            if (file.exists()) {
                file.delete();
            }
            return;
        }
        
        for (Kit kit : kits) {
            String path = kit.getName();
            config.set(path + ".displayName", kit.getDisplayName());
            config.set(path + ".contents", kit.getContents());
            config.set(path + ".armor", kit.getArmor());
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kits for player " + playerId);
        }
    }
    
    public void saveKit(UUID playerId, Kit kit) {
        List<Kit> kits = playerKits.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        // Remove existing kit with same name
        kits.removeIf(existingKit -> existingKit.getName().equals(kit.getName()));
        
        // Add new kit
        kits.add(kit);
        
        savePlayerKits(playerId);
    }
    
    public boolean deleteKit(UUID playerId, String kitName) {
        List<Kit> kits = playerKits.get(playerId);
        if (kits == null) return false;
        
        boolean removed = kits.removeIf(kit -> kit.getName().equals(kitName));
        if (removed) {
            savePlayerKits(playerId);
        }
        
        return removed;
    }
    
    public Kit getKit(UUID playerId, String kitName) {
        List<Kit> kits = playerKits.get(playerId);
        if (kits == null) return null;
        
        return kits.stream()
                .filter(kit -> kit.getName().equals(kitName))
                .findFirst()
                .orElse(null);
    }
    
    public List<Kit> getPlayerKits(UUID playerId) {
        return playerKits.getOrDefault(playerId, new ArrayList<>());
    }
    
    public boolean hasKit(UUID playerId, String kitName) {
        return getKit(playerId, kitName) != null;
    }
}