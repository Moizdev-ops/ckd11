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
    private final File kitSettingsFolder;
    private final Map<UUID, List<Kit>> playerKits;
    private final Map<String, Double> kitHearts; // UUID_kitName -> hearts
    private final Map<String, Boolean> kitNaturalRegen; // UUID_kitName -> naturalRegen
    
    public KitManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.kitsFolder = new File(plugin.getDataFolder(), "kits");
        this.kitSettingsFolder = new File(plugin.getDataFolder(), "kit-settings");
        this.playerKits = new HashMap<>();
        this.kitHearts = new HashMap<>();
        this.kitNaturalRegen = new HashMap<>();
        
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }
        if (!kitSettingsFolder.exists()) {
            kitSettingsFolder.mkdirs();
        }
        
        loadAllKits();
        loadAllKitSettings();
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
    
    private void loadAllKitSettings() {
        File[] settingsFiles = kitSettingsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (settingsFiles == null) return;
        
        for (File file : settingsFiles) {
            try {
                UUID playerId = UUID.fromString(file.getName().replace(".yml", ""));
                loadPlayerKitSettings(playerId);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid kit settings file: " + file.getName());
            }
        }
    }
    
    private void loadPlayerKitSettings(UUID playerId) {
        File file = new File(kitSettingsFolder, playerId.toString() + ".yml");
        if (!file.exists()) return;
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        for (String kitName : config.getKeys(false)) {
            String key = playerId.toString() + "_" + kitName;
            kitHearts.put(key, config.getDouble(kitName + ".hearts", 10.0)); // Changed default to 10
            kitNaturalRegen.put(key, config.getBoolean(kitName + ".naturalRegen", true));
        }
    }
    
    private void savePlayerKitSettings(UUID playerId) {
        File file = new File(kitSettingsFolder, playerId.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        String playerIdStr = playerId.toString();
        boolean hasSettings = false;
        
        for (Map.Entry<String, Double> entry : kitHearts.entrySet()) {
            if (entry.getKey().startsWith(playerIdStr + "_")) {
                String kitName = entry.getKey().substring(playerIdStr.length() + 1);
                config.set(kitName + ".hearts", entry.getValue());
                hasSettings = true;
            }
        }
        
        for (Map.Entry<String, Boolean> entry : kitNaturalRegen.entrySet()) {
            if (entry.getKey().startsWith(playerIdStr + "_")) {
                String kitName = entry.getKey().substring(playerIdStr.length() + 1);
                config.set(kitName + ".naturalRegen", entry.getValue());
                hasSettings = true;
            }
        }
        
        if (!hasSettings) {
            if (file.exists()) {
                file.delete();
            }
            return;
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kit settings for player " + playerId + ": " + e.getMessage());
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
                
                // Load contents with null safety
                Object contentsObj = config.get(kitName + ".contents");
                ItemStack[] contents = null;
                if (contentsObj instanceof ItemStack[]) {
                    contents = (ItemStack[]) contentsObj;
                } else if (contentsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<ItemStack> contentsList = (List<ItemStack>) contentsObj;
                    contents = contentsList.toArray(new ItemStack[0]);
                }
                
                // Load armor with null safety
                Object armorObj = config.get(kitName + ".armor");
                ItemStack[] armor = null;
                if (armorObj instanceof ItemStack[]) {
                    armor = (ItemStack[]) armorObj;
                } else if (armorObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<ItemStack> armorList = (List<ItemStack>) armorObj;
                    armor = armorList.toArray(new ItemStack[0]);
                }
                
                // Ensure arrays are properly sized
                if (contents == null) {
                    contents = new ItemStack[37]; // 36 + 1 for offhand
                } else if (contents.length < 37) {
                    // Extend array to include offhand slot
                    ItemStack[] newContents = new ItemStack[37];
                    System.arraycopy(contents, 0, newContents, 0, Math.min(contents.length, 36));
                    contents = newContents;
                }
                
                if (armor == null) {
                    armor = new ItemStack[4];
                } else if (armor.length != 4) {
                    // Ensure armor array is exactly 4 slots
                    ItemStack[] newArmor = new ItemStack[4];
                    System.arraycopy(armor, 0, newArmor, 0, Math.min(armor.length, 4));
                    armor = newArmor;
                }
                
                Kit kit = new Kit(kitName, displayName, contents, armor);
                kits.add(kit);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit " + kitName + " for player " + playerId + ": " + e.getMessage());
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
            plugin.getLogger().severe("Failed to save kits for player " + playerId + ": " + e.getMessage());
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
            
            // Also remove kit settings
            String key = playerId.toString() + "_" + kitName;
            kitHearts.remove(key);
            kitNaturalRegen.remove(key);
            savePlayerKitSettings(playerId);
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
    
    // Kit Settings Methods
    public double getKitHearts(UUID playerId, String kitName) {
        String key = playerId.toString() + "_" + kitName;
        return kitHearts.getOrDefault(key, 10.0); // Changed default to 10
    }
    
    public void setKitHearts(UUID playerId, String kitName, double hearts) {
        String key = playerId.toString() + "_" + kitName;
        kitHearts.put(key, hearts);
        savePlayerKitSettings(playerId);
    }
    
    public boolean getKitNaturalRegen(UUID playerId, String kitName) {
        String key = playerId.toString() + "_" + kitName;
        return kitNaturalRegen.getOrDefault(key, true);
    }
    
    public void setKitNaturalRegen(UUID playerId, String kitName, boolean naturalRegen) {
        String key = playerId.toString() + "_" + kitName;
        kitNaturalRegen.put(key, naturalRegen);
        savePlayerKitSettings(playerId);
    }
}