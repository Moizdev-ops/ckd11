package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {
    
    private final CustomKitDuels plugin;
    private final File arenasFolder;
    private final File schematicsFolder;
    private final Map<String, Arena> arenas;
    private final List<String> availableArenas;
    
    public ArenaManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.arenasFolder = new File(plugin.getDataFolder(), "arenas");
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        this.arenas = new HashMap<>();
        this.availableArenas = new ArrayList<>();
        
        // Create folders if they don't exist
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
        
        loadArenas();
    }
    
    public void loadArenas() {
        arenas.clear();
        availableArenas.clear();
        
        // Load from individual arena files
        File[] arenaFiles = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (arenaFiles != null) {
            for (File file : arenaFiles) {
                String arenaName = file.getName().replace(".yml", "");
                loadArena(arenaName);
            }
        }
        
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas (" + availableArenas.size() + " available for duels)");
    }
    
    private void loadArena(String arenaName) {
        File arenaFile = new File(arenasFolder, arenaName + ".yml");
        if (!arenaFile.exists()) return;
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
        Arena arena = new Arena(arenaName);
        
        try {
            // Load positions
            if (config.contains("pos1")) {
                arena.setPos1((Location) config.get("pos1"));
            }
            if (config.contains("pos2")) {
                arena.setPos2((Location) config.get("pos2"));
            }
            
            // Load spawn points
            if (config.contains("spawn1")) {
                arena.setSpawn1((Location) config.get("spawn1"));
            }
            if (config.contains("spawn2")) {
                arena.setSpawn2((Location) config.get("spawn2"));
            }
            
            // Load regeneration settings
            arena.setRegeneration(config.getBoolean("regeneration", false));
            arena.setSchematicName(config.getString("schematicName", arenaName.toLowerCase() + "_arena"));
            
            arenas.put(arenaName, arena);
            
            // Only add to available if fully configured
            if (arena.isComplete()) {
                availableArenas.add(arenaName);
            }
            
            plugin.getLogger().info("Loaded arena: " + arenaName + " (Complete: " + arena.isComplete() + ", Regen: " + arena.hasRegeneration() + ")");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load arena " + arenaName + ": " + e.getMessage());
        }
    }
    
    public void createArena(String name) {
        Arena arena = new Arena(name);
        arenas.put(name, arena);
        saveArena(arena);
        plugin.getLogger().info("Created new arena: " + name);
    }
    
    public void saveArena(Arena arena) {
        File arenaFile = new File(arenasFolder, arena.getName() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        // Save positions
        if (arena.getPos1() != null) {
            config.set("pos1", arena.getPos1());
        }
        if (arena.getPos2() != null) {
            config.set("pos2", arena.getPos2());
        }
        
        // Save spawn points
        if (arena.getSpawn1() != null) {
            config.set("spawn1", arena.getSpawn1());
        }
        if (arena.getSpawn2() != null) {
            config.set("spawn2", arena.getSpawn2());
        }
        
        // Save regeneration settings
        config.set("regeneration", arena.hasRegeneration());
        config.set("schematicName", arena.getSchematicName());
        
        try {
            config.save(arenaFile);
            
            // Update available arenas list
            if (arena.isComplete() && !availableArenas.contains(arena.getName())) {
                availableArenas.add(arena.getName());
            } else if (!arena.isComplete() && availableArenas.contains(arena.getName())) {
                availableArenas.remove(arena.getName());
            }
            
            plugin.getLogger().info("Saved arena: " + arena.getName());
            
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save arena " + arena.getName() + ": " + e.getMessage());
        }
    }
    
    public void generateSchematic(Arena arena, Player player) throws Exception {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            throw new Exception("FastAsyncWorldEdit (FAWE) is not installed!");
        }
        
        if (arena.getPos1() == null || arena.getPos2() == null) {
            throw new Exception("Arena positions are not set!");
        }
        
        try {
            // Use FAWE API to generate schematic with proper error handling
            com.sk89q.worldedit.world.World world = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getWorld());
            com.sk89q.worldedit.math.BlockVector3 min = com.sk89q.worldedit.bukkit.BukkitAdapter.asBlockVector(arena.getPos1());
            com.sk89q.worldedit.math.BlockVector3 max = com.sk89q.worldedit.bukkit.BukkitAdapter.asBlockVector(arena.getPos2());
            
            // Ensure min/max are properly ordered
            com.sk89q.worldedit.math.BlockVector3 actualMin = min.getMinimum(max);
            com.sk89q.worldedit.math.BlockVector3 actualMax = min.getMaximum(max);
            
            com.sk89q.worldedit.regions.CuboidRegion region = new com.sk89q.worldedit.regions.CuboidRegion(world, actualMin, actualMax);
            
            // Use a more robust clipboard creation approach
            com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard clipboard = 
                new com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard(region);
            
            // Create edit session with proper limits
            try (com.sk89q.worldedit.EditSession editSession = 
                 com.sk89q.worldedit.WorldEdit.getInstance().newEditSession(world)) {
                
                com.sk89q.worldedit.function.operation.ForwardExtentCopy copy = 
                    new com.sk89q.worldedit.function.operation.ForwardExtentCopy(editSession, region, clipboard, actualMin);
                
                // Set copy options to be more robust
                copy.setCopyingEntities(false);
                copy.setCopyingBiomes(false);
                
                com.sk89q.worldedit.function.operation.Operations.complete(copy);
            }
            
            // Save schematic
            File schematicFile = new File(schematicsFolder, arena.getSchematicName() + ".schem");
            com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat format = 
                com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat.SPONGE_SCHEMATIC;
            
            try (com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter writer = 
                 format.getWriter(new java.io.FileOutputStream(schematicFile))) {
                writer.write(clipboard);
            }
            
            plugin.getLogger().info("Generated schematic for arena " + arena.getName() + ": " + schematicFile.getName());
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to generate schematic for arena " + arena.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Schematic generation failed: " + e.getMessage());
        }
    }
    
    public void regenerateArena(Arena arena) {
        if (!arena.isRegenerationReady()) {
            plugin.getLogger().warning("Arena " + arena.getName() + " is not ready for regeneration!");
            return;
        }
        
        if (!plugin.getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            plugin.getLogger().warning("FastAsyncWorldEdit (FAWE) is required for arena regeneration!");
            return;
        }
        
        try {
            File schematicFile = new File(schematicsFolder, arena.getSchematicName() + ".schem");
            if (!schematicFile.exists()) {
                plugin.getLogger().warning("Schematic file not found for arena " + arena.getName() + ": " + schematicFile.getName());
                return;
            }
            
            // Load schematic using FAWE
            com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat format = 
                com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat.SPONGE_SCHEMATIC;
            
            com.sk89q.worldedit.extent.clipboard.Clipboard clipboard;
            try (com.sk89q.worldedit.extent.clipboard.io.ClipboardReader reader = 
                 format.getReader(new java.io.FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }
            
            com.sk89q.worldedit.world.World world = 
                com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(arena.getPos1().getWorld());
            
            // FIXED: Calculate the minimum corner for proper positioning
            Location pos1 = arena.getPos1();
            Location pos2 = arena.getPos2();
            
            // Get the minimum corner (bottom-left-back corner)
            int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            
            com.sk89q.worldedit.math.BlockVector3 pastePosition = 
                com.sk89q.worldedit.math.BlockVector3.at(minX, minY, minZ);
            
            try (com.sk89q.worldedit.EditSession editSession = 
                 com.sk89q.worldedit.WorldEdit.getInstance().newEditSession(world)) {
                
                // Set a reasonable block limit for regeneration
                editSession.setBlockChangeLimit(1000000);
                
                com.sk89q.worldedit.function.operation.Operation operation = 
                    new com.sk89q.worldedit.session.ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pastePosition)
                        .ignoreAirBlocks(false)
                        .copyEntities(false)
                        .copyBiomes(false)
                        .build();
                
                com.sk89q.worldedit.function.operation.Operations.complete(operation);
            }
            
            plugin.getLogger().info("Regenerated arena: " + arena.getName());
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to regenerate arena " + arena.getName() + ": " + e.getMessage());
            e.printStackTrace();
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
    
    public List<String> getAllArenas() {
        return new ArrayList<>(arenas.keySet());
    }
    
    public boolean hasArena(String name) {
        return arenas.containsKey(name);
    }
    
    public boolean deleteArena(String name) {
        Arena arena = arenas.remove(name);
        if (arena == null) return false;
        
        // Delete arena file
        File arenaFile = new File(arenasFolder, name + ".yml");
        if (arenaFile.exists()) {
            arenaFile.delete();
        }
        
        // Delete schematic file if it exists
        File schematicFile = new File(schematicsFolder, arena.getSchematicName() + ".schem");
        if (schematicFile.exists()) {
            schematicFile.delete();
        }
        
        availableArenas.remove(name);
        plugin.getLogger().info("Deleted arena: " + name);
        return true;
    }
}