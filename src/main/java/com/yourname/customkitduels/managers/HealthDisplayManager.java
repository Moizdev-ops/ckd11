package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced health display manager with Adventure API support
 * Uses custom names below player names with proper hex colors
 */
public class HealthDisplayManager {
    
    private final CustomKitDuels plugin;
    private final Map<UUID, BukkitRunnable> healthTasks;
    private final Map<UUID, String> originalNames; // Store original custom names
    private final Map<UUID, Boolean> originalNameVisibility; // Store original visibility
    
    public HealthDisplayManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.healthTasks = new HashMap<>();
        this.originalNames = new HashMap<>();
        this.originalNameVisibility = new HashMap<>();
        
        plugin.getLogger().info("HealthDisplayManager initialized with Adventure API support");
    }
    
    /**
     * Start health display for a player
     */
    public void startHealthDisplay(Player player) {
        stopHealthDisplay(player); // Stop any existing display
        
        // Store original custom name and visibility
        originalNames.put(player.getUniqueId(), player.getCustomName());
        originalNameVisibility.put(player.getUniqueId(), player.isCustomNameVisible());
        
        BukkitRunnable healthTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    healthTasks.remove(player.getUniqueId());
                    return;
                }
                
                updateHealthDisplay(player);
            }
        };
        
        healthTask.runTaskTimer(plugin, 0L, 10L); // Update every 0.5 seconds
        healthTasks.put(player.getUniqueId(), healthTask);
        
        plugin.getLogger().info("Started health display for " + player.getName());
    }
    
    /**
     * Stop health display for a player
     */
    public void stopHealthDisplay(Player player) {
        BukkitRunnable task = healthTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        // Restore original custom name and visibility
        String originalName = originalNames.remove(player.getUniqueId());
        Boolean originalVisibility = originalNameVisibility.remove(player.getUniqueId());
        
        player.setCustomName(originalName);
        player.setCustomNameVisible(originalVisibility != null ? originalVisibility : false);
        
        plugin.getLogger().info("Stopped health display for " + player.getName());
    }
    
    /**
     * Update health display for a player with Adventure API colors
     */
    private void updateHealthDisplay(Player player) {
        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();
        
        // Create health display using Adventure API
        String healthDisplayText = ColorUtils.createHealthDisplay(player.getName(), health, maxHealth);
        
        // Translate to legacy format for custom name
        String translatedText = ColorUtils.translateColors(healthDisplayText);
        
        // Set custom name with health display
        player.setCustomName(translatedText);
        player.setCustomNameVisible(true);
    }
    
    /**
     * Check if health display is active for a player
     */
    public boolean isHealthDisplayActive(Player player) {
        return healthTasks.containsKey(player.getUniqueId());
    }
    
    /**
     * Clean up all health displays
     */
    public void cleanup() {
        // Cancel all tasks
        for (BukkitRunnable task : healthTasks.values()) {
            task.cancel();
        }
        healthTasks.clear();
        
        // Restore all original names and visibility
        for (Map.Entry<UUID, String> entry : originalNames.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                Boolean originalVisibility = originalNameVisibility.get(entry.getKey());
                player.setCustomName(entry.getValue());
                player.setCustomNameVisible(originalVisibility != null ? originalVisibility : false);
            }
        }
        originalNames.clear();
        originalNameVisibility.clear();
        
        plugin.getLogger().info("HealthDisplayManager cleaned up");
    }
}